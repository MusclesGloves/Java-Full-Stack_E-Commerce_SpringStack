import { useState } from "react";
import API from "../axios";
import { useAuth } from "../auth/AuthContext";
import { useNavigate } from "react-router-dom";

export default function DemoCheckout(){
  const [amount, setAmount] = useState(499);
  const [status, setStatus] = useState("");
  const { token } = useAuth();
  const navigate = useNavigate();

  const loadRazorpayScript = () => { /* unchanged */ };

  const pay = async () => {
    setStatus("");
    try {
      const res = await API.post("/payments/create-order", { amount });
      if (res.data.provider === "mock") {
        setStatus("Payment successful (Mock)");
       navigate("/orders");   // go see it immediately
        return;
      }
      const ok = await loadRazorpayScript();
      if(!ok){ setStatus("Failed to load Razorpay"); return; }
      const { orderId, amount: paise, keyId } = res.data;
      const rz = new window.Razorpay({
        key: keyId,
        amount: paise,
        currency: "INR",
        order_id: orderId,
        handler: async (resp) => {
          try{
            await API.post("/payments/verify", resp);
            setStatus("Payment successful");
           navigate("/orders");
          }catch(e){
            setStatus("Verification failed");
          }
        },
        theme: { color: "#6C5CE7" }
      });
      rz.open();
    } catch (e){
      setStatus(e?.response?.data?.error || "Payment failed");
    }

  return (
    <div className="container p-4">
      <h3>Checkout Demo</h3>
      <p>Enter an amount (₹) and click pay. Uses Razorpay in test mode if configured, otherwise Mock.</p>
      <input type="number" className="form-control" value={amount} onChange={(e)=>setAmount(parseInt(e.target.value||0))} min="1" />
      <button className="btn btn-primary mt-2" onClick={pay} disabled={!token}>Pay ₹{amount}</button>
      <div className="mt-2">{status}</div>
    </div>
  );
}
}
