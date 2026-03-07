# e-commerce-spring-boot

- Designed and implemented a spring boot microservices architecture with jwt-based auth (access + refresh token), kafka event-driven messaging, and api gateway security
- Implemented role-based access control and email verification,order send using JavaMail
- Containerized redis and kafka using Docker and Docker Compose

# Modules
- Backend: Java 21, Spring Boot, Spring Security
- Microservices: Spring Cloud(Eureka, Gateway, Config, User, Product, Order, Notification)
- Security: JWT, Refresh Token, Role-Based Access Control
- Messaging: Apache Kafka
- Database: Postgres, Redis
- Email: JavaMail (Brevo SMTP)
- Containerization: Docker, Docker Compose
- Build Tool: Maven, Swagger OpenAPI

## 1. User Service
- localhost:9000/api/user/**
- User registration and authentication
- Jwt Token(access token) and Refresh Token
- Email verification during registration
- Password reset and change flows
- Role-based authorization (ADMIN/USER)
- User login attempts limit with redis
- Swagger Api for documentation
- Spring Jpa Auditing with Base Entity

## 2. Product Service
- localhost:9001/api/product/**
- Category and Product related operation and role based access control
- Spring boot redis for caching like Cacheable, CachePut and CacheEvict
- Spring boot Jpa Auditing

## 3. Order Service
- localhost:9003/api/order/**
- Handle order creation and management
- Feign Client(User and Product Service)
- Publishes order events to kafka for asynchronous processing
- Spring Jpa Auditing

## 4. Notification Service
- localhost:9005
- Listens to kafka order events
- Sends email notifications
- Uses asynchronous processing for scalability 

## 5. Eureka Server
- localhost:8761/eureka
- Service discovery using Eureka

## 6. Config Server
- localhost:9004
- Centralized configuration using spring Cloud Config (native profile)
- All microservices configuration inside config server for access
- config/**

## 7. Api Gateway
- localhost:9002
- Central entry point for all client request
- Validates JWT before routing
- Forwards authorized requests to internal services
- ** API Examples (Register & login)**:
  ```json
  // User Registration: POST http://localhost:9002/api/user/register
  {
    "username": "Kishor Pandey",
    "email": "kishorpandey981@gmail.com",
    "password": "kishor@@##4426"
  } 
  // User Login: POST http://localhost:9002/api/user/login
  {
    "username": "kishorpandey981@gmail.com",
    "password": "kishor@@##4426"
  }
- Load balancing

## 8. Security Design
- JWT authentication flow
- Role-based access
- Password hashing (BCrypt)
- Gateway-level validation
- Auditing

### docker-compose for bitnami kafka image and redis
