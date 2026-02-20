import React, { useContext, useEffect, useMemo, useState } from "react";
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

  // Helper: build image URL directly (no blob fetching)
  const getImageSrc = (productId) => {
    const base = (API?.defaults?.baseURL || "").replace(/\/$/, "");
    return `${base}/product/${productId}/image`;
  };

  // Sync cart -> cartItems, attach image urls
  useEffect(() => {
    const src = Array.isArray(cart) ? cart : [];
    setCartItems(
      src.map((it) => ({
        ...it,
        imageUrl: getImageSrc(it.id),
      }))
    );
  }, [cart]);

  const totalPrice = useMemo(() => {
    const src = cartItems?.length ? cartItems : (cart || []);
    return src.reduce(
      (acc, item) => acc + (item.price || 0) * (item.quantity || 1),
      0
    );
  }, [cartItems, cart]);

  const handleIncreaseQuantity = (itemId) => {
    setCartItems((items) =>
      items.map((it) => {
        if (it.id !== itemId) return it;
        const next = (it.quantity || 1) + 1;
        const max = it.stockQuantity ?? Infinity;
        return { ...it, quantity: Math.min(next, max) };
      })
    );
  };

  const handleDecreaseQuantity = (itemId) => {
    setCartItems((items) =>
      items.map((it) => {
        if (it.id !== itemId) return it;
        const next = (it.quantity || 1) - 1;
        return { ...it, quantity: Math.max(next, 1) };
      })
    );
  };

  const handleRemoveFromCart = (itemId) => {
    removeFromCart(itemId);
    setCartItems((items) => items.filter((it) => it.id !== itemId));
  };

  const handleCheckout = async () => {
    try {
      const items = (cartItems || []).map((it) => ({
        productId: it.id,
        quantity: it.quantity || 1,
      }));

      const amount = Math.round(totalPrice);
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
        navigate("/orders");
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
                    <img
                      src={item.imageUrl}
                      alt={item.name}
                      className="cart-item-image"
                      loading="lazy"
                      decoding="async"
                      onError={(e) => {
                        e.currentTarget.src =
                          "data:image/svg+xml;charset=utf-8," +
                          encodeURIComponent(
                            `<svg xmlns="http://www.w3.org/2000/svg" width="240" height="180">
                              <rect width="100%" height="100%" fill="#eee"/>
                              <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#999" font-family="Arial" font-size="16">
                                No Image
                              </text>
                            </svg>`
                          );
                      }}
                    />
                  </div>

                  <div className="description">
                    <span>{item.brand}</span>
                    <span>{item.name}</span>
                  </div>

                  <div className="quantity">
                    <button
                      className="plus-btn"
                      type="button"
                      onClick={() => handleIncreaseQuantity(item.id)}
                      disabled={
                        item.stockQuantity != null &&
                        (item.quantity || 1) >= item.stockQuantity
                      }
                      title="Increase"
                    >
                      <i className="bi bi-plus-square-fill"></i>
                    </button>

                    <input type="button" name="name" value={item.quantity || 1} readOnly />

                    <button
                      className="minus-btn"
                      type="button"
                      onClick={() => handleDecreaseQuantity(item.id)}
                      disabled={(item.quantity || 1) <= 1}
                      title="Decrease"
                    >
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

            <Button
              className="btn btn-primary"
              style={{ width: "100%" }}
              onClick={() => setShowModal(true)}
            >
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