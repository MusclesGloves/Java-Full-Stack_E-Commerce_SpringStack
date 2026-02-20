# Demo Script (5 minutes)

1. **Start backend** (Spring Boot on 8080). DB: PostgreSQL `telusko`.
2. **Start frontend** (`npm install && npm run dev` on 5173). App opens at **/login**.
3. Click **Register**, create an **Admin** account → it logs you in and redirects.
4. As Admin, go to Add Product (admin-only), create a product. Show it appears for all users.
5. Logout → Register a **User** account. Login as user.
6. Visit **/checkout**, pay ₹499.
   - If Razorpay keys are configured: Razorpay modal (Test Mode) opens.
   - If not: Mock payment succeeds instantly.
7. (Optional) List your orders with `GET /api/payments/my` in Postman.

## Razorpay (optional)
Add to `application.properties`:
```
payments.provider=razorpay
razorpay.keyId=rzp_test_xxx
razorpay.keySecret=xxxxxxx
```
If keys are missing, the app uses **mock** payments automatically.
