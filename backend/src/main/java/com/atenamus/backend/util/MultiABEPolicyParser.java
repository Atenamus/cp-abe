package com.atenamus.backend.util;

import java.util.*;

/**
 * Multi-Authority CP-ABE Policy Parser
 * 
 * This class parses access policies written in a human-readable Boolean format
 * (e.g., `(attr1@auth1 AND attr2@auth1) OR attr3@auth2`) into a
 * machine-readable
 * Linear Secret Sharing Scheme (LSSS) structure composed of an access matrix
 * (A)
 * and a mapping function (rho).
 * 
 * Implementation is based on the paper:
 * "Efficient Statically-Secure Large-Universe Multi-Authority Attribute-Based
 * Encryption"
 */
public class MultiABEPolicyParser {

    /**
     * Result class to hold the access matrix and rho mapping
     */
    public static class PolicyResult {
        private final List<List<Integer>> accessMatrix;
        private final List<String> rho;

        public PolicyResult(List<List<Integer>> accessMatrix, List<String> rho) {
            this.accessMatrix = new ArrayList<>();
            for (List<Integer> row : accessMatrix) {
                this.accessMatrix.add(new ArrayList<>(row));
            }
            this.rho = new ArrayList<>(rho);
        }

        public List<List<Integer>> getAccessMatrix() {
            return Collections.unmodifiableList(accessMatrix);
        }

        public List<String> getRho() {
            return Collections.unmodifiableList(rho);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Access Matrix (A):\n");

            for (List<Integer> row : accessMatrix) {
                sb.append(row).append("\n");
            }

            sb.append("\nRho Mapping (ρ):\n");
            for (int i = 0; i < rho.size(); i++) {
                sb.append("ρ(").append(i).append(") = ").append(rho.get(i)).append("\n");
            }

            return sb.toString();
        }
    }

    // Parse tree node types
    private enum NodeType {
        ATTRIBUTE, AND, OR
    }

    // Node in the parse tree
    private static class TreeNode {
        NodeType type;
        String value;
        List<TreeNode> children = new ArrayList<>();

        TreeNode(NodeType type, String value) {
            this.type = type;
            this.value = value;
        }

        void addChild(TreeNode child) {
            children.add(child);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(type).append(": ").append(value);
            if (!children.isEmpty()) {
                sb.append(" (");
                for (int i = 0; i < children.size(); i++) {
                    if (i > 0)
                        sb.append(", ");
                    sb.append(children.get(i));
                }
                sb.append(")");
            }
            return sb.toString();
        }
    }

    // Fields for the LSSS matrix and mapping
    private List<List<Integer>> accessMatrix = new ArrayList<>();
    private List<String> rho = new ArrayList<>();

    /**
     * Parse a policy string into an access matrix and rho mapping.
     * 
     * @param policy The policy string (e.g., "(attr1@auth1 AND attr2@auth1) OR
     *               attr3@auth2")
     * @return A PolicyResult containing the access matrix and rho mapping
     * @throws IllegalArgumentException if the policy is invalid
     */
    public PolicyResult parse(String policy) throws IllegalArgumentException {
        if (policy == null || policy.trim().isEmpty()) {
            throw new IllegalArgumentException("Policy cannot be empty");
        }

        // Clear previous results
        accessMatrix.clear();
        rho.clear();

        // Tokenize the policy string
        List<String> tokens = tokenize(policy);

        // Build the parse tree
        TreeNode root = buildParseTree(tokens);

        // Convert to access structure
        convertToAccessStructure(root);

        // Ensure the access matrix is valid
        validateAccessMatrix();

        return new PolicyResult(accessMatrix, rho);
    }

