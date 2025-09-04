import { useAuth } from "./auth/AuthContext";
import { Navigate, Outlet, useLocation} from "react-router-dom";

function AuthGate() {
  const { token } = useAuth();
  const loc = useLocation();
  if (!token) {
    if (loc.pathname === "/login" || loc.pathname === "/register") return <Outlet />;
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}

import "./App.css";
import React, { useState, useEffect } from "react";
import Home from "./components/Home";
import Navbar from "./components/Navbar";
import Cart from "./components/Cart";
import AddProduct from "./components/AddProduct";
import Product from "./components/Product";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import DemoCheckout from "./components/DemoCheckout";
import MyOrders from "./components/MyOrders";
import AdminOrders from "./components/AdminOrders";
import Login from "./auth/Login";
import Register from "./auth/Register";
import { AppProvider } from "./Context/Context";
import UpdateProduct from "./components/UpdateProduct";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import 'bootstrap/dist/css/bootstrap.min.css';


function App() {
  const [cart, setCart] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState("");

  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
    console.log("Selected category:", category);
  };
  const addToCart = (product) => {
    const existingProduct = cart.find((item) => item.id === product.id);
    if (existingProduct) {
      setCart(
        cart.map((item) =>
          item.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        )
      );
    } else {
      setCart([...cart, { ...product, quantity: 1 }]);
    }
  };

  return (
    <AppProvider>
        <Navbar onSelectCategory={handleCategorySelect}
         />
        <Routes>
  <Route element={<AuthGate/>}>

          <Route
            path="/"
            element={
              <Home addToCart={addToCart} selectedCategory={selectedCategory}
              />
            }
          />
          <Route path="/product" element={<Product  />} />
          <Route path="product/:id" element={<Product  />} />
          <Route path="/cart" element={<Cart />} />
        

          <Route path="/orders" element={<MyOrders />} />
          <Route element={<AdminGate/>}>
            <Route path="/admin/orders" element={<AdminOrders/>} />
            <Route path="/add_product" element={<AddProduct />} />
            <Route path="/product/update/:id" element={<UpdateProduct />} />
          </Route>

    <Route path="/login" element={<Login/>} />
  <Route path="/register" element={<Register/>} />
  </Route>
  <Route path="/checkout" element={<DemoCheckout/>} />
</Routes>
    </AppProvider>
  );
}

export default App;


function AdminGate(){ 
  const { isAdmin } = useAuth();
  return isAdmin() ? <Outlet/> : <Navigate to="/" replace />;
}
