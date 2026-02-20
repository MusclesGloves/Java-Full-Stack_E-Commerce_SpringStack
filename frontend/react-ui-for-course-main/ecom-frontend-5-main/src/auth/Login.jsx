import { useState } from "react";
import API from "../axios";
import { useAuth } from "./AuthContext";
import { useNavigate, Link } from "react-router-dom";
import "./auth.css";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [formErr, setFormErr] = useState({});
  const [banner, setBanner] = useState({ type: "", text: "" });

  const validate = () => {
    const e = {};
    if (!username || username.trim().length < 3) e.username = "Username must be at least 3 characters";
    if (!password || password.length < 6) e.password = "Password must be at least 6 characters";
    setFormErr(e);
    return Object.keys(e).length === 0;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    setBanner({ type: "", text: "" });
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await API.post("/auth/login", { username, password });
      const token = res.data?.token;
      if (!token) throw new Error("No token in response");
      login(token);
      setBanner({ type: "success", text: "Login successful" });
      setTimeout(() => navigate("/"), 300);
    } catch (err) {
      setBanner({ type: "error", text: err?.response?.data?.error || "Invalid credentials" });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-bg">
      <div className="auth-card">
        <h1 className="auth-title">Welcome back</h1>
        <p className="auth-sub">Please sign in to continue</p>
        {banner.text && <div className={`auth-banner ${banner.type === "error" ? "banner-error" : "banner-success"}`}>{banner.text}</div>}
        <form onSubmit={submit}>
          <label>Username</label>
          <input className="auth-input" value={username} onChange={(e)=>setUsername(e.target.value)} placeholder="e.g. alice"/>
          <div className="auth-error">{formErr.username || ""}</div>
          <label>Password</label>
          <input className="auth-input" type="password" value={password} onChange={(e)=>setPassword(e.target.value)} placeholder="••••••••" />
          <div className="auth-error">{formErr.password || ""}</div>
          <button className="auth-btn" disabled={loading}>{loading ? "Signing in..." : "Sign in"}</button>
        </form>
        <div className="auth-footer">
          New here? <Link className="auth-link" to="/register">Create an account</Link>
        </div>
        <div className="small-muted">Tip: Register with "Admin" option to manage products.</div>
      </div>
    </div>
  );
}
