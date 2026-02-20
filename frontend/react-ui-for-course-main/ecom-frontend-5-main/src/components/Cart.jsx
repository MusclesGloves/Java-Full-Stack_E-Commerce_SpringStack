import React, { useContext, useEffect, useMemo, useState } from "react";
import AppContext from "../Context/Context";
import unplugged from "../assets/unplugged.png";
import API from "../axios";

const Cart = () => {
  const { cart, removeFromCart, clearCart, updateQuantity, refreshData } =
    useContext(AppContext);

  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);

  // Base URL for API (remove trailing slash if any)
  const apiBase = useMemo(() => {
    const base = API?.defaults?.baseURL || "";
    return base.replace(/\/$/, "");
  }, []);

  /**
   * ✅ PERFORMANCE FIX:
   * - Keep ONLY 1 request to /products
   * - DO NOT fetch per-product images as blobs
   * - Set imageUrl directly to: `${apiBase}/product/{id}/image`
   */
  useEffect(() => {
    const hydrateCartItems = async () => {
      setLoading(true);

      try {
        // One request: fetch products so we can validate cart items and enrich details
        const res = await API.get("/products");
        const products = res.data || [];

        const byId = new Map(products.map((p) => [p.id, p]));

        // Filter cart items that still exist in backend
        const valid = (cart || []).filter((it) => byId.has(it.id));

        // Merge backend product info (fresh price/stock/name etc.) + attach image URL
        const hydrated = valid.map((it) => {
          const p = byId.get(it.id);
          return {
            ...p,
            quantity: it.quantity || 1,
            imageUrl: `${apiBase}/product/${it.id}/image`,
          };
        });

        setCartItems(hydrated);
      } catch (e) {
        console.error("Error fetching product data:", e);
        // Fallback: show cart as-is (still attach image URL so UI works)
        const fallback = (cart || []).map((it) => ({
          ...it,
          imageUrl: `${apiBase}/product/${it.id}/image`,
        }));
        setCartItems(fallback);
      } finally {
        setLoading(false);
      }
    };

    if (cart?.length) hydrateCartItems();
    else {
      setCartItems([]);
      setLoading(false);
    }
  }, [cart, apiBase]);

  const totalPrice = useMemo(() => {
    return (cartItems || []).reduce(
      (acc, item) => acc + (item.price || 0) * (item.quantity || 1),
      0
    );
  }, [cartItems]);

  const handleCheckout = async () => {
    try {
      // If you want to ensure latest data before checkout:
      // await refreshData();
      // proceed checkout logic...
      alert("Checkout successful!");
      clearCart();
    } catch (error) {
      console.error("Checkout error:", error);
      alert("Checkout failed!");
    }
  };

  if (loading) {
    return (
      <h2 className="text-center" style={{ padding: "18rem" }}>
        Loading cart...
      </h2>
    );
  }

  if (!cartItems.length) {
    return (
      <div className="text-center" style={{ padding: "10rem" }}>
        <h2>Your cart is empty</h2>
      </div>
    );
  }

  return (
    <div className="container" style={{ marginTop: "100px" }}>
      <h2 className="mb-4">Your Cart</h2>

      {cartItems.map((item) => (
        <div
          key={item.id}
          className="d-flex align-items-center justify-content-between border p-3 mb-3"
          style={{ borderRadius: "10px" }}
        >
          <div className="d-flex align-items-center">
            <img
              src={item.imageUrl}
              alt={item.name}
              width="80"
              height="80"
              loading="lazy"
              style={{ borderRadius: "8px", objectFit: "cover" }}
              onError={(e) => {
                e.currentTarget.src = unplugged;
              }}
            />

            <div className="ms-3">
              <h5 className="mb-1">{item.name}</h5>
              <p className="mb-0">
                ₹{item.price} × {item.quantity}
              </p>
              {item.stockQuantity != null && (
                <small style={{ opacity: 0.7 }}>
                  Stock: {item.stockQuantity}
                </small>
              )}
            </div>
          </div>

          <div className="d-flex align-items-center gap-2">
            <button
              className="btn btn-outline-secondary btn-sm"
              onClick={() => updateQuantity(item.id, item.quantity - 1)}
              disabled={item.quantity <= 1}
            >
              -
            </button>

            <span style={{ minWidth: 24, textAlign: "center" }}>
              {item.quantity}
            </span>

            <button
              className="btn btn-outline-secondary btn-sm"
              onClick={() => updateQuantity(item.id, item.quantity + 1)}
              disabled={
                item.stockQuantity != null && item.quantity >= item.stockQuantity
              }
              title={
                item.stockQuantity != null && item.quantity >= item.stockQuantity
                  ? "Maximum stock reached"
                  : undefined
              }
            >
              +
            </button>

            <button
              className="btn btn-danger btn-sm"
              onClick={() => removeFromCart(item.id)}
            >
              Remove
            </button>
          </div>
        </div>
      ))}

      <div className="d-flex justify-content-between align-items-center mt-4">
        <h4>Total: ₹{totalPrice}</h4>
        <button className="btn btn-primary" onClick={handleCheckout}>
          Checkout
        </button>
      </div>
    </div>
  );
};

export default Cart;