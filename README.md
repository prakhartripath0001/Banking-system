# Banking System

A microservices-based banking system architecture.

## Services Overview

- **Account Service:** Manages user accounts, balances, and status (block, deduct, etc.).
- **Transaction Service:** Handles money transfers and payment transactions.
- **Fraud Detection Service:** Monitors and blocks suspicious activities.
- **Notification Service:** Sends alerts and updates to users.
- **API Gateway:** Routes external requests to the appropriate internal services.

## Architecture & Patterns

The system utilizes several architectural patterns common in modern distributed systems, including:
- **Microservices Architecture:** Services are independently deployable and decoupled.
- **Saga Pattern:** To maintain data consistency across distributed transactions without relying on a single distributed database lock. (See `docs/saga-pattern.md` for details).

## License

If you copy or use this code in any way, you must provide clear credit and attribution to the original author.
