
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.maverick.MyJSONParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class JSONParserTest {


    @Tag("test-empty-json-string")
    @DisplayName("Empty JSON")
    @Test
    public void testEmptyJson(){
        MyJSONParser parser = new MyJSONParser();
        assertTrue(parser.validJSON("{}"));
        assertTrue(parser.validJSON("{\n\n}"));
        assertTrue(parser.validJSON(" {} \n"));
        assertTrue(parser.validJSON("\0{}"));
        assertFalse(parser.validJSON(" {}a"));
        assertTrue(parser.validJSON("{}"));
        assertTrue(parser.validJSON("{   \n \t }"));
        assertTrue(parser.validJSON("  { }  \n"));
        assertFalse(parser.validJSON("  1{{ }  \n"));
    }


    @Tag("trim-whitespaces")
    @DisplayName("Trim Whitespaces")
    @Test
    public void testTrim() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String s1 = "    ";
        String s2 = " \0   ";
        String s3 = "    \tr";
        String s4 = "\t";
        String s5 = " qqqqq ";
        String s6 = "qqqqq";
        String s7 = "\"";
        MyJSONParser parser = new MyJSONParser();
        Method method = parser.getClass().getDeclaredMethod("trim", String.class);
        method.setAccessible(true);
        MyJSONParser.INDEX a = (MyJSONParser.INDEX) method.invoke(parser, s1);
        assertEquals(a.start(), a.end());
        a = (MyJSONParser.INDEX) method.invoke(parser, s2);
        assertEquals(a.start(), a.end());
        a = (MyJSONParser.INDEX) method.invoke(parser, s3);
        assertEquals(a.start(), a.end());
        assertEquals(a.end() - a.start() + 1, 1);
        a = (MyJSONParser.INDEX) method.invoke(parser, s4);
        assertEquals(a.start(), a.end());
        a = (MyJSONParser.INDEX) method.invoke(parser, s5);
        assertEquals(a.start(), 1);
        assertEquals(a.end(), 5);
        assertEquals(a.end() - a.start() + 1, 5);
        a = (MyJSONParser.INDEX) method.invoke(parser, s6);
        assertEquals(a.start(),0);
        assertEquals(a.end(),4);
        assertEquals(a.end() - a.start() + 1, 5);
        a = (MyJSONParser.INDEX) method.invoke(parser, s7);
        assertEquals(a.start(),0);
        assertEquals(a.end(),0);

    }


    @Tag("string-token")
    @DisplayName("String Tokens")
    @Test
    public void testStringToken() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String json = " {\n \" \":}\n  \t";
        MyJSONParser parser = new MyJSONParser();
        Method methodTrim = parser.getClass().getDeclaredMethod("trim", String.class);
        Method methodToken = parser.getClass().getDeclaredMethod("nextStringToken", String.class, int.class, int.class);
        methodTrim.setAccessible(true);
        methodToken.setAccessible(true);
        MyJSONParser.INDEX idx = (MyJSONParser.INDEX) methodTrim.invoke(parser, json);
        MyJSONParser.INDEX tokenIdx = (MyJSONParser.INDEX) methodToken.invoke(parser, json,4, 12);
        assertNotEquals(tokenIdx.end(), tokenIdx.start());
        assertEquals(1, idx.start());
        assertEquals(8 , idx.end());
        assertEquals(4, tokenIdx.start());
        assertEquals(6, tokenIdx.end());

        json = "{  \"   \"  : \" 123:43 \" }   ";
        idx = (MyJSONParser.INDEX) methodTrim.invoke(parser, json);
        assertEquals(0, idx.start());
        assertEquals(23, idx.end());
        tokenIdx = (MyJSONParser.INDEX) methodToken.invoke(parser, json, 3, 26);
        assertEquals(3, tokenIdx.start());
        assertEquals(7, tokenIdx.end());
        tokenIdx = (MyJSONParser.INDEX) methodToken.invoke(parser, json, 7, 22);
        assertEquals(7, tokenIdx.start());
        assertEquals(12, tokenIdx.end());
        json = "{  \"  \" d : }   ";
        idx = (MyJSONParser.INDEX) methodTrim.invoke(parser, json);
        assertEquals(0, idx.start());
        assertEquals(12, idx.end());
        tokenIdx = (MyJSONParser.INDEX) methodToken.invoke(parser, json, 3, idx.end());
        assertEquals(6, tokenIdx.end());
        tokenIdx = (MyJSONParser.INDEX) methodToken.invoke(parser, json, 7, idx.end());
        assertEquals(7, tokenIdx.start());
        assertEquals(-1, tokenIdx.end());
    }


    @Tag("numeric-token")
    @DisplayName("Numeric Tokens")
    @Test
    public void testNextNumericToken() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        MyJSONParser parser = new MyJSONParser();
        Class<MyJSONParser> clazz= MyJSONParser.class;
        Method method_Numeric = clazz.getDeclaredMethod("nextNumericToken", String.class, int.class, int.class);
        method_Numeric.setAccessible(true);

        MyJSONParser.INDEX idx = (MyJSONParser.INDEX) method_Numeric.invoke(parser, "12344\n,", 0, 6);
        assertEquals(4, idx.end());
        idx = (MyJSONParser.INDEX) method_Numeric.invoke(parser, "12344p,", 0, 5);
        assertEquals(4, idx.end());
        idx = (MyJSONParser.INDEX) method_Numeric.invoke(parser, "\0\n\t12344 ,",3, 9);
        assertEquals(7, idx.end());
        idx = (MyJSONParser.INDEX) method_Numeric.invoke(parser, "\0\n\t12344",3, 7);
        assertEquals(7, idx.end());
        idx = (MyJSONParser.INDEX) method_Numeric.invoke(parser, "\0\n\t123a44 ,:", 3, 10);
        assertEquals(5, idx.end());

    }

    @Tag("value-token")
    @DisplayName("Value Tokens (null, true, false)")
    @Test
    public void testNextValueToken() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MyJSONParser parser = new MyJSONParser();
        Class<MyJSONParser> clazz= MyJSONParser.class;
        Method method_Value = clazz.getDeclaredMethod("nextValueToken", String.class, int.class, int.class, char.class);
        method_Value.setAccessible(true);
        MyJSONParser.INDEX idx = (MyJSONParser.INDEX) method_Value.invoke(parser, "12344\n,", 0, 6, 'n');
        assertEquals(-1, idx.end());
        idx = (MyJSONParser.INDEX) method_Value.invoke(parser, "true\n ,\t ",0, 8, 't');
        assertEquals(3, idx.end());
        idx = (MyJSONParser.INDEX) method_Value.invoke(parser, "uutrue1 ,\t ",2, 6, 't');
        assertEquals(5, idx.end());
        idx = (MyJSONParser.INDEX) method_Value.invoke(parser, "fse,",0, 4, 'n');
        assertEquals(-1, idx.end());

    }


    @Tag("array-token")
    @DisplayName("Array Tokens [...]")
    @Test
    public void testNextArrayToken() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        MyJSONParser parser = new MyJSONParser();
        Class<MyJSONParser> clazz= MyJSONParser.class;
        Method method_Value = clazz.getDeclaredMethod("nextArrayToken", String.class, int.class, int.class);
        method_Value.setAccessible(true);
        MyJSONParser.INDEX idx = (MyJSONParser.INDEX) method_Value.invoke(parser, "[  \n]",0, 4);
        assertEquals(4, idx.end());
        idx = (MyJSONParser.INDEX) method_Value.invoke(parser, "[\"t\",\"true\",\"h\",null  \n]",0, 23);
        assertEquals(23, idx.end());
        idx = (MyJSONParser.INDEX) method_Value.invoke(parser, "[\"t\";\"true\"]",0, 23);
        assertEquals(-1, idx.end());
        idx = (MyJSONParser.INDEX) method_Value.invoke(parser, "[\"t\";\"true\",[], {\"\":\"\"}]",0, 23);
        assertEquals(-1, idx.end());
        idx = (MyJSONParser.INDEX) method_Value.invoke(parser, "[[], {\"d\": \"d\"}]",0, 15);
        assertEquals(15, idx.end());

    }

    @Tag("valid-json")
    @DisplayName("Validate JSON")
    @Test
    public void validateJson(){
        MyJSONParser parser = new MyJSONParser();
        assertTrue(parser.validJSON("{\"fff\":\"hhh\", \"kk\":\"kkkk\"}"));
        assertTrue(parser.validJSON("{   \"key\": \"value\",   \"key2\": \"value\" }"));
        assertTrue(parser.validJSON("{   \"key\": \"value\",   \"key2\": \"value\", \"lastName\":\"Smith\" }"));
        assertFalse(parser.validJSON("{   \"key\": \"value\",   \"key2\": \"value\", \"lastName\":\"Smith\" 2 }"));
        assertFalse(parser.validJSON("{   hh\"key\": \"value\",   \"key2\": \"value\", \"lastName\":\"Smith\"  }"));
        assertFalse(parser.validJSON("{{}}"));
        assertFalse(parser.validJSON("{\"key\": \"value\",}"));
        assertFalse(parser.validJSON("{   \"key\": \"value\",   key2: \"value\" }"));
        assertTrue(parser.validJSON("{   \"key\": \"value\",   \"key2\": \"value\"\r, \"key\"\n: 1234 }"));
        assertTrue(parser.validJSON("{\"y :,\nyy\" : \"zzz\"}"));
        assertTrue(parser.validJSON("{\"key\": null}"));
        assertTrue(parser.validJSON("{\"key\": -1234}"));
        assertFalse(parser.validJSON("{\"key\": 1234,}"));
        assertTrue(parser.validJSON("{   \"key1\": true,   \"key2\": false,   \"key3\": null,   \"key4\": \"value\",   \"key5\": -101 }"));
        assertFalse(parser.validJSON("{   \"key1\": true,   \"key2\": False,   \"key3\": null,   \"key4\": \"value\",   \"key5\": [] }"));
        assertTrue(parser.validJSON("{   \"key\": \"value\",   \"key-n\": 101,   \"key-o\": {},   \"key-l\": [] }"));
        assertTrue(parser.validJSON("{   \"key\": \"value\",   \"key-n\": 101,   \"key-o\": {     \"inner key\": \"inner value\"   },   \"key-l\": [\"list value\"] }"));
        assertFalse(parser.validJSON("{   \"key\": \"value\",   \"key-n\": 101,   \"key-o\": {     \"inner key\": \"inner value\"   },   \"key-l\": ['list value'] }"));
    }


}
