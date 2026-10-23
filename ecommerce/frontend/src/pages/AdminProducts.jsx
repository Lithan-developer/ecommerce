import { useEffect, useState } from 'react';
import api, { errMsg } from '../api/axios.js';

const empty = { name: '', description: '', price: '', stock: '', imageUrl: '', categoryId: '' };

export default function AdminProducts() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(empty);
  const [editingId, setEditingId] = useState(null);
  const [newCategory, setNewCategory] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const [p, c] = await Promise.all([api.get('/products', { params: { size: 100 } }), api.get('/categories')]);
      setProducts(p.data.content);
      setCategories(c.data);
    } catch (e) {
      setError(errMsg(e));
    }
  };
  useEffect(() => { load(); }, []);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    const payload = { ...form, price: Number(form.price), stock: Number(form.stock), categoryId: Number(form.categoryId) };
    try {
      if (editingId) await api.put(`/products/${editingId}`, payload);
      else await api.post('/products', payload);
      setForm(empty);
      setEditingId(null);
      load();
    } catch (err) {
      setError(errMsg(err));
    }
  };

  const edit = (p) => {
    setEditingId(p.id);
    setForm({ name: p.name, description: p.description || '', price: p.price, stock: p.stock,
              imageUrl: p.imageUrl || '', categoryId: p.categoryId });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const remove = async (id) => {
    if (!window.confirm('Delete this product?')) return;
    try {
      await api.delete(`/products/${id}`);
      load();
    } catch (e) {
      setError(errMsg(e));
    }
  };

  const addCategory = async (e) => {
    e.preventDefault();
    try {
      await api.post('/categories', { name: newCategory });
      setNewCategory('');
      load();
    } catch (err) {
      setError(errMsg(err));
    }
  };

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <div>
      <h2>Manage Products</h2>
      {error && <p className="error">{error}</p>}

      <form onSubmit={addCategory} className="inline-form">
        <input placeholder="New category name" value={newCategory} required onChange={(e) => setNewCategory(e.target.value)} />
        <button className="btn btn-outline" type="submit">Add category</button>
      </form>

      <form onSubmit={submit} className="card">
        <h3>{editingId ? `Edit product #${editingId}` : 'Add product'}</h3>
        <input placeholder="Name" value={form.name} required onChange={set('name')} />
        <textarea placeholder="Description" value={form.description} onChange={set('description')} />
        <input type="number" step="0.01" min="0.01" placeholder="Price" value={form.price} required onChange={set('price')} />
        <input type="number" min="0" placeholder="Stock" value={form.stock} required onChange={set('stock')} />
        <input placeholder="Image URL (optional)" value={form.imageUrl} onChange={set('imageUrl')} />
        <select value={form.categoryId} required onChange={set('categoryId')}>
          <option value="">Select category</option>
          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <div>
          <button className="btn" type="submit">{editingId ? 'Update' : 'Create'}</button>
          {editingId && (
            <button type="button" className="btn btn-outline" onClick={() => { setEditingId(null); setForm(empty); }}>Cancel</button>
          )}
        </div>
      </form>

      <table>
        <thead><tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th></th></tr></thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td><td>{p.name}</td><td>{p.categoryName}</td>
              <td>₹{Number(p.price).toFixed(2)}</td><td>{p.stock}</td>
              <td>
                <button className="btn btn-outline" onClick={() => edit(p)}>Edit</button>
                <button className="btn btn-danger" onClick={() => remove(p.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
