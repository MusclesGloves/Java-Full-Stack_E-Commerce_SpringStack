import { useState } from "react";
import API from "../axios";
import { useAuth } from "./AuthContext";
import { useNavigate, Link } from "react-router-dom";
import "./auth.css";

export default function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [admin, setAdmin] = useState(false);
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
      const res = await API.post("/auth/register", { username, password, admin });
      const token = res.data?.token;
      if (!token) throw new Error("No token in response");
      login(token);
      setBanner({ type: "success", text: "Account created" });
      setTimeout(()=> navigate("/"), 300);
    } catch (err) {
      const msg = err?.response?.data?.error || "Registration failed";
      setBanner({ type: "error", text: msg });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-bg">
      <div className="auth-card">
        <h1 className="auth-title">Create your account</h1>
        <p className="auth-sub">One account for shopping & managing your orders</p>
        {banner.text && <div className={`auth-banner ${banner.type === "error" ? "banner-error" : "banner-success"}`}>{banner.text}</div>}
        <form onSubmit={submit}>
          <label>Username</label>
          <input className="auth-input" value={username} onChange={(e)=>setUsername(e.target.value)} placeholder="e.g. alice"/>
          <div className="auth-error">{formErr.username || ""}</div>
          <label>Password</label>
          <input className="auth-input" type="password" value={password} onChange={(e)=>setPassword(e.target.value)} placeholder="••••••••" />
          <div className="auth-error">{formErr.password || ""}</div>
          <label style={{display:'flex',alignItems:'center',gap:8,marginTop:8}}>
            <input type="checkbox" checked={admin} onChange={(e)=>setAdmin(e.target.checked)} />
            Register as Admin
          </label>
          <button className="auth-btn" disabled={loading}>{loading ? "Creating..." : "Create account"}</button>
        </form>
        <div className="auth-footer">
          Already have an account? <Link className="auth-link" to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}