    /**
     * Tokenize the policy string into attributes, operators, and parentheses
     */
    private List<String> tokenize(String policy) throws IllegalArgumentException {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        int length = policy.length();

        while (i < length) {
            char c = policy.charAt(i);

            if (Character.isWhitespace(c)) {
                // Skip whitespace
                i++;
            } else if (c == '(' || c == ')') {
                tokens.add(String.valueOf(c));
                i++;
            } else if (i + 2 < length &&
                    policy.substring(i, i + 3).equalsIgnoreCase("AND") &&
                    (i + 3 == length || !Character.isLetterOrDigit(policy.charAt(i + 3)))) {
                tokens.add("AND");
                i += 3;
            } else if (i + 1 < length &&
                    policy.substring(i, i + 2).equalsIgnoreCase("OR") &&
                    (i + 2 == length || !Character.isLetterOrDigit(policy.charAt(i + 2)))) {
                tokens.add("OR");
                i += 2;
            } else if (Character.isLetterOrDigit(c) || c == '@' || c == '_') {
                // Handle attribute with authority
                int start = i;
                while (i < length && (Character.isLetterOrDigit(policy.charAt(i)) ||
                        policy.charAt(i) == '@' || policy.charAt(i) == '_')) {
                    i++;
                }

                String attr = policy.substring(start, i);
                if (!attr.contains("@")) {
                    throw new IllegalArgumentException(
                            "Attribute must be in the format 'attribute@authority': " + attr);
                }

                tokens.add(attr);
            } else {
                throw new IllegalArgumentException("Invalid character in policy at position " + i + ": " + c);
            }
        }

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("No valid tokens found in policy");
        }

