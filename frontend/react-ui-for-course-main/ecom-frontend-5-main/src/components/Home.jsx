import React, { useContext, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import AppContext from "../Context/Context";
import unplugged from "../assets/unplugged.png";
import API from "../axios";

const Home = ({ selectedCategory }) => {
  const { data, isError, addToCart, refreshData, cart } = useContext(AppContext);

  const [products, setProducts] = useState([]);
  const [isDataFetched, setIsDataFetched] = useState(false);

  // Helper: build image URL directly (no blob fetching)
  const getImageSrc = (productId) => {
    const base = (API?.defaults?.baseURL || "").replace(/\/$/, "");
    // base is expected like: https://your-backend.com/api
    return `${base}/product/${productId}/image`;
  };

  // Fetch products once
  useEffect(() => {
    if (!isDataFetched) {
      refreshData();
      setIsDataFetched(true);
    }
  }, [refreshData, isDataFetched]);

  // When product list changes, enrich with imageUrl (no network call here)
  useEffect(() => {
    if (Array.isArray(data)) {
      setProducts(
        data.map((p) => ({
          ...p,
          imageUrl: getImageSrc(p.id),
        }))
      );
    }
  }, [data]);

  const filteredProducts = useMemo(() => {
    if (!selectedCategory) return products;
    const sc = selectedCategory.toLowerCase();
    return products.filter(
      (p) => (p.category || "").toLowerCase() === sc
    );
  }, [products, selectedCategory]);

  if (isError) {
    return (
      <h2 className="text-center" style={{ padding: "18rem" }}>
        <img
          src={unplugged}
          alt="Error"
          style={{ width: "100px", height: "100px" }}
        />
      </h2>
    );
  }

  return (
    <div
      className="grid"
      style={{
        marginTop: "88px",
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
        gap: "20px",
        padding: "20px",
      }}
    >
      {filteredProducts.length === 0 ? (
        <h2
          className="text-center"
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          No Products Available
        </h2>
      ) : (
        filteredProducts.map((product) => {
          const {
            id,
            brand,
            name,
            price,
            productAvailable,
            imageUrl,
            stockQuantity,
          } = product;

          // cart-aware guards
          const inCartQty = cart.find((p) => p.id === id)?.quantity || 0;
          const atLimit = stockQuantity != null && inCartQty >= stockQuantity;
          const isOut = productAvailable === false || stockQuantity === 0;

          return (
            <div
              className="card mb-3"
              style={{
                width: "200px",
                height: "360px",
                boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
                borderRadius: "10px",
                overflow: "hidden",
                backgroundColor: productAvailable ? "#fff" : "#ccc",
                display: "flex",
                flexDirection: "column",
                justifyContent: "space-around",
                alignItems: "stretch",
                boxSizing: "content-box",
              }}
              key={id}
            >
              <Link
                to={`/product/${id}`}
                style={{ textDecoration: "none", color: "inherit" }}
              >
                <img
                  src={imageUrl}
                  alt={name}
                  loading="lazy"
                  decoding="async"
                  onError={(e) => {
                    // fallback if image missing
                    e.currentTarget.src =
                      "data:image/svg+xml;charset=utf-8," +
                      encodeURIComponent(
                        `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300">
                          <rect width="100%" height="100%" fill="#eee"/>
                          <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#999" font-family="Arial" font-size="18">
                            No Image
                          </text>
                        </svg>`
                      );
                  }}
                  style={{
                    width: "100%",
                    height: "150px",
                    objectFit: "cover",
                    padding: "5px",
                    margin: "0",
                    borderRadius: "10px 10px 10px 10px",
                  }}
                />

                <div
                  className="card-body"
                  style={{
                    flexGrow: 1,
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "space-between",
                    padding: "10px",
                  }}
                >
                  <div>
                    <h5
                      className="card-title"
                      style={{
                        margin: "0 0 10px 0",
                        fontSize: "1.2rem",
                      }}
                    >
                      {String(name || "").toUpperCase()}
                    </h5>
                    <i
                      className="card-brand"
                      style={{ fontStyle: "italic", fontSize: "0.8rem" }}
                    >
                      {"~ " + brand}
                    </i>
                  </div>

                  <hr className="hr-line" style={{ margin: "10px 0" }} />

                  <div className="home-cart-price">
                    <h5
                      className="card-text"
                      style={{
                        fontWeight: "600",
                        fontSize: "1.1rem",
                        marginBottom: "5px",
                      }}
                    >
                      <i className="bi bi-currency-rupee"></i>
                      {price}
                    </h5>
                  </div>

                  <button
                    className="btn-hover color-9"
                    style={{ margin: "10px 25px 0px" }}
                    onClick={(e) => {
                      e.preventDefault();
                      addToCart(product);
                    }}
                    disabled={isOut || atLimit}
                    title={atLimit ? "Maximum stock reached" : undefined}
                  >
                    {isOut ? "Out of Stock" : atLimit ? "Limit Reached" : "Add to Cart"}
                  </button>
                </div>
              </Link>
            </div>
          );
        })
      )}
    </div>
  );
};

export default Home;