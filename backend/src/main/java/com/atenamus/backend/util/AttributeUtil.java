package com.atenamus.backend.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttributeUtil {
    private static final Set<String> VALID_PREFIXES = new HashSet<>(Arrays.asList("role", "department"));

    public static boolean validateAttribute(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return false;
        }

        String[] parts = attribute.split("_", 2);
        if (parts.length != 2) {
            return false;
        }

        return VALID_PREFIXES.contains(parts[0]);
    }

    public static List<String> validateAttributes(List<String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("Attributes cannot be null or empty");
        }

        for (String attribute : attributes) {
            if (!validateAttribute(attribute)) {
                throw new IllegalArgumentException("Invalid attribute format: " + attribute);
            }
        }

        return attributes;
    }
}