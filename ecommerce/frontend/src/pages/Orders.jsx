import { useEffect, useState } from 'react';
import api, { errMsg } from '../api/axios.js';

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');

  const load = () =>
    api.get('/orders', { params: { size: 50 } })
      .then((r) => setOrders(r.data.content))
      .catch((e) => setError(errMsg(e)));

  useEffect(() => { load(); }, []);

  const cancel = async (id) => {
    setError('');
    try {
      await api.put(`/orders/${id}/cancel`);
      load();
    } catch (e) {
      setError(errMsg(e));
    }
  };

  return (
    <div>
      <h2>My Orders</h2>
      {error && <p className="error">{error}</p>}
      {orders.length === 0 && <p className="muted">No orders yet.</p>}
      {orders.map((o) => (
        <div className="card" key={o.id}>
          <div className="row">
            <strong>Order #{o.id}</strong>
            <span className={`badge ${o.status}`}>{o.status}</span>
          </div>
          <p className="muted">{new Date(o.createdAt).toLocaleString()} · {o.shippingAddress}</p>
          <ul>
            {o.items.map((i) => (
              <li key={i.productId}>{i.productName} × {i.quantity} — ₹{Number(i.subtotal).toFixed(2)}</li>
            ))}
          </ul>
          <strong>Total: ₹{Number(o.totalAmount).toFixed(2)}</strong>
          {o.status === 'PENDING' && (
            <div><button className="btn btn-danger" onClick={() => cancel(o.id)}>Cancel order</button></div>
          )}
        </div>
      ))}
    </div>
  );
}
