# E-Commerce Web Application

Java 17 · Spring Boot 3 · Spring Security (JWT) · Spring Data JPA/Hibernate · MySQL · React (Vite)

## Run it

### 1. Backend
Requirements: JDK 17+, Maven, MySQL running locally.

```bash
cd backend
# optional overrides (defaults: root/root on localhost:3306, DB auto-created)
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
mvn spring-boot:run
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html (click **Authorize**, paste the token from `/api/auth/login`)
- Seeded admin: `admin@shop.com` / `Admin@123` (change it!) plus sample categories and products.
- Tables are created automatically (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Frontend
Requirements: Node 18+.

```bash
cd frontend
npm install
npm run dev     # http://localhost:5173
```
Set `VITE_API_URL` (see `.env.example`) if the backend is not on `localhost:8080`.

## API overview

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Public |
| GET | `/api/products?keyword=&categoryId=&minPrice=&maxPrice=&sortBy=id\|name\|price&direction=asc\|desc&page=0&size=10` | Public |
| GET | `/api/products/{id}`, `/api/categories` | Public |
| POST/PUT/DELETE | `/api/products`, `/api/categories` | ADMIN |
| GET | `/api/cart` | USER |
| POST | `/api/cart/items` `{productId, quantity}` | USER |
| PUT/DELETE | `/api/cart/items/{productId}` | USER |
| POST | `/api/orders` `{shippingAddress}` (checkout) | USER |
| GET | `/api/orders`, `/api/orders/{id}` | Owner / ADMIN |
| PUT | `/api/orders/{id}/cancel` (PENDING only) | Owner / ADMIN |
| GET | `/api/admin/orders` | ADMIN |
| PUT | `/api/admin/orders/{id}/status?status=SHIPPED` | ADMIN |

## Where each resume bullet lives

| Resume bullet | Code |
|---|---|
| JWT auth + role-based authorization | `security/*`, `config/SecurityConfig.java` |
| JPA/Hibernate entities & relationships | `entity/*` |
| Search, filter, sort, pagination | `ProductService.search` (JPA `Specification`), `PageResponse` |
| Validation + global exception handling | `Dtos` (Jakarta validation), `exception/GlobalExceptionHandler.java` |
| Transactional order & cart workflows | `OrderService.placeOrder`, `CartService` |
| Swagger/OpenAPI | `config/OpenApiConfig.java` |

## Notes
- Public registration always creates `USER` accounts; admins come from the seeder or direct DB changes.
- Checkout runs in one transaction with pessimistic row locks on products, so stock can't be oversold and failures roll back cleanly.
- For production: set `JWT_SECRET` (Base64, 256+ bits), DB credentials and `CORS_ORIGINS` via environment variables, and switch `ddl-auto` to `validate` with Flyway/Liquibase migrations.
