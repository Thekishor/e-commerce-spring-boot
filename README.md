# e-commerce-spring-boot

### Designed and implemented a spring boot microservices architecture with jwt-based auth (access + refresh token), kafka event-driven messaging, and api gateway security
### Implemented role-based access control and email verification using JavaMail
### Containerized redis and kafka using Docker and Docker Compose

## Modules
- Backend: Java 21, Spring Boot, Spring Security
- Microservices: Spring Cloud(Eureka, Gateway, Config, User, Product, Order, Notification)
- Security: JWT, Refresh Token, Role-Based Access Control
- Messaging: Apache Kafka
- Database: Postgres, Redis
- Email: JavaMail (Brevo SMTP)
- Containerization: Docker, Docker Compose
- Build Tool: Maven

### 1. User Service
- localhost:9000/api/user/**
- Handles user registration and authentication
- Implements Jwt Token(access token) and Refresh Token
- Email verification during registration
- Password reset and change flows
- Role-based authorization (ADMIN/USER)
- User login attempts limit with redis 
- JWT access token are short-lived, while refresh token are stored securely and rotated

### 2. Product Service
- localhost:9001/api/product/**
- Category and Product related operation and role based access control
- Spring boot redis for caching like Cacheable, CachePut and CacheEvict

### 3. Order Service
- localhost:9003/api/order/**
- Handle order creation and management
- Communicates with User and Product service via Feign Client
- Publishes order events to kafka for asynchronous processing

### 4. Notification Service
- localhost:9005
- Listens to kafka order events
- Sends email notifications without blocking order flow
- Uses asynchronous processing for scalability 

### 5. Eureka Server
- localhost:8761/eureka
- Service discovery using Eureka

### 6. Config Server
- localhost:9004
- Centralized configuration using spring Cloud Config (native profile)
- All microservices configuration inside config server for access
- config/**

### 7. Api Gateway
- localhost:9002
- Central entry point for all client request
- Validates JWT before routing
- Forwards authorized requests to internal services
- For example:
     http://localhost:9002/api/user/login 
- Load balancing

### 8. Security Design
- JWT authentication flow
- Role-based access
- Password hashing (BCrypt)
- Gateway-level validation

### docker-compose for bitnami kafka image and redis
