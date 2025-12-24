# e-commerce-spring-boot
This project demonstrates the implementation of real world microservice project using spring boot, security, kafka, java mail, jwt token, authentication and authorization, and inter service communication through feign client to access resources or information from other microservice. 

## Modules

### 1. User Service
- localhost:9000/api/user/**
- Handles user register, login, jwt token, authorization code flow
- Issues Jwt Token
- Password changed, reset password, and verify email using java mail sender

### 2. Product Service
- localhost:9001/api/product/**
- Category and Product related operation and role based access control
- Using Request Header and identify users role then provide access resources

### 3. Order Service
- localhost:9003/api/order/**
- Purchase request, inter service communication
- Feign client interface for user and product service communication with authorization header
- Kafka for Order Event send to notification service or act as a kafka producer with kafka bootstrap server localhost:9092

### 4. Notification Service
- localhost:9005
- Act as a kafka consumer, consume information or event from produce using topic name and then send to consumer mail
- Using brevo mail hostname, credentials

### 5. Eureka Server
- localhost:8761/eureka
- centralized management

### 6. Config Server
- localhost:9004
- All microservices configuration inside config server for access

### 7. Api Gateway
- localhost:9002
- All Service passed thorugh api gateway, validate jwt token and forward request to each microservice according to path matching approach
- For example:
     http://localhost:9002/api/user/login -> for user login and access jwt token
- Load balancing

### docker-compose for bitnami kafka image
