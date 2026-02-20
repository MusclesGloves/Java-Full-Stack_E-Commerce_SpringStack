import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080/api",
});
delete API.defaults.headers.common["Authorization"];
export default API;


// Attach JWT token if present
API.interceptors.request.use((config)=>{
  const token = localStorage.getItem('token');
  if(token){ config.headers.Authorization = `Bearer ${token}`; }
  return config;
});
