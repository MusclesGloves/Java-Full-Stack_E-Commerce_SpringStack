import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import API from "../axios";

const UpdateProduct = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState({});
  const [image, setImage] = useState(null);
  const [updateProduct, setUpdateProduct] = useState({
    id: null,
    name: "",
    description: "",
    brand: "",
    price: "",
    category: "",
    releaseDate: "",
    productAvailable: false,
    stockQuantity: "",
  });

  useEffect(() => {
    (async () => {
      try {
        // 1) fetch product JSON
        const res = await API.get(`/product/${id}`);
        setProduct(res.data);
        setUpdateProduct(res.data);

        // 2) fetch image as blob and make a File for preview/re-upload
        const imgRes = await API.get(`/product/${id}/image`, { responseType: "blob" });
        const file = new File(
          [imgRes.data],
          res.data.imageName || `product-${id}.jpg`,
          { type: imgRes.headers["content-type"] || "image/jpeg" }
        );
        setImage(file);
      } catch (err) {
        console.error("Error fetching product:", err);
        alert("Failed to load product.");
      }
    })();
  }, [id]);

  useEffect(() => {
    console.log("image Updated", image);
  }, [image]);

  const handleImageChange = (e) => {
    if (e.target.files?.[0]) setImage(e.target.files[0]);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUpdateProduct((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const fd = new FormData();
      if (image) fd.append("imageFile", image);
      fd.append("product", new Blob([JSON.stringify(updateProduct)], { type: "application/json" }));

      // IMPORTANT: no manual Content-Type header here
      await API.put(`/product/${id}`, fd);

      alert("Product updated successfully!");
      navigate(`/product/${id}`);
    } catch (err) {
      console.error("Error updating product:", err?.response || err);
      const msg = err?.response?.data?.error || err?.response?.data || "Failed to update product. Please try again.";
      alert(msg);
    }
  };

  return (
    <div className="update-product-container">
      <div className="center-container" style={{ marginTop: "7rem" }}>
        <h1>Update Product</h1>
        <form className="row g-3 pt-1" onSubmit={handleSubmit}>
          <div className="col-md-6">
            <label className="form-label"><h6>Name</h6></label>
            <input type="text" className="form-control" placeholder={product.name}
              value={updateProduct.name || ""} onChange={handleChange} name="name" />
          </div>

          <div className="col-md-6">
            <label className="form-label"><h6>Brand</h6></label>
            <input type="text" className="form-control" placeholder={product.brand}
              value={updateProduct.brand || ""} onChange={handleChange} name="brand" />
          </div>

          <div className="col-12">
            <label className="form-label"><h6>Description</h6></label>
            <input type="text" className="form-control" placeholder={product.description}
              value={updateProduct.description || ""} onChange={handleChange} name="description" />
          </div>

          <div className="col-5">
            <label className="form-label"><h6>Price</h6></label>
            <input type="number" className="form-control" placeholder={product.price}
              value={updateProduct.price ?? ""} onChange={handleChange} name="price" />
          </div>

          <div className="col-md-6">
            <label className="form-label"><h6>Category</h6></label>
            <select className="form-select" value={updateProduct.category || ""} onChange={handleChange} name="category">
              <option value="">Select category</option>
              <option value="laptop">Laptop</option>
              <option value="headphone">Headphone</option>
              <option value="mobile">Mobile</option>
              <option value="electronics">Electronics</option>
              <option value="toys">Toys</option>
              <option value="fashion">Fashion</option>
            </select>
          </div>

          <div className="col-md-4">
            <label className="form-label"><h6>Stock Quantity</h6></label>
            <input type="number" className="form-control" placeholder={product.stockQuantity}
              value={updateProduct.stockQuantity ?? ""} onChange={handleChange} name="stockQuantity" />
          </div>

          <div className="col-md-8">
            <label className="form-label"><h6>Image</h6></label>
            <img
              src={image ? URL.createObjectURL(image) : ""}
              alt={product.imageName}
              style={{ width: "100%", height: "180px", objectFit: "cover", padding: "5px", margin: 0 }}
            />
            <input className="form-control" type="file" onChange={handleImageChange} />
          </div>

          <div className="col-12">
            <div className="form-check">
              <input className="form-check-input" type="checkbox" name="productAvailable"
                id="gridCheck" checked={!!updateProduct.productAvailable}
                onChange={(e) => setUpdateProduct((p) => ({ ...p, productAvailable: e.target.checked }))} />
              <label className="form-check-label">Product Available</label>
            </div>
          </div>

          <div className="col-12">
            <button type="submit" className="btn btn-primary">Submit</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UpdateProduct;
