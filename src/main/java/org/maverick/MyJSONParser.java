package org.maverick;


public class MyJSONParser implements JSONParser {

    private static final char BEGIN_CURLY = '{';
    private static final char END_CURLY = '}';
    private static final char QUOTES = '"';

    private static final char BEGIN_ARRAY = '[';
    private static final char END_ARRAY = ']';
    private static final char COLON = ':';
    private static final char COMMA = ',';
    private static final char NULL = 'n';
    private static final char TRUE = 't';
    private static final char FALSE = 'f';
    private static final char ZERO = '0';
    private static final char NINE = '9';
    private static final char NEGATIVE = '-';
    private static final String NULL_STR = "NULL";
    private static final String TRUE_STR = "TRUE";
    private static final String FALSE_STR = "FALSE";


    public MyJSONParser() {

    }

    /**
     * Inner Record to store the starting and the ending index of a token
     * the attribute end will contain -1 if the token is invalid
     *
     * @param start starting index of the token
     * @param end   ending index of the token; -1 if the token is invalid
     */

    public record INDEX(int start, int end) {
    }


    @Override
    public boolean validJSON(String s) {

        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("String of length greater than zero expected");
        }

        INDEX index = this.trim(s);
        if (index.end == index.start) {
            return false;
        }

        if (s.charAt(index.start) != BEGIN_CURLY || s.charAt(index.end) != END_CURLY) {
            return false;
        }

