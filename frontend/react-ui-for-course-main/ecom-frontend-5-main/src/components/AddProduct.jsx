import React, { useState } from "react";
import API from "../axios";

const AddProduct = () => {
  const [product, setProduct] = useState({
    name: "",
    brand: "",
    description: "",
    price: "",
    category: "",
    stockQuantity: "",
    releaseDate: "",
    productAvailable: false,
  });
  const [image, setImage] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProduct({ ...product, [name]: value });
  };

  const handleImageChange = (e) => {
    setImage(e.target.files[0]);
  };

  const submitHandler = (event) => {
    event.preventDefault();

    // Convert price & stock to numbers before sending
    const payload = {
      ...product,
      price: product.price === "" ? null : Number(product.price),
      stockQuantity: product.stockQuantity === "" ? null : parseInt(product.stockQuantity, 10),
    };

    const formData = new FormData();
    formData.append("imageFile", image);
    formData.append(
      "product",
      new Blob([JSON.stringify(payload)], { type: "application/json" })
    );

    API.post("/product", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    })
      .then((response) => {
        console.log("Product added successfully:", response.data);
        alert("Product added successfully");
      })
      .catch((error) => {
        console.error("Error adding product:", error);
        alert("Error adding product");
      });
  };

  return (
    <div className="container">
      <div className="center-container">
        <form className="row g-3 pt-5" onSubmit={submitHandler}>
          <div className="col-md-6">
            <label className="form-label"><h6>Name</h6></label>
            <input
              type="text"
              className="form-control"
              placeholder="Product Name"
              onChange={handleInputChange}
              value={product.name}
              name="name"
              required
            />
          </div>
          <div className="col-md-6">
            <label className="form-label"><h6>Brand</h6></label>
            <input
              type="text"
              name="brand"
              className="form-control"
              placeholder="Enter your Brand"
              value={product.brand}
              onChange={handleInputChange}
              id="brand"
              required
            />
          </div>
          <div className="col-12">
            <label className="form-label"><h6>Description</h6></label>
            <input
              type="text"
              className="form-control"
              placeholder="Add product description"
              value={product.description}
              name="description"
              onChange={handleInputChange}
              id="description"
              required
            />
          </div>
          <div className="col-5">
            <label className="form-label"><h6>Price</h6></label>
            <input
              type="number"
              className="form-control"
              placeholder="Eg: 1000"
              onChange={handleInputChange}
              value={product.price}
              name="price"
              id="price"
              min="1"
              step="0.01"
              required
            />
          </div>
          <div className="col-md-6">
            <label className="form-label"><h6>Category</h6></label>
            <select
              className="form-select"
              value={product.category}
              onChange={handleInputChange}
              name="category"
              id="category"
              required
            >
              <option value="">Select category</option>
              <option value="Laptop">Laptop</option>
              <option value="Headphone">Headphone</option>
              <option value="Mobile">Mobile</option>
              <option value="Electronics">Electronics</option>
              <option value="Toys">Toys</option>
              <option value="Fashion">Fashion</option>
            </select>
          </div>
          <div className="col-md-4">
            <label className="form-label"><h6>Stock Quantity</h6></label>
            <input
              type="number"
              className="form-control"
              placeholder="Stock Remaining"
              onChange={handleInputChange}
              value={product.stockQuantity}
              name="stockQuantity"
              id="stockQuantity"
              min="0"
              step="1"
              required
            />
          </div>
          <div className="col-md-4">
            <label className="form-label"><h6>Release Date</h6></label>
            <input
              type="date"
              className="form-control"
              value={product.releaseDate}
              name="releaseDate"
              onChange={handleInputChange}
              id="releaseDate"
              required
            />
          </div>
          <div className="col-md-4">
            <label className="form-label"><h6>Image</h6></label>
            <input
              className="form-control"
              type="file"
              onChange={handleImageChange}
              required
            />
          </div>
          <div className="col-12">
            <div className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                name="productAvailable"
                id="gridCheck"
                checked={product.productAvailable}
                onChange={(e) =>
                  setProduct({ ...product, productAvailable: e.target.checked })
                }
              />
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

export default AddProduct;
