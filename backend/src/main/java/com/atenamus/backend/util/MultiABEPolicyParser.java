package com.atenamus.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MultiABEPolicyParser {
    private static final Logger logger = LoggerFactory.getLogger(MultiABEPolicyParser.class);

    public static class AccessStructure {
        public final List<List<Integer>> matrix;
        public final List<String> rho;

        public AccessStructure(List<List<Integer>> matrix, List<String> rho) {
            this.matrix = matrix;
            this.rho = rho;
        }
    }

    private static class Node {
        String value; // "AND", "OR", or attribute
        List<Node> children;

        Node(String value) {
            this.value = value;
            this.children = new ArrayList<>();
        }

        void addChild(Node child) {
            children.add(child);
        }
    }

    public static AccessStructure parsePolicy(String policy) {
        if (policy == null || policy.trim().isEmpty()) {
            throw new IllegalArgumentException("Policy cannot be null or empty");
        }

        // Step 1: Parse policy into a tree
        Node root = buildTree(policy);
        if (root == null) {
            throw new IllegalArgumentException("Failed to parse policy: " + policy);
        }

        // Step 2: Convert tree to LSSS
        List<List<Integer>> matrix = new ArrayList<>();
        List<String> rho = new ArrayList<>();
        int[] vectorSize = { 1 }; // Tracks required columns, starts with 1 for s
        buildLSSS(root, matrix, rho, vectorSize);

        // Step 3: Normalize matrix to uniform column size
        int maxColumns = vectorSize[0];
        for (List<Integer> row : matrix) {
            while (row.size() < maxColumns) {
                row.add(0);
            }
        }

        AccessStructure result = new AccessStructure(matrix, rho);
        logger.info("Parsed policy '{}' into matrix: {}, rho: {}", policy, result.matrix, result.rho);
        return result;
    }

    private static Node buildTree(String policy) {
        String[] tokens = policy.replace("(", " ( ").replace(")", " ) ").split("\\s+");
        Stack<Node> stack = new Stack<>();
        Node current = null;

        for (String token : tokens) {
            if (token.isEmpty())
                continue;

            if (token.equals("(")) {
                if (current != null) {
                    stack.push(current);
                }
                current = null;
            } else if (token.equals(")")) {
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Mismatched parentheses in policy: " + policy);
                }
                Node child = current;
                current = stack.pop();
                if (child != null) {
                    current.addChild(child);
                }
            } else if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
                if (current != null) {
                    throw new IllegalArgumentException("Unexpected operator " + token + " in policy: " + policy);
                }
                current = new Node(token.toUpperCase());
            } else {
                Node attributeNode = new Node(token);
                if (current == null) {
                    current = attributeNode;
                } else if (current.value.equals("AND") || current.value.equals("OR")) {
                    current.addChild(attributeNode);
                } else {
                    throw new IllegalArgumentException("Invalid policy structure near " + token + ": " + policy);
                }
            }
        }

        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("Unclosed parentheses in policy: " + policy);
        }
        return current;
    }

    private static void buildLSSS(Node node, List<List<Integer>> matrix, List<String> rho, int[] vectorSize) {
        if (node.value.equals("AND")) {
            if (node.children.size() != 2) {
                throw new IllegalArgumentException("AND node must have exactly 2 children");
            }
            Node left = node.children.get(0);
            Node right = node.children.get(1);

            List<List<Integer>> leftMatrix = new ArrayList<>();
            List<String> leftRho = new ArrayList<>();
            buildLSSS(left, leftMatrix, leftRho, vectorSize);

            List<List<Integer>> rightMatrix = new ArrayList<>();
            List<String> rightRho = new ArrayList<>();
            buildLSSS(right, rightMatrix, rightRho, vectorSize);

            int n = vectorSize[0];
            vectorSize[0] = n + 1; // Increment for new share
            int newColumn = n;

            // Left gets partial share (1 in new column)
            for (List<Integer> row : leftMatrix) {
                while (row.size() < n)
                    row.add(0);
                row.add(1);
                matrix.add(row);
            }
            rho.addAll(leftRho);

            // Right gets complementary share (-1 in new column)
            for (List<Integer> row : rightMatrix) {
                while (row.size() < n)
                    row.add(0);
                row.add(-1);
                matrix.add(row);
            }
            rho.addAll(rightRho);
        } else if (node.value.equals("OR")) {
            for (Node child : node.children) {
                buildLSSS(child, matrix, rho, vectorSize);
            }
        } else {
            // Leaf node (attribute)
            List<Integer> row = new ArrayList<>();
            row.add(1); // Share of s
            for (int i = 1; i < vectorSize[0]; i++) {
                row.add(0);
            }
            matrix.add(row);
            rho.add(node.value);
        }
    }

    private static int precedence(String operator) {
        return operator.equalsIgnoreCase("AND") ? 2 : 1;
    }

    // For testing
    public static void main(String[] args) {
        String policy = "(attr1@auth1 AND attr2@auth1) OR attr3@auth2";
        AccessStructure as = parsePolicy(policy);
        System.out.println("Matrix: " + as.matrix);
        System.out.println("Rho: " + as.rho);
    }
}