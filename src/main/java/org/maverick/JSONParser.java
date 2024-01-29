package org.maverick;

/**
 * An interface to manipulate JSON string.
 */
public interface JSONParser {

    /**
     * Method to validate if a string has a valid JSON structure
     * @param s the input string to br tested
     * @return true if the JSON structure is valid; false otherwise
     */
    public boolean validJSON(String s);
}
