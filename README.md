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

### Verify Account
<br>
<img width="1419" height="613" alt="Screenshot 2025-12-24 223942" src="https://github.com/user-attachments/assets/5bb26021-eb22-454e-b3ad-f3ab77e3f60c" />

### Reset Password
<br>
<img width="1824" height="868" alt="Screenshot 2025-12-24 224003" src="https://github.com/user-attachments/assets/e9a648fe-0805-485c-8540-4ecd40b2dc7b" />


### Order Details
<br>
<img width="1808" height="899" alt="Screenshot 2025-12-24 224029" src="https://github.com/user-attachments/assets/07103637-556a-49d1-bccd-fe4f338e9e50" />

### User register
<br>
<img width="1900" height="1077" alt="Screenshot 2025-12-24 224227" src="https://github.com/user-attachments/assets/c116155b-faa5-49b8-8daa-a2d9ae94f6c6" />

### User login
<br>
<img width="1914" height="1079" alt="Screenshot 2025-12-24 224244" src="https://github.com/user-attachments/assets/91e6f6f4-d305-45c8-85f4-4bb5ba017ad9" />

### Get User By Id
<br>
<img width="1909" height="1068" alt="Screenshot 2025-12-24 224326" src="https://github.com/user-attachments/assets/1479069e-86a0-485f-9e6c-4001e0cbb575" />

### Changed Password
<br>
<img width="1919" height="1021" alt="Screenshot 2025-12-24 224440" src="https://github.com/user-attachments/assets/45dfaeff-0fcf-421e-8d5d-6aa62bbf68c9" />

### Create Order
<br>
<img width="1919" height="753" alt="Screenshot 2025-12-24 224619" src="https://github.com/user-attachments/assets/5b568ec0-1c57-4203-be08-5a0560a1d277" />
