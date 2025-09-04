# 🛒 SpringStack – Full Stack E-Commerce Application

SpringStack is a full-stack e-commerce web application built with **Java Spring Boot (Backend)** and **React (Frontend)**.  
It supports secure JWT-based authentication, role-based authorization (User/Admin), product management with images, cart & checkout, and a mock/Razorpay-integrated payment flow.

---

## 🚀 Tech Stack

**Backend**  
- Java 17, Spring Boot 3.3  
- Spring Security (JWT Authentication)  
- Spring Data JPA (PostgreSQL)  
- Validation, Lombok  
- Razorpay SDK (payments)

**Frontend**  
- React + Vite  
- React Router  
- Axios (with JWT interceptor)  
- Context API for global state  
- Bootstrap for UI

**Database**  
- PostgreSQL (DDL auto-update for dev, Flyway recommended for prod)

---

## ✨ Features

- 🔑 **Authentication**: JWT-based login/register, role management  
- 🛍️ **Products**:
  - Browse products, search, view details
  - Admin: Add, update, delete products with images  
  - Stock management (auto-disable on 0 stock)  
- 🛒 **Cart & Checkout**:
  - LocalStorage-backed cart
  - Real-time stock validation
- 💳 **Payments**:
  - Mock provider (default)  
  - Razorpay test/live integration ready  
- 📊 **Orders**:
  - User: My Orders  
  - Admin: All Orders dashboard  

---

## ⚙️ Installation & Setup

### 1️⃣ Backend (Spring Boot)
```bash
cd SpringBoot (BACK-END)
mvn spring-boot:run
```

Configure `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/telusko
spring.datasource.username=postgres
spring.datasource.password=Tiger

jwt.secret=your-secret-key
payments.provider=mock  # or razorpay
```

### 2️⃣ Frontend (React + Vite)
```bash
cd ReactJS (FRONT-END)/react-ui-for-course-main/ecom-frontend-5-main
npm install
npm run dev
```

---

## 📸 Screenshots

### 🔐 Authentication
- **Login**  
  <img width="1919" height="997" alt="LoginPage" src="https://github.com/user-attachments/assets/24d19381-7801-4886-bd0b-3357c8c7f0a8" />

- **Register**  
  <img width="1919" height="996" alt="RegistrationPage" src="https://github.com/user-attachments/assets/e12ebf79-e806-4e0d-be34-b8d886af543a" />


### 🏠 Home & Product Catalog
- **Browse Products**  
  <img width="1919" height="994" alt="UserHomePage" src="https://github.com/user-attachments/assets/e6ec38de-603f-48ab-8647-6e7900e1072b" />


- **Category Filter**  
  <img width="1919" height="994" alt="AvailableCategories" src="https://github.com/user-attachments/assets/fc59606f-eb1e-484d-a718-9a217665900e" />


### 📦 Cart & Checkout
- **Cart View**  
  <img width="1919" height="1001" alt="AddToCartPage" src="https://github.com/user-attachments/assets/b764ce88-3466-4ecc-9c8a-7042e9cdd6ce" />


### 🛠️ Admin Features
- **Add Product**  
  <img width="1919" height="996" alt="AddingProductPage" src="https://github.com/user-attachments/assets/cdf38981-2554-4561-a041-7b9093e465e1" />


- **Product Details (Admin Actions)**  
  <img width="1919" height="995" alt="AdminProductPage" src="https://github.com/user-attachments/assets/907678d5-7085-44ec-b8c7-1fca295e7a6a" />

- **Admin View of Products**  
  <img width="1919" height="998" alt="AdminPage" src="https://github.com/user-attachments/assets/feadfc95-7834-4932-8216-cd4e4ce8aa87" />


### 👤 User Features
- **Product Details (User)**  
  <img width="1919" height="997" alt="UserProductPage" src="https://github.com/user-attachments/assets/d1763b9f-1d9b-4e12-b8f2-8b62e0e7cea3" />

- **My Orders**  
  <img width="1919" height="987" alt="UserPurchasedOrders(Receipt)" src="https://github.com/user-attachments/assets/78db34b4-f475-4484-ada9-a3b268a26b10" />

---

## 📂 Project Structure

```
E-Commerce (JAVA FULL STACK - FINAL)/
├─ SpringBoot (BACK-END)/
│  ├─ src/main/java/com/telusko/springecom/...
│  └─ src/main/resources/
├─ ReactJS (FRONT-END)/
│  └─ react-ui-for-course-main/ecom-frontend-5-main/
```

---

## 🛠️ Roadmap

- [ ] Add pagination & filters for products  
- [ ] Secure admin creation (disable “Register as Admin” in prod)  
- [ ] Move product images to S3/MinIO (instead of DB BLOBs)  
- [ ] Add email verification & password reset  
- [ ] Improve order domain (Order + OrderItems)  

---

## 🤝 Contributing

Pull requests are welcome! Please open an issue first for major changes.

---

## 📜 License

MIT License © 2025  