        if (index.start + 1 == index.end) {
            return true;
        }
        return nextObjectToken(s, index.start, index.end).end == index.end;

    }

    /**
     * Method to extract index position of next object token
     * an object token is contained in curly braces. Eg { }
     *
     * @param s     the string.
     * @param start the position from where to start scanning for the token
     * @param end   end index of the string s.
     * @return INDEX
     */
    private INDEX nextObjectToken(String s, int start, int end) {

        INDEX kIdx, vIdx;
        int nxt, separatorIdx;
        char nxtCh, sep;

        for (int i = start + 1; i <= end; i++) {
            nxtCh = s.charAt(i);

            if (nxtCh > 32) {

                if (nxtCh == END_CURLY) {
                    return new INDEX(start, i);
                }

                if (nxtCh != QUOTES) {
                    return new INDEX(start, -1);
                }

                kIdx = this.nextStringToken(s, i, end);
                separatorIdx = validateSeparator(s, kIdx.end, end, COLON);
                if (separatorIdx == -1) {
                    return new INDEX(start, -1);
                }

                nxt = getNextChar(s, separatorIdx, end);
                nxtCh = s.charAt(nxt);
                nxtCh = nxtCh >= ZERO && nxtCh <= NINE ? ZERO : nxtCh;
                vIdx = switch (nxtCh) {

                    case QUOTES -> this.nextStringToken(s, nxt, end);
                    case NULL, TRUE, FALSE -> this.nextValueToken(s, nxt, end, nxtCh);
                    case ZERO, NEGATIVE -> this.nextNumericToken(s, nxt, end);
                    case BEGIN_ARRAY -> this.nextArrayToken(s, nxt, end);
                    case BEGIN_CURLY -> this.nextObjectToken(s, nxt, end);
                    case END_CURLY ->
                            new INDEX(start, nxt - 1); // decrementing the counter so that '}' can be found as next char
                    default -> new INDEX(start, -1);
                };

                if (vIdx.end != -1) {
                    nxt = getNextChar(s, vIdx.end, end);
                    sep = s.charAt(nxt);
                    if (sep == END_CURLY) {
                        return new INDEX(start, nxt);
                    } else if (sep == COMMA && s.charAt(getNextChar(s, nxt, end)) != END_CURLY) {
                        i = nxt;
                    } else {
                        return new INDEX(start, -1);
                    }

                } else {
                    return vIdx;
                }

            }
        }

        return new INDEX(start, -1);
    }

    private int validateSeparator(String s, int start, int end, char separator) {
        int i = getNextChar(s, start, end);
        return s.charAt(i) == separator ? i : -1;

    }

    /**
     * Method to remove the leading and trailing whitespaces from the string
     *
     * @param s the string to trim
     * @return INDEX containing the start and end index of non whitespace
     * character of the string
     */

    private INDEX trim(String s) {

        int length = s.length();
        int i = 0, len = length;
        while (s.charAt(i++) <= 32) {
            if (i == length) {
                return new INDEX(0, 0);
            }
        }

        while (s.charAt(--len) <= 32) {
            if (i == 0) {
                return new INDEX(0, 0);
            }
        }

        return new INDEX(i - 1, len);
    }


    /**
     * Returns the next non whitespace character from the start index
     */
    private int getNextChar(String s, int start, int end) {

        for (int i = start + 1; i <= end; i++) {
            if (s.charAt(i) > 32) {
                return i;
            }
        }

        return end;
    }


    /**
     * Method to extract index position of next string token
     * a value token can be any token in double quotes ""
     *
     * @param s        the string.
     * @param beginIdx the position from where to start scanning for the token
     * @param end      end index of the string s.
     * @return INDEX
     */

    private INDEX nextStringToken(String s, int beginIdx, int end) {

        int i = beginIdx, qEnd = -1;
        while (++i <= end) {
            if (s.charAt(i) == QUOTES) {
                qEnd = i;
                break;
            }
        }

        return new INDEX(beginIdx, qEnd);

    }


    /**
     * Method to extract index position of next value token
     * a value token can be any token from the set {null, true, false}
     *
     * @param str the string.
     * @param nxt the position from where to start scanning for the token
     * @param end end index of the string s.
     * @param sep the first character of the hint that triggered search for value token
     * @return INDEX
     */
    private INDEX nextValueToken(String str, int nxt, int end, char sep) {

        if (sep == NULL && NULL_STR.equalsIgnoreCase(str.substring(nxt, nxt + 4))) {
            return new INDEX(nxt, nxt + 3);
        } else if (sep == TRUE && TRUE_STR.equalsIgnoreCase(str.substring(nxt, nxt + 4))) {
            return new INDEX(nxt, nxt + 3);
        } else if (sep == FALSE && FALSE_STR.equalsIgnoreCase(str.substring(nxt, nxt + 5))) {
            return new INDEX(nxt, nxt + 4);
        }

        return new INDEX(nxt, -1);
    }

    /**
     * Method to extract index position of next numeric token
     * a numeric token can be positive or negative and contains digits
     * 0-9 with any number of repetitions
     *
     * @param str   the string.
     * @param start the position from where to start scanning for the token
     * @param end   end index of the string s.
     * @return INDEX
     */

    private INDEX nextNumericToken(String str, int start, int end) {

        int i = start;
        char ch = str.charAt(++i);
        while (ch >= ZERO && ch <= NINE) {
            if (i == end) {
                return new INDEX(start, i);
            }
            ch = str.charAt(++i);

        }
        // the pointer will have moved one position forward when the condition fails
        return new INDEX(start, i - 1);
    }

    /**
     * Method to extract index position of next array token
     * an object token is contained in square brackets. Eg [ ]
     *
     * @param s     the string.
     * @param start the position from where to start scanning for the token
     * @param end   end index of the string s.
     * @return INDEX
     */
    private INDEX nextArrayToken(String s, int start, int end) {

        INDEX idx;
        int nxt;
        char nxtCh, sep;

        for (int i = start + 1; i <= end; i++) {
            nxtCh = s.charAt(i);
            // escape whitespaces
            if (nxtCh > 32) {


                idx = switch (nxtCh) {

                    case QUOTES -> this.nextStringToken(s, i, end);
                    case NULL, TRUE, FALSE -> this.nextValueToken(s, i, end, nxtCh);
                    case ZERO, NEGATIVE -> this.nextNumericToken(s, i, end);
                    case BEGIN_ARRAY -> this.nextArrayToken(s, i, end);
                    case BEGIN_CURLY -> this.nextObjectToken(s, i, end);
                    case END_ARRAY ->
                            new INDEX(start, i - 1);   // decrement the pointer so that ']' is found as next character
                    default -> new INDEX(start, -1);
                };

                if (idx.end != -1) {
                    nxt = getNextChar(s, idx.end, end);
                    sep = s.charAt(nxt);
                    if (sep == END_ARRAY) {
                        return new INDEX(start, nxt);
                    } else if (sep == COMMA) {
                        i = nxt;
                    } else {
                        return new INDEX(start, -1);
                    }

                } else {
                    return idx;
                }
            }
        }
        return new INDEX(start, -1);

    }


}

