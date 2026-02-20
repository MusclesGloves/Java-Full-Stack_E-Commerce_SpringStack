import axios from "../axios";
import { useState, useEffect, createContext } from "react";

const AppContext = createContext({
  data: [],
  isError: "",
  cart: [],
  addToCart: () => {},
  removeFromCart: () => {},
  refreshData: () => {},
  clearCart: () => {},
});

export const AppProvider = ({ children }) => {
  const [data, setData] = useState([]);
  const [isError, setIsError] = useState("");
  const [cart, setCart] = useState(
    JSON.parse(localStorage.getItem("cart")) || []
  );

  // --- Stock-aware addToCart (single source of truth) ---
  const addToCart = (product) => {
  const stock = product?.stockQuantity ?? Infinity;

  setCart((prev) => {
    const idx = prev.findIndex((p) => p.id === product.id);

    // already in cart → bump qty but do not exceed stock
    if (idx >= 0) {
      const current = prev[idx];
      const curQty = current.quantity || 1;
      const newQty = Math.min(curQty + 1, stock);

      if (newQty === curQty) {
        alert("Maximum available stock reached for this item.");
        return prev; // no change
      }

      const copy = [...prev];
      copy[idx] = { ...current, quantity: newQty, stockQuantity: stock };
      localStorage.setItem("cart", JSON.stringify(copy));

      // ✅ success feedback
      alert(`${product.name} quantity updated in cart.`);
      return copy;
    }

    // not in cart yet
    if (stock <= 0 || product?.productAvailable === false) {
      alert("This product is currently out of stock.");
      return prev;
    }

    const next = [...prev, { ...product, quantity: 1, stockQuantity: stock }];
    localStorage.setItem("cart", JSON.stringify(next));

    // ✅ success feedback
    alert(`${product.name} added to cart successfully.`);
    return next;
  });
};


  const removeFromCart = (productId) => {
    const updatedCart = cart.filter((item) => item.id !== productId);
    setCart(updatedCart);
    localStorage.setItem("cart", JSON.stringify(updatedCart));
  };

  const clearCart = () => {
    setCart([]);
    localStorage.setItem("cart", JSON.stringify([]));
  };

  const refreshData = async () => {
    try {
      const response = await axios.get("/products");
      setData(response.data || []);
    } catch (error) {
      setIsError(error?.message || "Failed to load products");
    }
  };

  useEffect(() => {
    refreshData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    localStorage.setItem("cart", JSON.stringify(cart));
  }, [cart]);

  return (
    <AppContext.Provider
      value={{ data, isError, cart, addToCart, removeFromCart, refreshData, clearCart }}
    >
      {children}
    </AppContext.Provider>
  );
};

export default AppContext;
