import { useNavigate, useParams } from "react-router-dom";
import { useContext, useEffect, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import AppContext from "../Context/Context";
import API from "../axios";

const Product = () => {
  const { isAdmin } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();

  const { addToCart, removeFromCart, cart, refreshData } = useContext(AppContext);

  const [product, setProduct] = useState(null);
  const [imageUrl, setImageUrl] = useState("");

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await API.get(`/product/${id}`);
        const p = response.data;
        setProduct(p);

        if (p?.imageName) {
          const img = await API.get(`/product/${id}/image`, { responseType: "blob" });
          setImageUrl(URL.createObjectURL(img.data));
        }
      } catch (error) {
        console.error("Error fetching product:", error);
      }
    };

    fetchProduct();
  }, [id]);

  const handleDelete = async () => {
    if (!window.confirm("Delete this product?")) return;
    try {
      await API.delete(`/product/${id}`);
      removeFromCart(Number(id));
      alert("Deleted");
      refreshData();
      navigate("/");
    } catch (e) {
      alert("Delete failed: " + (e?.response?.data?.error || e.message));
    }
  };

  const handleEditClick = () => navigate(`/product/update/${id}`);

  const handlAddToCart = () => {
    addToCart(product);
    // alert shown from context if stock limit is hit
  };

  if (!product) {
    return (
      <h2 className="text-center" style={{ padding: "10rem" }}>
        Loading...
      </h2>
    );
  }

  // --- UI guard: disable Add button when cart already at stock cap ---
  const inCartQty = cart.find((p) => p.id === product.id)?.quantity || 0;
  const atLimit =
    product?.stockQuantity != null && inCartQty >= product.stockQuantity;
  const isOut =
    product?.productAvailable === false || product?.stockQuantity === 0;

  return (
    <>
      <div className="containers" style={{ display: "flex" }}>
        <img
          className="left-column-img"
          src={imageUrl}
          alt={product.imageName}
          style={{ width: "50%", height: "auto" }}
        />

        <div className="right-column" style={{ width: "50%" }}>
          <div className="product-description">
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <span style={{ fontSize: "1.2rem", fontWeight: "lighter" }}>
                {product.category}
              </span>
              <p className="release-date" style={{ marginBottom: "2rem" }}>
                <h6>
                  Listed :
                  <span>
                    <i> {new Date(product.releaseDate).toLocaleDateString()}</i>
                  </span>
                </h6>
              </p>
            </div>

            <h1
              style={{
                fontSize: "2rem",
                marginBottom: "0.5rem",
                textTransform: "capitalize",
                letterSpacing: "1px",
              }}
            >
              {product.name}
            </h1>
            <i style={{ marginBottom: "3rem" }}>{product.brand}</i>
            <p
              style={{
                fontWeight: "bold",
                fontSize: "1rem",
                margin: "10px 0px 0px",
              }}
            >
              PRODUCT DESCRIPTION :
            </p>
            <p style={{ marginBottom: "1rem" }}>{product.description}</p>
          </div>

          <div className="product-price">
            <span style={{ fontSize: "2rem", fontWeight: "bold" }}>
              {"$" + product.price}
            </span>

            <button
              className={`cart-btn ${isOut || atLimit ? "disabled-btn" : ""}`}
              onClick={handlAddToCart}
              disabled={isOut || atLimit}
              style={{
                padding: "1rem 2rem",
                fontSize: "1rem",
                backgroundColor: "#007bff",
                color: "white",
                border: "none",
                borderRadius: "5px",
                cursor: "pointer",
                marginBottom: "1rem",
              }}
              title={atLimit ? "Maximum stock reached" : undefined}
            >
              {isOut ? "Out of Stock" : atLimit ? "Limit Reached" : "Add to cart"}
            </button>

            {isAdmin() && (
              <h6 style={{ marginBottom: "1rem" }}>
                Stock Available :{" "}
                <i style={{ color: "green", fontWeight: "bold" }}>
                  {product.stockQuantity}
                </i>
              </h6>
            )}
          </div>

          {isAdmin() && (
            <div className="update-button" style={{ display: "flex", gap: "1rem" }}>
              <button
                className="btn btn-primary"
                type="button"
                onClick={handleEditClick}
                style={{
                  padding: "1rem 2rem",
                  fontSize: "1rem",
                  backgroundColor: "#007bff",
                  color: "white",
                  border: "none",
                  borderRadius: "5px",
                  cursor: "pointer",
                }}
              >
                Update
              </button>

              <button
                className="btn btn-primary"
                type="button"
                onClick={handleDelete}
                style={{
                  padding: "1rem 2rem",
                  fontSize: "1rem",
                  backgroundColor: "#dc3545",
                  color: "white",
                  border: "none",
                  borderRadius: "5px",
                  cursor: "pointer",
                }}
              >
                Delete
              </button>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default Product;
