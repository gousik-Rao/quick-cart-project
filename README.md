# 🛒 QuickCart - E-Commerce Backend API

Welcome to **QuickCart** – a backend system designed for a simple yet functional e-commerce application. This project focuses on authentication, user management, payments, and product operations using **Spring Boot**, **JWT**, **Stripe**, and a **PostgreSQL** database.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white)

---

## 🚀 Features

- ✅ User SignUp / Login with JWT authentication
- 🛡️ Role-based Access (`Admin`, `User`, `Seller`)
- 💳 Stripe Payment Integration
- 📦 Product and Order Management
- 🧾 Swagger-enabled API documentation
- 📬 Email and phone number fields with validation
- 🏝 Lombok

---

## 📁 Tech Stack

| Layer        | Tech Used                    |
|--------------|------------------------------|
| Backend      | Spring Boot (Java)           |
| Security     | Spring Security, JWT         |
| Database     | PostgreSQL                   |
| Payment      | Stripe API                   |
| Docs         | Swagger (OpenAPI 3.0)        |

---

## 📸 API Preview (Swagger UI)

> Swagger docs help you visualize and test API endpoints easily.

http://localhost:8080/swagger-ui/index.html

## 🔐 Authentication Flow
User Sign-Up: /api/auth/signup

User Login: /api/auth/signin
Returns a JWT cookie used for authenticated endpoints.

JWT Filter: Validates token on each request and sets security context.

## 📦 Project Setup

# 1. Clone the repository
git clone https://github.com/<your-username>/quickcart.git
cd quickcart

# 2. Configure application.yml with your DB & Stripe keys

# 3. Run the project using your IDE or CLI
./mvnw spring-boot:run

## 📂 Folder Structure

src/
├── main/
│   ├── java/com/ecommerce/project
│   │   ├── controller
│   │   ├── model
│   │   ├── service
│   │   ├── repository
│   │   └── security
│   └── resources/
│       └── application.yml


### 👤 Author
# Gousik Rao N
Passionate about backend development, DSA, and building scalable systems.
📬 gousikthason@gmail.com

#### ⭐ If you found this project helpful, give it a star and share it with your peers!

---







