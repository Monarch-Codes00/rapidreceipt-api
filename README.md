# RapidReceipt API

> **Spring Boot 3 · Java 17 · PostgreSQL · JWT**

Backend REST API for the RapidReceipt platform — invoice generation, customer management, subscription tracking, and third-party service integrations.

---

## Tech Stack

| Layer        | Technology                     |
|--------------|--------------------------------|
| Language     | Java 17                        |
| Framework    | Spring Boot 3.2                |
| Security     | Spring Security + JWT (jjwt)   |
| Persistence  | Spring Data JPA + Hibernate    |
| Database     | PostgreSQL                     |
| Build Tool   | Maven                          |
| Utilities    | Lombok, Bean Validation        |

---

## Project Structure

```
src/main/java/com/rapidreceipt/
├── RapidReceiptApplication.java   ← Entry point
├── config/                        ← Security, CORS, app config beans
├── auth/                          ← Authentication (login, register, JWT)
├── user/                          ← User domain (profile, roles)
├── customer/                      ← Customer management
├── service/                       ← Business service layer (domain services)
├── invoice/                       ← Invoice creation & management
├── subscription/                  ← Subscription & plan management
├── sync/                          ← Third-party sync integrations
└── common/                        ← Shared DTOs, exceptions, utilities
```

---

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 14+

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-org/rapidreceipt-api.git
cd rapidreceipt-api
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Edit `.env` with your local values (see table below).

### 3. Create the database

```sql
CREATE DATABASE rapidreceipt;
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The server starts at **http://localhost:8080**.

---

## Environment Variables

| Variable              | Description                          | Default                                      |
|-----------------------|--------------------------------------|----------------------------------------------|
| `DB_URL`              | PostgreSQL JDBC URL                  | `jdbc:postgresql://localhost:5432/rapidreceipt` |
| `DB_USERNAME`         | Database username                    | `postgres`                                   |
| `DB_PASSWORD`         | Database password                    | `postgres`                                   |
| `JWT_SECRET`          | HS256 signing secret (min 256-bit)   | `devsecretkey` *(change in production!)*     |
| `PAYSTACK_SECRET_KEY` | Paystack API secret key              | —                                            |

> ⚠️ **Never commit your `.env` file.** It is listed in `.gitignore`.

---

## Useful Commands

```bash
# Compile only
mvn clean compile

# Run tests
mvn test

# Package as JAR
mvn clean package -DskipTests

# Run packaged JAR
java -jar target/rapidreceipt-api-0.0.1-SNAPSHOT.jar
```

---

## Contributing

1. Branch from `main` using the convention `feature/<ticket-id>-short-description`.
2. Open a pull request against `main` with a clear description.
3. All PRs require at least one review before merging.

---

## License

Proprietary — © RapidReceipt. All rights reserved.
