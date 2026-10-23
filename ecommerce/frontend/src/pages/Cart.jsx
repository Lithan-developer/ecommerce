import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api, { errMsg } from '../api/axios.js';

export default function Cart() {
  const [cart, setCart] = useState({ items: [], total: 0 });
  const [address, setAddress] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const load = () => api.get('/cart').then((r) => setCart(r.data)).catch((e) => setError(errMsg(e)));
  useEffect(() => { load(); }, []);

  const run = async (fn) => {
    setError('');
    try {
      const { data } = await fn();
      setCart(data);
    } catch (e) {
      setError(errMsg(e));
    }
  };

  const updateQty = (id, quantity) => quantity >= 1 && run(() => api.put(`/cart/items/${id}`, { quantity }));
  const remove = (id) => run(() => api.delete(`/cart/items/${id}`));

  const placeOrder = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/orders', { shippingAddress: address });
      navigate('/orders');
    } catch (err) {
      setError(errMsg(err));
    }
  };

  return (
    <div>
      <h2>Your Cart</h2>
      {error && <p className="error">{error}</p>}
      {cart.items.length === 0 ? (
        <p className="muted">Your cart is empty.</p>
      ) : (
        <>
          <table>
            <thead>
              <tr><th>Product</th><th>Price</th><th>Qty</th><th>Subtotal</th><th></th></tr>
            </thead>
            <tbody>
              {cart.items.map((i) => (
                <tr key={i.productId}>
                  <td>{i.name}</td>
                  <td>₹{Number(i.price).toFixed(2)}</td>
                  <td>
                    <button className="btn btn-outline" onClick={() => updateQty(i.productId, i.quantity - 1)}>-</button>
                    <span className="qty">{i.quantity}</span>
                    <button className="btn btn-outline" onClick={() => updateQty(i.productId, i.quantity + 1)}>+</button>
                  </td>
                  <td>₹{Number(i.subtotal).toFixed(2)}</td>
                  <td><button className="btn btn-danger" onClick={() => remove(i.productId)}>Remove</button></td>
                </tr>
              ))}
            </tbody>
          </table>
          <h3 className="total">Total: ₹{Number(cart.total).toFixed(2)}</h3>

          <form onSubmit={placeOrder} className="card">
            <h3>Checkout</h3>
            <textarea placeholder="Shipping address" required rows={3} value={address}
                      onChange={(e) => setAddress(e.target.value)} />
            <button className="btn" type="submit">Place order</button>
          </form>
        </>
      )}
    </div>
  );
}
