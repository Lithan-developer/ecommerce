import { useEffect, useState } from 'react';
import api, { errMsg } from '../api/axios.js';

const STATUSES = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

export default function AdminOrders() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');

  const load = () =>
    api.get('/admin/orders', { params: { size: 100 } })
      .then((r) => setOrders(r.data.content))
      .catch((e) => setError(errMsg(e)));

  useEffect(() => { load(); }, []);

  const changeStatus = async (id, status) => {
    setError('');
    try {
      await api.put(`/admin/orders/${id}/status`, null, { params: { status } });
      load();
    } catch (e) {
      setError(errMsg(e));
    }
  };

  return (
    <div>
      <h2>All Orders</h2>
      {error && <p className="error">{error}</p>}
      <table>
        <thead><tr><th>#</th><th>Customer</th><th>Date</th><th>Total</th><th>Status</th></tr></thead>
        <tbody>
          {orders.map((o) => (
            <tr key={o.id}>
              <td>{o.id}</td>
              <td>{o.customerEmail}</td>
              <td>{new Date(o.createdAt).toLocaleString()}</td>
              <td>₹{Number(o.totalAmount).toFixed(2)}</td>
              <td>
                <select value={o.status} onChange={(e) => changeStatus(o.id, e.target.value)}
                        disabled={o.status === 'CANCELLED' || o.status === 'DELIVERED'}>
                  {STATUSES.map((s) => <option key={s}>{s}</option>)}
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
