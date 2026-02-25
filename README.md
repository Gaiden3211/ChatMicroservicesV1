# DiscordAnalog: Microservices-based Real-time Messenger

## Overview
DiscordAnalog is a distributed real-time messaging platform built using a microservices architecture. 

The system supports real-time messaging, voice/video calls, end-to-end encrypted private chats, and horizontal scaling across multiple chat service instances.

## Tech Stack
* **Backend:** Java 21, Spring Boot, Spring Cloud.
* **Communication:** WebSockets (Real-time), gRPC (Inter-service), WebRTC (Voice/Video).
* **Messaging & Async:** Apache Kafka, Redis.
* **Database:** PostgreSQL, AWS S3 (Storage).
* **Security:** JWT, OAuth2, E2EE (End-to-End Encryption).
* **Infrastructure:** Docker, AWS EC2, GitHub Actions (CI/CD).
* **AI:** Spring AI.

## Key Features
* **Microservices Architecture:** Distributed system with a clear separation of concerns (Auth, Chat, Guild, Notification).
* **Real-time Interaction:** Instant messaging via WebSockets.
* **Security First:** End-to-End Encryption (E2EE) is implemented, where keys are stored exclusively on the client side (IndexedDB).
* **Media & Calls:** Support for audio/video calls via WebRTC and file uploads to AWS S3.
* **Event-Driven:** Utilizing Apache Kafka for event processing and notifications.

## System Architecture & Data Flow
The project is built on a microservices architecture. All external requests pass through the API Gateway, which acts as a single entry point. Services have their own isolated databases (PostgreSQL) and communicate with each other using gRPC (for fast synchronous calls) and Apache Kafka (for asynchronous events).

### User Registration Flow
Below is a diagram of the new user creation process, demonstrating how the infrastructure works:

<img width="728" height="735" alt="image" src="https://github.com/user-attachments/assets/bba3d6f5-d8eb-4802-b7f5-ad581a55a453" />

### Send Message Flow
The project implements E2EE. Before sending, the client encrypts the message with the recipient's public key and only then sends it. The recipient then decrypts the message using their private key.

<img width="751" height="736" alt="image" src="https://github.com/user-attachments/assets/2e59c27e-be85-4dbf-9c9a-86af69474e91" />

### Redis Usage
Redis is used not only for caching but also as a Pub/Sub mechanism to synchronize multiple Chat Service instances. 

Since WebSocket connections are stateful and bound to specific instances, Redis enables real-time message propagation across horizontally scaled nodes.

## 📸 UI / Screenshots

Here is a glimpse of the DiscordAnalog client interface in action:

<p align="center">
  <img width="1913" height="929" alt="image" src="https://github.com/user-attachments/assets/8639fd22-c648-4c64-954b-284b022191e8" />
</p>

* **Left:** List of active chats (via WebSockets).
* **Right:** Real-time messaging with End-to-End Encryption enabled.








## Getting Started (Local Development)
Since the project uses a microservices infrastructure (databases, message brokers), the most convenient way to run it locally is using Docker.

### Prerequisites
* **Docker** and **Docker Compose**
* **Java 21** (if you plan to build and run the services manually)

### Configuration (Environment Variables)
For all integrations (Redis, OAuth2, AI, AWS) to work correctly, the project requires environment variables. Create a `.env` file in the root directory of the project and add your keys:

```env
# Google Secrets
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

BASE_URL=https:
AUTH_REDIRECT_URI=${BASE_URL}/login/oauth2/code/google
JWT_SECRET=
REDIS_PASSWORD=
POSTGRES_DB_NEON_PASSWORD=
POSTGRES_DB_NEON_USER_DB=
POSTGRES_DB_NEON_AUTH_DB=
POSTGRES_DB_NEON_GUILD_DB=
POSTGRES_DB_NEON_CHAT_DB=

AWS_S3_BUCKET=