        return tokens;
    }

    /**
     * Build a parse tree from the tokenized policy
     */
    private TreeNode buildParseTree(List<String> tokens) throws IllegalArgumentException {
        int[] index = { 0 }; // Use array to simulate pass-by-reference
        TreeNode root = parseExpression(tokens, index);

        if (index[0] < tokens.size()) {
            throw new IllegalArgumentException(
                    "Unexpected tokens at position " + index[0] + ": " + tokens.get(index[0]));
        }

        return root;
    }

    /**
     * Parse an expression from tokens
     */
    private TreeNode parseExpression(List<String> tokens, int[] index) throws IllegalArgumentException {
        if (index[0] >= tokens.size()) {
            throw new IllegalArgumentException("Unexpected end of policy expression");
        }

        TreeNode left = parsePrimary(tokens, index);

        while (index[0] < tokens.size() &&
                (tokens.get(index[0]).equalsIgnoreCase("AND") || tokens.get(index[0]).equalsIgnoreCase("OR"))) {
            String operator = tokens.get(index[0]).toUpperCase();
            NodeType type = operator.equals("AND") ? NodeType.AND : NodeType.OR;
            index[0]++; // Consume operator

            if (index[0] >= tokens.size()) {
                throw new IllegalArgumentException("Unexpected end of policy after operator: " + operator);
            }

            TreeNode operatorNode = new TreeNode(type, operator);
            operatorNode.addChild(left);

            TreeNode right = parsePrimary(tokens, index);
            operatorNode.addChild(right);

            left = operatorNode;
        }

        return left;
    }

    /**
     * Parse a primary expression (attribute or parenthesized expression)
     */
    private TreeNode parsePrimary(List<String> tokens, int[] index) throws IllegalArgumentException {
        if (index[0] >= tokens.size()) {
            throw new IllegalArgumentException("Unexpected end of policy");
        }

        String token = tokens.get(index[0]);

        if (token.equals("(")) {
            index[0]++; // Consume opening parenthesis

            TreeNode node = parseExpression(tokens, index);

            if (index[0] >= tokens.size() || !tokens.get(index[0]).equals(")")) {
                throw new IllegalArgumentException("Missing closing parenthesis");
            }

            index[0]++; // Consume closing parenthesis
            return node;
        } else if (token.contains("@")) {
            // Attribute node
            if (!isValidAttributeFormat(token)) {
                throw new IllegalArgumentException("Invalid attribute format: " + token);
            }

            TreeNode node = new TreeNode(NodeType.ATTRIBUTE, token);
            index[0]++; // Consume attribute
            return node;
        } else {
            throw new IllegalArgumentException("Unexpected token: " + token);
        }
    }

    /**
     * Validate that an attribute is in the correct format: attr@auth
     */
    private boolean isValidAttributeFormat(String attr) {
        String[] parts = attr.split("@");
        return parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty();
    }

    /**
     * Convert the parse tree to an LSSS access structure
     */
    private void convertToAccessStructure(TreeNode root) {
        // Start with vector [1]
        List<Integer> baseVector = new ArrayList<>();
        baseVector.add(1);

        // Process the tree
        processNode(root, baseVector);

        // Normalize matrix to ensure all rows have the same length
        normalizeMatrix();
    }

    /**
     * Process a node in the parse tree and build the access structure
     */
    private void processNode(TreeNode node, List<Integer> vector) {
        switch (node.type) {
            case ATTRIBUTE:
                // Add the row to the access matrix and the attribute to rho
                accessMatrix.add(new ArrayList<>(vector));
                rho.add(node.value);
                break;

            case AND:
                // For AND gate, create shares that must all be combined
                processAndGate(node, vector);
                break;

            case OR:
                // For OR gate, any child can satisfy the condition
                processOrGate(node, vector);
                break;
        }
    }

    /**
     * Process an AND gate node
     */
    private void processAndGate(TreeNode node, List<Integer> vector) {
        if (node.children.size() < 2) {
            throw new IllegalArgumentException("AND gate must have at least 2 children");
        }

        // Generate shares for AND gate (all shares required to recover the secret)
        List<List<Integer>> shares = generateSharesForAnd(vector, node.children.size());

        // Distribute shares to children
        for (int i = 0; i < node.children.size(); i++) {
            processNode(node.children.get(i), shares.get(i));
        }
    }

    /**
     * Process an OR gate node
     */
    private void processOrGate(TreeNode node, List<Integer> vector) {
        if (node.children.size() < 2) {
            throw new IllegalArgumentException("OR gate must have at least 2 children");
        }

        // For OR gate, all children get the same vector
        for (TreeNode child : node.children) {
            processNode(child, new ArrayList<>(vector));
        }
    }

    /**
     * Generate share vectors for an AND gate according to LSSS scheme
     * For n children, we need n linearly independent vectors that sum to the parent
     * vector
     */
    /**
     * Generate share vectors for an AND gate according to LSSS scheme
     */
    private List<List<Integer>> generateSharesForAnd(List<Integer> vector, int numShares) {
        if (numShares != 2) {
            throw new IllegalArgumentException("AND gate must have exactly 2 children");
        }

        List<List<Integer>> shares = new ArrayList<>();
        // First share: vector + new column with 1
        List<Integer> firstShare = new ArrayList<>(vector);
        firstShare.add(1);
        shares.add(firstShare);

        // Second share: zero vector + new column with -1
        List<Integer> secondShare = new ArrayList<>();
        for (int i = 0; i < vector.size(); i++) {
            secondShare.add(0);
        }
        secondShare.add(-1);
        shares.add(secondShare);

        return shares;
    }

    /**
     * Normalize matrix so all rows have the same number of columns
     */
    private void normalizeMatrix() {
        if (accessMatrix.isEmpty()) {
            return;
        }

        // Find maximum column count
        int maxColumns = 0;
        for (List<Integer> row : accessMatrix) {
            maxColumns = Math.max(maxColumns, row.size());
        }

        // Pad shorter rows with zeros
        for (List<Integer> row : accessMatrix) {
            while (row.size() < maxColumns) {
                row.add(0);
            }
        }
    }

    /**
     * Validate the access matrix and rho mapping
     * - Matrix cannot be empty
     * - All rows must have the same length
     * - Matrix and rho must have the same number of rows
     */
    private void validateAccessMatrix() {
        if (accessMatrix.isEmpty()) {
            throw new IllegalStateException("Access matrix cannot be empty");
        }

        int numColumns = accessMatrix.get(0).size();
        for (List<Integer> row : accessMatrix) {
            if (row.size() != numColumns) {
                throw new IllegalStateException("All rows in the access matrix must have the same number of columns");
            }
        }

        if (accessMatrix.size() != rho.size()) {
            throw new IllegalStateException("Access matrix and rho mapping must have the same length");
        }
    }

    /**
     * Check if a policy contains duplicate attributes
     * Note: In some CP-ABE schemes duplicate attributes are allowed, so this is
     * optional
     * and not part of the main parsing flow
     */
    public boolean containsDuplicateAttributes(String policy) {
        PolicyResult result = parse(policy);
        Set<String> attributes = new HashSet<>(result.getRho());
        return attributes.size() < result.getRho().size();
    }

    public static void main(String[] args) {
        MultiABEPolicyParser parser = new MultiABEPolicyParser();
        String[] policies = {
                "(attr1@auth1 AND attr2@auth1) OR attr3@auth2",
                "(attr1@auth1 AND (attr2@auth2 OR attr3@auth3))",
                "attr1@auth1",
                "(attr1@auth1 AND attr2@auth1"
        };
        for (String policy : policies) {
            try {
                PolicyResult result = parser.parse(policy);
                System.out.println("Policy: " + policy);
                System.out.println(result);
                System.out.println();
            } catch (Exception e) {
                System.err.println("Error parsing '" + policy + "': " + e.getMessage());
            }
        }
    }
}