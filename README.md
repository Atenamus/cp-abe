# CP-ABE

This project implements Ciphertext-Policy Attribute-Based Encryption (CP-ABE) with a Spring Boot backend and a React frontend .

## About CP-ABE

Ciphertext-Policy Attribute-Based Encryption (CP-ABE) is an encryption scheme that enables fine-grained access control over encrypted data. In CP-ABE:

- Data is encrypted according to an access policy (e.g., "(admin AND finance) AND (manager AND hr)")
- Users receive private keys associated with their attributes (e.g., "admin", "finance", "hr")
- Decryption is only possible if a user's attributes satisfy the encryption policy

## Project Structure

```
cp-abe/
├── backend/         # Spring Boot backend
├── frontend/        # React frontend
```

## Prerequisites

- Java 21
- Node.js 22.14.0
- Docker and Docker Compose
- Maven
- pnpm (for frontend)

## Running the Project

### Backend (Spring Boot)

Navigate to the backend directory and run:

```bash
cd backend
docker compose up -d
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend (React)

Navigate to the frontend directory and run:

```bash
cd frontend
pnpm install
pnpm run dev
```

The frontend will start on `http://localhost:5173`

### Using Docker Compose

To run the entire application stack using Docker:

```bash
# Build and start containers
docker-compose up --build

# Stop containers
docker-compose down
```

The services will be available at:
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`

## API Documentation

The backend provides RESTful endpoints for CP-ABE operations:

- `POST /api/cpabe/setup` - Initialize the CP-ABE system
- `POST /api/cpabe/keygen` - Generate user private keys
- `POST /api/cpabe/encrypt` - Encrypt files using an access policy
- `POST /api/cpabe/decrypt` - Decrypt files using private keys

## Testing

Run backend tests:
```bash
cd backend
mvn test
```
