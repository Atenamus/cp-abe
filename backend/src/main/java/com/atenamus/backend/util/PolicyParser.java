package com.atenamus.backend.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyParser {

    /**
     * Converts a user-friendly infix policy expression to postfix notation
     * Supported operators: and, or, m of n
     * Example: "(A and B) or C" -> "A B 2of2 C 1of2"
     */
    public static String convertInfixToPostfix(String infixPolicy) {
        // First, preprocess attribute comparisons to convert them to single tokens
        String preprocessed = preprocessAttributeComparisons(infixPolicy);

        // Then preprocess M of N expressions
        preprocessed = preprocessMofN(preprocessed);

        System.out.println("After preprocessing: " + preprocessed);

        // Tokenize the input, handling parentheses and operators
        ArrayList<String> tokens = tokenizeInfixPolicy(preprocessed);

        // Use the Shunting Yard algorithm to convert infix to postfix
        ArrayList<String> postfixTokens = shuntingYard(tokens);

        // Join the tokens with spaces to create the final postfix policy string
        return String.join(" ", postfixTokens);
    }

    /**
     * Preprocess attribute comparisons (e.g., "department = IT" -> "department_IT")
     */
    private static String preprocessAttributeComparisons(String infixPolicy) {
        // Handle various comparison patterns

        // Pattern for equality: attr = value
        Pattern eqPattern = Pattern.compile("(\\w+)\\s*=\\s*(\\w+)");
        Matcher eqMatcher = eqPattern.matcher(infixPolicy);
        StringBuffer result = new StringBuffer();

        while (eqMatcher.find()) {
            String attribute = eqMatcher.group(1);
            String value = eqMatcher.group(2);
            eqMatcher.appendReplacement(result, attribute + "_" + value);
        }
        eqMatcher.appendTail(result);

        String processed = result.toString();

        // Pattern for greater than or equal: attr >= value
        Pattern gePattern = Pattern.compile("(\\w+)\\s*>=\\s*(\\w+)");
        Matcher geMatcher = gePattern.matcher(processed);
        result = new StringBuffer();

        while (geMatcher.find()) {
            String attribute = geMatcher.group(1);
            String value = geMatcher.group(2);
            geMatcher.appendReplacement(result, attribute + "_ge_" + value);
        }
        geMatcher.appendTail(result);

        processed = result.toString();

        // Pattern for greater than: attr > value
        Pattern gtPattern = Pattern.compile("(\\w+)\\s*>\\s*(\\w+)");
        Matcher gtMatcher = gtPattern.matcher(processed);
        result = new StringBuffer();

        while (gtMatcher.find()) {
            String attribute = gtMatcher.group(1);
            String value = gtMatcher.group(2);
            gtMatcher.appendReplacement(result, attribute + "_gt_" + value);
        }
        gtMatcher.appendTail(result);

        processed = result.toString();

        // Pattern for less than or equal: attr <= value
        Pattern lePattern = Pattern.compile("(\\w+)\\s*<=\\s*(\\w+)");
        Matcher leMatcher = lePattern.matcher(processed);
        result = new StringBuffer();

        while (leMatcher.find()) {
            String attribute = leMatcher.group(1);
            String value = leMatcher.group(2);
            leMatcher.appendReplacement(result, attribute + "_le_" + value);
        }
        leMatcher.appendTail(result);

        processed = result.toString();

        // Pattern for less than: attr < value
        Pattern ltPattern = Pattern.compile("(\\w+)\\s*<\\s*(\\w+)");
        Matcher ltMatcher = ltPattern.matcher(processed);
        result = new StringBuffer();

        while (ltMatcher.find()) {
            String attribute = ltMatcher.group(1);
            String value = ltMatcher.group(2);
            ltMatcher.appendReplacement(result, attribute + "_lt_" + value);
        }
        ltMatcher.appendTail(result);

        return result.toString();
    }

    /**
     * Tokenizes an infix policy expression
     */
    private static ArrayList<String> tokenizeInfixPolicy(String infixPolicy) {
        ArrayList<String> tokens = new ArrayList<>();
        // Replace "and", "or", "(", ")" with spaces around them for easier tokenization
        String prepared = infixPolicy
                .replaceAll("\\(", " ( ")
                .replaceAll("\\)", " ) ")
                .replaceAll("\\band\\b", " and ")
                .replaceAll("\\bor\\b", " or ")
                .replaceAll("\\bof\\b", " of ")
                .replaceAll("\\bnot\\b", " not ");

        // Split by spaces and filter out empty tokens
        for (String token : prepared.split("\\s+")) {
            if (!token.trim().isEmpty()) {
                tokens.add(token.trim());
            }
        }

        return tokens;
    }

    /**
     * Implementation of the Shunting Yard algorithm for converting infix to postfix
     */
    private static ArrayList<String> shuntingYard(ArrayList<String> tokens) {
        ArrayList<String> output = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();

        // Define operator precedence (higher number = higher precedence)
        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("not", 3); // NOT has highest precedence
        precedence.put("and", 2);
        precedence.put("or", 1);

        for (String token : tokens) {
            if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                // Pop operators until we find the matching opening parenthesis
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    processOperator(operatorStack.pop(), output);
                }

                // Remove the opening parenthesis
                if (!operatorStack.isEmpty() && operatorStack.peek().equals("(")) {
                    operatorStack.pop();
                }
            } else if (token.equals("and") || token.equals("or") || token.equals("not")) {
                // Process operators according to precedence
                while (!operatorStack.isEmpty() &&
                        !operatorStack.peek().equals("(") &&
                        precedence.getOrDefault(operatorStack.peek(), 0) >= precedence.get(token)) {
                    processOperator(operatorStack.pop(), output);
                }
                operatorStack.push(token);
            } else if (token.matches("\\d+of\\d+")) {
                // Handle "k of n" operators directly
                // Note: This is for thresholds that are already in correct form
                output.add(token);
            } else {
                // Token is an attribute - add directly to output
                output.add(token);
            }
        }

        // Pop any remaining operators from the stack
        while (!operatorStack.isEmpty()) {
            processOperator(operatorStack.pop(), output);
        }

        return output;
    }

    /**
     * Process operators and convert them to the "k of n" format
     */
    private static void processOperator(String operator, ArrayList<String> output) {
        if (operator.matches("\\d+of\\d+")) {
            // Direct "k of n" operator - add as is
            output.add(operator);
        } else if (operator.equals("and")) {
            // "and" becomes "2of2"
            output.add("2of2");
        } else if (operator.equals("or")) {
            // "or" becomes "1of2"
            output.add("1of2");
        } else if (operator.equals("not")) {
            // "not" becomes a unary operator - in CPABE this is often handled specially
            output.add("0of1");
        }
    }

    /**
     * Preprocess a policy expression to handle "m of n" expressions
     * Example: "2 of (A, B, C)" -> convert to equivalent expression using AND/OR
     */
    public static String preprocessMofN(String infixPolicy) {
        // Regular expression to match m of (A, B, C, ...) patterns
        Pattern pattern = Pattern.compile("(\\d+)\\s+of\\s+\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(infixPolicy);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            int k = Integer.parseInt(matcher.group(1));
            String itemsList = matcher.group(2);
            String[] items = itemsList.split(",");

            // Trim whitespace from each item
            for (int i = 0; i < items.length; i++) {
                items[i] = items[i].trim();
            }

            // Create a special placeholder token that will be handled specially
            // Format: items[0] items[1] ... items[n-1] kofn
            StringBuilder replacement = new StringBuilder();
            for (int i = 0; i < items.length; i++) {
                if (i > 0) {
                    replacement.append(" ");
                }
                replacement.append(items[i]);
            }
            replacement.append(" ").append(k).append("of").append(items.length);

            matcher.appendReplacement(result, replacement.toString());
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
