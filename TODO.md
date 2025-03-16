# TODO List

## BUGS

## Refactoring

- [ ] Refactor encryption logic to read data from file and encrypt it.

## High Priority (Core Functionality)

- [ ] Implement Decrypt API endpoint: Create endpoint for decrypting files using private keys.
- [ ] Implement exception handling with meaningful error messages for all CP-ABE operations.
- [ ] Validate all input parameters to prevent security issues and ensure correct CP-ABE operation.
- [ ] Implement file management service to handle storage, retrieval, and cleanup of keys and encrypted files.
- [ ] Create backend integration tests to validate core cryptographic functions.

## Security Features (implement early)

- [ ] Implement JWT authentication for securing API access.
- [ ] Implement secure key storage using encryption or secure storage services.
- [ ] Add structured logging system for all operations, errors, and security events.
- [ ] Create rate limiting for API endpoints to prevent brute force attacks.

## Frontend Implementation

- [ ] Design responsive dashboard layout for key management and encryption operations.
- [ ] Create visualization for attribute possession vs. policy requirements.
- [ ] Design responsive layout for all screen sizes and devices.
- [ ] Add help tooltips for complex CP-ABE concepts and operations.
- [ ] Create UI component tests to ensure frontend reliability.

## Backend Enhancements

- [ ] Implement caching mechanisms for frequently accessed keys and parameters.
- [ ] Add support for file versioning to track changes to encrypted documents.
- [ ] Implement attribute revocation mechanism to invalidate compromised attributes.
- [ ] Create administrative interface for managing system-wide attributes.
- [ ] Add batch operations for encrypting/decrypting multiple files.
- [ ] Implement key rotation functionality for regular security updates.
- [ ] Create API endpoint for querying available attributes in the system.

## Performance Optimization

- [ ] Profile and optimize core CP-ABE operations for better throughput.
- [ ] Add multi-threading support for parallel encryption/decryption operations.
- [ ] Optimize database queries for attribute and policy storage.
- [ ] Implement lazy loading for large key structures.
- [ ] Create background processing for long-running encryption tasks.
- [ ] Add compression options for encrypted files to reduce storage needs.
- [ ] Implement performance testing suite to identify bottlenecks.

## Documentation

- [ ] Create Swagger/OpenAPI documentation for all API endpoints.
- [ ] Write user guide explaining CP-ABE concepts and system usage.
- [ ] Create technical documentation covering system architecture and data flows.
- [ ] Document all supported attribute types and policy structures.
- [ ] Create examples of common access policy patterns.
- [ ] Add diagrams explaining the cryptographic workflow.
- [ ] Document deployment instructions for different environments.
- [ ] Create troubleshooting guide for common issues.
- [ ] Update README with setup instructions and quick start guide.

## Change Log

- [x] 16/03/2025 (Tushar Agrawal): Created basic onboarding flow ui
- [x] 16/03/2025 (Tushar Agrawal): Created landing and authentication pages for the website.
- [x] 14/03/2025 (R Lalith): Implemented key management UI to view, download, and manage public/master/private keys.
- [x] 14/03/2025 (R Lalith): Created interactive policy builder with AND operators and attribute selection.
- [x] 14/03/2025 (R Lalith): Created encryption form for uploading files and specifying attribute-based access policies.
- [x] 14/03/2025 (R Lalith): Implemented drag-and-drop file upload with progress indicators.
- [x] 14/03/2025 (R Lalith): Build file browser for viewing and managing encrypted documents.
- [x] 14/03/2025 (R Lalith): Added user-friendly error messages for all cryptographic operations.
- [x] 14/03/2025 (R Lalith): Implemented toast notification system for operation success/failure.
- [x] 10/03/2025 (Tushar Agrawal): Added a utility class to convert policies into required postfix notation.
- [x] 08/03/2025 (Asif Ali Khan): Implemented Encrypt API endpoint
- [x] 16/02/2025 (Tushar Agrawal): Created KeyGen API for generating attribute-based private keys.
- [x] 15/02/2025 (Tushar Agrawal): Implemented Setup API for generating system parameters or public key and master key.
