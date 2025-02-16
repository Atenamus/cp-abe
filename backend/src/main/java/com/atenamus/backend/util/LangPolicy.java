package com.atenamus.backend.util;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Comparator;

/**
 * Utility class for attributes in a language policy.
 */
public class LangPolicy {

    /**
     * Parses a string of attributes and returns an array of sorted attributes.
     * Each attribute must contain a ":" to be considered valid.
     *
     * @param input The string containing space-separated attributes.
     * @return A sorted array of valid attributes.
     * @throws IllegalArgumentException if an invalid attribute is encountered.
     */
    public static String[] parseAttribute(String input) {
        List<String> attributes = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(input);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.contains(":")) {
                attributes.add(token);
            } else {
                throw new IllegalArgumentException("Invalid attribute: " + token);
            }
        }

        // Sort the attributes alphabetically using a custom comparator
        attributes.sort(new AlphabeticComparator());

        return attributes.toArray(new String[0]);
    }

    /**
     * Comparator for sorting strings alphabetically.
     */
    static class AlphabeticComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }
}
