import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api, { errMsg } from '../api/axios.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, totalPages: 0, totalElements: 0 });
  const [filters, setFilters] = useState({ keyword: '', categoryId: '', sortBy: 'id', direction: 'asc' });
  const [page, setPage] = useState(0);
  const [message, setMessage] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/categories').then((r) => setCategories(r.data)).catch(() => {});
  }, []);

  // Debounce so we don't hit the API on every keystroke
  useEffect(() => {
    const t = setTimeout(async () => {
      try {
        const params = { ...filters, page, size: 8 };
        if (!params.categoryId) delete params.categoryId;
        if (!params.keyword) delete params.keyword;
        const { data } = await api.get('/products', { params });
        setProducts(data.content);
        setPageInfo(data);
      } catch (e) {
        setMessage(errMsg(e));
      }
    }, 250);
    return () => clearTimeout(t);
  }, [filters, page]);

  const change = (patch) => {
    setPage(0);
    setFilters((f) => ({ ...f, ...patch }));
  };

  const addToCart = async (productId) => {
    if (!user) return navigate('/login');
    try {
      await api.post('/cart/items', { productId, quantity: 1 });
      setMessage('Added to cart');
    } catch (e) {
      setMessage(errMsg(e));
    }
    setTimeout(() => setMessage(''), 2500);
  };

  return (
    <div>
      <h2>Products</h2>
      <div className="filters">
        <input placeholder="Search products..." value={filters.keyword}
               onChange={(e) => change({ keyword: e.target.value })} />
        <select value={filters.categoryId} onChange={(e) => change({ categoryId: e.target.value })}>
          <option value="">All categories</option>
          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select value={filters.sortBy} onChange={(e) => change({ sortBy: e.target.value })}>
          <option value="id">Default</option>
          <option value="name">Name</option>
          <option value="price">Price</option>
        </select>
        <select value={filters.direction} onChange={(e) => change({ direction: e.target.value })}>
          <option value="asc">Ascending</option>
          <option value="desc">Descending</option>
        </select>
      </div>

      {message && <p className="toast">{message}</p>}

      <div className="grid">
        {products.map((p) => (
          <div className="card" key={p.id}>
            {p.imageUrl && <img src={p.imageUrl} alt={p.name} className="thumb" />}
            <h3>{p.name}</h3>
            <p className="muted">{p.categoryName}</p>
            <p>{p.description}</p>
            <p className="price">₹{Number(p.price).toFixed(2)}</p>
            <p className={p.stock > 0 ? 'muted' : 'error'}>{p.stock > 0 ? `${p.stock} in stock` : 'Out of stock'}</p>
            <button className="btn" disabled={p.stock === 0} onClick={() => addToCart(p.id)}>Add to cart</button>
          </div>
        ))}
        {products.length === 0 && <p className="muted">No products found.</p>}
      </div>

      <div className="pager">
        <button className="btn btn-outline" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
        <span>Page {pageInfo.totalPages === 0 ? 0 : page + 1} of {pageInfo.totalPages}</span>
        <button className="btn btn-outline" disabled={page + 1 >= pageInfo.totalPages} onClick={() => setPage(page + 1)}>Next</button>
      </div>
    </div>
  );
}
