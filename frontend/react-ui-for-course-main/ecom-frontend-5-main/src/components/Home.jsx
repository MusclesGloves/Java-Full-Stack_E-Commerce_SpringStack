import React, { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import AppContext from "../Context/Context";
import unplugged from "../assets/unplugged.png";

const Home = ({ selectedCategory }) => {
  // ⬇️ include cart so we can check per-item quantities
  const { data, isError, addToCart, refreshData, cart } = useContext(AppContext);
  const [products, setProducts] = useState([]);
  const [isDataFetched, setIsDataFetched] = useState(false);

  useEffect(() => {
    if (!isDataFetched) {
      refreshData();
      setIsDataFetched(true);
    }
  }, [refreshData, isDataFetched]);

  useEffect(() => {
    if (data && data.length > 0) {
      const fetchImagesAndUpdateProducts = async () => {
        const updatedProducts = await Promise.all(
          data.map(async (product) => {
            try {
              const response = await axios.get(
                `http://localhost:8080/api/product/${product.id}/image`,
                { responseType: "blob" }
              );
              const imageUrl = URL.createObjectURL(response.data);
              return { ...product, imageUrl };
            } catch (error) {
              console.error("Error fetching image for product ID:", product.id, error);
              return { ...product, imageUrl: "placeholder-image-url" };
            }
          })
        );
        setProducts(updatedProducts);
      };

      fetchImagesAndUpdateProducts();
    }
  }, [data]);

  const filteredProducts = selectedCategory
    ? products.filter(
        (product) =>
          product.category?.toLowerCase() === selectedCategory.toLowerCase()
      )
    : products;

  if (isError) {
    return (
      <h2 className="text-center" style={{ padding: "18rem" }}>
        <img src={unplugged} alt="Error" style={{ width: "100px", height: "100px" }} />
      </h2>
    );
  }

  return (
    <>
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
            style={{ display: "flex", justifyContent: "center", alignItems: "center" }}
          >
            No Products Available
          </h2>
        ) : (
          filteredProducts.map((product) => {
            const { id, brand, name, price, productAvailable, imageUrl, stockQuantity } =
              product;

            // ⬇️ cart-aware guards (same logic as Product page)
            const inCartQty = cart.find((p) => p.id === id)?.quantity || 0;
            const atLimit =
              stockQuantity != null && inCartQty >= stockQuantity;
            const isOut =
              productAvailable === false || stockQuantity === 0;

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
                        style={{ margin: "0 0 10px 0", fontSize: "1.2rem" }}
                      >
                        {name.toUpperCase()}
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
                        style={{ fontWeight: "600", fontSize: "1.1rem", marginBottom: "5px" }}
                      >
                        <i className="bi bi-currency-rupee"></i>
                        {price}
                      </h5>
                    </div>

                    {/* ⬇️ Button that reflects Out of Stock / Limit Reached */}
                    <button
                      className="btn-hover color-9"
                      style={{ margin: "10px 25px 0px" }}
                      onClick={(e) => {
                        e.preventDefault(); // keep card link from navigating
                        addToCart(product);
                      }}
                      disabled={isOut || atLimit}
                      title={atLimit ? "Maximum stock reached" : undefined}
                    >
                      {isOut
                        ? "Out of Stock"
                        : atLimit
                        ? "Limit Reached"
                        : "Add to Cart"}
                    </button>
                  </div>
                </Link>
              </div>
            );
          })
        )}
      </div>
    </>
  );
};

export default Home;
