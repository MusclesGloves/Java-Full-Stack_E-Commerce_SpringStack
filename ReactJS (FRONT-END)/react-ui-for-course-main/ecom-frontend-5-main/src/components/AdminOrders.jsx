import { useEffect, useState } from "react";
import API from "../axios";

export default function AdminOrders(){
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(()=>{
    (async ()=>{
      try{
        const res = await API.get("/payments/all");
        setOrders(res.data || []);
      }catch(e){
        setError(e?.response?.data?.error || e.message);
      }finally{
        setLoading(false);
      }
    })();
  },[]);

  if(loading) return <div className="container p-4">Loading all orders…</div>;
  if(error) return <div className="container p-4 text-danger">Error: {error}</div>;

  return (
    <div className="container p-4">
      <h3>All Payments (Admin)</h3>
      {orders.length===0 ? <div>No orders yet.</div> : (
        <table className="table table-striped">
          <thead><tr><th>User</th><th>When</th><th>Provider</th><th>Order ID</th><th>Payment ID</th><th>Status</th><th>Amount (₹)</th></tr></thead>
          <tbody>
            {orders.map(o => (
              <tr key={o.id}>
                <td>{o.username}</td>
                <td>{new Date(o.createdAt).toLocaleString()}</td>
                <td>{o.provider}</td>
                <td>{o.orderId}</td>
                <td>{o.paymentId}</td>
                <td>{o.status}</td>
                <td>{o.amount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}