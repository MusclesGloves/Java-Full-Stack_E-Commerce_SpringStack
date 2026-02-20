# 🛒 SpringStack – Full Stack E-Commerce Application

SpringStack is a production-ready full-stack e-commerce web application built with **Java Spring Boot (Backend)** and **React + Vite (Frontend)**.  
It supports secure JWT-based authentication, role-based authorization (User/Admin), optimized image handling, cart & checkout, and mock/Razorpay-integrated payment flow.  
The application is fully deployable using **Render (Backend)** and **Vercel (Frontend)**.

---

## 🌍 Live Deployed Links

### 🔗 Frontend (Vercel)
https://java-full-stack-e-commerce-spring-stack-cofob8uki.vercel.app

### 🔗 Backend API (Render)
https://java-full-stack-e-commerce-springstack.onrender.com

### 🔗 API Base URL
https://java-full-stack-e-commerce-springstack.onrender.com/api

> ⚠️ Note: Backend is hosted on Render free tier and may take 30–60 seconds to wake up after inactivity.

---

## 🚀 Tech Stack

### Backend
- Java 17
- Spring Boot 3.3
- Spring Security (JWT Authentication)
- Spring Data JPA
- PostgreSQL
- Lombok
- Bean Validation
- Razorpay SDK (Optional)

### Frontend
- React 18 + Vite
- React Router
- Axios (JWT interceptor)
- Context API for global state
- Bootstrap 5
- Lazy-loaded images

### Database
- PostgreSQL
- DDL auto-update (Dev)
- Flyway recommended (Production)

---

## ✨ Features

### 🔑 Authentication
- JWT-based login & registration
- Role-based authorization (USER / ADMIN)
- Stateless backend configuration
- Protected admin endpoints

### 🛍️ Products
- Browse products
- Category filtering
- Product details page
- Admin: Add, update, delete products
- Stock auto-disable when quantity reaches zero
- Optimized image rendering (no BLOB prefetching)

### 🛒 Cart & Checkout
- LocalStorage-backed cart
- Real-time stock validation
- Quantity limit guards
- Persistent cart across sessions

### 💳 Payments
- Mock provider (default)
- Razorpay integration ready
- Order creation after successful payment

### 📊 Orders
- User: My Orders
- Admin: All Orders dashboard

---

## ⚡ Performance Optimizations

- Removed N+1 image blob fetching
- Direct image rendering via `/api/product/{id}/image`
- Lazy loading images (`loading="lazy"`)
- Reduced unnecessary API refetching
- Optimized cart hydration logic
- CORS configuration optimized for Vercel deployment
- Stateless JWT authentication for faster API handling

---

## ⚙️ Installation & Setup

### 1️⃣ Backend (Spring Boot)

cd SpringBoot (BACK-END)
mvn spring-boot:run

Configure `application.properties`:

spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
spring.datasource.username=postgres
spring.datasource.password=your_password

app.jwt.secret=${APP_JWT_SECRET:dev-secret-change-me}
payments.provider=mock

If using environment variable:

export APP_JWT_SECRET=your-production-secret

---

### 2️⃣ Frontend (React + Vite)

cd ReactJS (FRONT-END)/react-ui-for-course-main/ecom-frontend-5-main
npm install
npm run dev

Create `.env` file:

VITE_API_BASE_URL=http://localhost:8080/api

For production:

VITE_API_BASE_URL=https://your-backend.onrender.com/api

---

## 🌍 Production Deployment

### Backend (Render)

Set environment variables:
APP_JWT_SECRET
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
APP_CORS_ALLOWED_ORIGINS=https://*.vercel.app

---

### Frontend (Vercel)

Create `vercel.json`:

{
  "rewrites": [{ "source": "/(.*)", "destination": "/" }]
}

Set environment variable in Vercel:
VITE_API_BASE_URL=https://your-backend.onrender.com/api

---

## 📸 Screenshots

### 🔐 Authentication
(Login & Register screenshots)

### 🏠 Home & Product Catalog
(Product browsing & filtering screenshots)

### 🛒 Cart & Checkout
(Cart screenshot)

### 🛠️ Admin Features
(Admin dashboard screenshots)

### 👤 User Features
(My Orders screenshot)

---

## 📂 Project Structure

E-Commerce (JAVA FULL STACK - FINAL)/
├─ SpringBoot (BACK-END)/
│  ├─ controller/
│  ├─ service/
│  ├─ repository/
│  ├─ model/
│  ├─ security/
│  └─ config/
│
├─ ReactJS (FRONT-END)/
│  └─ react-ui-for-course-main/ecom-frontend-5-main/
│     ├─ components/
│     ├─ Context/
│     ├─ axios.jsx
│     └─ pages/

---

## 🛠️ Roadmap

- [ ] Add pagination & filters for products
- [ ] Disable admin self-registration in production
- [ ] Move product images to S3 / Cloud storage
- [ ] Add email verification & password reset
- [ ] Introduce Flyway migrations
- [ ] Improve order domain modeling

---

## 🤝 Contributing

Pull requests are welcome. Please open an issue first for major changes.

---

## 📜 License

MIT License © 2026