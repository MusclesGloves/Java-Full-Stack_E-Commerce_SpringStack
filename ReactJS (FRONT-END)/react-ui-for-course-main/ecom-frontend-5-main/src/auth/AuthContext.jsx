import { createContext, useContext, useEffect, useState } from "react";
import API from "../axios";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem("token") || "");
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem("user")) || null; } catch { return null; }
  });
  const [roles, setRoles] = useState(() => new Set(JSON.parse(localStorage.getItem("roles")||"[]")));

  // persist token
  useEffect(() => {
    if (token) localStorage.setItem("token", token);
    else localStorage.removeItem("token");
  }, [token]);

  // fetch /api/me on token change
  useEffect(() => {
    async function fetchMe(){
      if(!token){ setUser(null); setRoles(new Set()); localStorage.removeItem("user"); localStorage.removeItem("roles"); return; }
      try{
        const res = await API.get("/me");
        const u = { username: res.data.username };
        const r = new Set(res.data.roles || []);
        setUser(u);
        setRoles(r);
        localStorage.setItem("user", JSON.stringify(u));
        localStorage.setItem("roles", JSON.stringify(Array.from(r)));
      }catch(e){
        console.warn("Failed to fetch /me, clearing token", e?.response?.data || e.message);
        setToken("");
        setUser(null);
        setRoles(new Set());
      }
    }
    fetchMe();
  }, [token]);

  const login = (t) => setToken(t);
  const logout = () => {
    setToken("");
    setUser(null);
    setRoles(new Set());
    localStorage.removeItem("cart"); // optional: clear cart on logout
  };

  const hasRole = (r) => roles.has(r) || roles.has("ROLE_"+r); // accept both "ADMIN" and "ROLE_ADMIN"
  const isAdmin = () => hasRole("ADMIN");
  const isUser = () => hasRole("USER");

  return (
    <AuthContext.Provider value={{ token, login, logout, user, roles, hasRole, isAdmin, isUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(){ return useContext(AuthContext); }