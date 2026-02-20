import React, { useContext, useState, useEffect, useMemo } from "react";
import AppContext from "../Context/Context";
import API from "../axios";
import CheckoutPopup from "./CheckoutPopup";
import { Button } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

const Cart = () => {
  const { cart, removeFromCart, clearCart } = useContext(AppContext);
  const [cartItems, setCartItems] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const navigate = useNavigate();

  // compute total from cartItems (fallback to cart until hydrated)
  const totalPrice = useMemo(() => {
    const src = cartItems?.length ? cartItems : (cart || []);
    return src.reduce((acc, item) => acc + (item.price || 0) * (item.quantity || 1), 0);
  }, [cartItems, cart]);

  // hydrate images for items that still exist in backend
  useEffect(() => {
    const run = async () => {
      try {
        const res = await API.get("/products");
        const backendIds = new Set((res.data || []).map(p => p.id));
        const valid = (cart || []).filter(it => backendIds.has(it.id));

        const withImages = await Promise.all(
          valid.map(async (item) => {
            try {
              const img = await API.get(`/product/${item.id}/image`, { responseType: "blob" });
              const url = URL.createObjectURL(img.data);
              return { ...item, imageUrl: url };
            } catch {
              return { ...item, imageUrl: "" };
            }
          })
        );
        setCartItems(withImages);
      } catch (e) {
        console.error("Error fetching product data:", e);
        setCartItems(cart || []);
      }
    };
    if (cart?.length) run(); else setCartItems([]);
  }, [cart]);

  const handleIncreaseQuantity = (itemId) => {
    setCartItems(items =>
      items.map(it =>
        it.id === itemId
          ? { ...it, quantity: Math.min((it.quantity || 1) + 1, it.stockQuantity ?? Infinity) }
          : it
      )
    );
  };

  const handleDecreaseQuantity = (itemId) => {
    setCartItems(items =>
      items.map(it =>
        it.id === itemId ? { ...it, quantity: Math.max((it.quantity || 1) - 1, 1) } : it
      )
    );
  };

  const handleRemoveFromCart = (itemId) => {
    removeFromCart(itemId);
    setCartItems(items => items.filter(it => it.id !== itemId));
  };

  // NEW: send line items + amount; backend will record payment + decrement stock
  const handleCheckout = async () => {
    try {
      const items = (cartItems || []).map(it => ({
        productId: it.id,
        quantity: it.quantity || 1
      }));

      const amount = Math.round(totalPrice); // rupees
      if (amount <= 0 || items.length === 0) {
        alert("Cart is empty or total is invalid.");
        return;
      }

      const res = await API.post("/payments/checkout", { amount, items });

      if (res.data?.status === "PAID") {
        alert("Payment successful!");
        clearCart();
        setCartItems([]);
        setShowModal(false);
        navigate("/orders"); // or your success page
      } else {
        alert("Payment status unknown. Please check your orders.");
      }
    } catch (e) {
      console.error("Checkout failed:", e?.response || e);
      alert(e?.response?.data?.error || "Checkout failed");
    }
  };

  return (
    <div className="cart-container">
      <div className="shopping-cart">
        <div className="title">Shopping Bag</div>
        {cartItems.length === 0 ? (
          <div className="empty" style={{ textAlign: "left", padding: "2rem" }}>
            <h4>Your cart is empty</h4>
          </div>
        ) : (
          <>
            {cartItems.map((item) => (
              <li key={item.id} className="cart-item">
                <div className="item" style={{ display: "flex", alignContent: "center" }}>
                  <div>
                    <img src={item.imageUrl} alt={item.name} className="cart-item-image" />
                  </div>
                  <div className="description">
                    <span>{item.brand}</span>
                    <span>{item.name}</span>
                  </div>
                  <div className="quantity">
                    <button className="plus-btn" type="button" onClick={() => handleIncreaseQuantity(item.id)}>
                      <i className="bi bi-plus-square-fill"></i>
                    </button>
                    <input type="button" name="name" value={item.quantity} readOnly />
                    <button className="minus-btn" type="button" onClick={() => handleDecreaseQuantity(item.id)}>
                      <i className="bi bi-dash-square-fill"></i>
                    </button>
                  </div>
                  <div className="total-price" style={{ textAlign: "center" }}>
                    ₹{(item.price || 0) * (item.quantity || 1)}
                  </div>
                  <button className="remove-btn" onClick={() => handleRemoveFromCart(item.id)}>
                    <i className="bi bi-trash3-fill"></i>
                  </button>
                </div>
              </li>
            ))}
            <div className="total">Total: ₹{Math.round(totalPrice)}</div>
            <Button className="btn btn-primary" style={{ width: "100%" }} onClick={() => setShowModal(true)}>
              Checkout
            </Button>
          </>
        )}
      </div>

      <CheckoutPopup
        show={showModal}
        handleClose={() => setShowModal(false)}
        cartItems={cartItems}
        totalPrice={Math.round(totalPrice)}
        handleCheckout={handleCheckout}
      />
    </div>
  );
};

export default Cart;
