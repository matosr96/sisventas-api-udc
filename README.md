# SisVentas — Sales management API

[![Java CI/CD](https://github.com/matosr96/sisventas-api/actions/workflows/ci.yml/badge.svg)](https://github.com/matosr96/sisventas-api/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

REST API for a small point of sale: product catalog, sales with stock control, and a trail of
who did what.

This is a **personal project**. It is not deployed anywhere and serves no users. Its purpose is
to be a reference implementation of one specific architecture standard — one file per
operation, layers with a single reason to change, errors as domain codes, schema governed by
hand-written migrations — applied end to end in a domain that hurts when modeled badly:
invoicing.

> Code is in English; comments and Javadoc are in Spanish.

## What it does

- **Catalog**: products with a unique SKU, purchase and sale price, grouped into categories.
- **Sales with line items**: every sale records what was sold, how many units, and **the price
  at the time of the sale**. Raising a price does not alter invoices already issued.
- **Purchases from suppliers**: the mirror of sales — line items with a frozen unit cost,
  server-computed total, per-year numbering (`P-2026-000001`).
- **A stock ledger**: every change to a product's stock — initial load, purchase, sale, void,
  manual adjustment — leaves exactly one entry with the resulting balance. Stock is never
  edited by hand; it is the sum of its entries.
- **Per-year invoice numbering** (`F-2026-000001`) and **invoice PDF** (`GET /sales/{id}/pdf`).
- **Users and roles** (`USER`, `ADMIN`) with JWT and a single authorization matrix, plus
  admin endpoints to list users, assign roles and deactivate accounts.
- **Automatic auditing** of every successful write, with no work from the routes.
- **Uniform contracts**: `{ count, page, pages, items }` for pagination and
  `{ "message": "<code>" }` for every error.
- **OpenAPI/Swagger** at `/swagger-ui.html`.

### Still out of scope

Stated plainly, so the scope is not overread:

- **Hosting.** Every green build on `main` publishes the image to GHCR, but nothing runs it:
  there is no server, no environment, no URL.
- **Invoice branding** beyond the business name: no logo, tax breakdown or template.
- **Password recovery.** Users change their own password with the current one; there is no
  reset by email.

## Quick start

With Docker, which brings MySQL along:

```bash
docker compose up -d db
./scripts/db-migrate.sh apply
./mvnw spring-boot:run
```

Without Docker, against your own MySQL 8:

```bash
export DB_URL="jdbc:mysql://localhost:3306/bdsisventas"
export DB_USERNAME=root DB_PASSWORD=...
export JWT_SECRET="a_string_of_at_least_48_bytes_long_no_default_exists"
./scripts/db-migrate.sh apply
./mvnw spring-boot:run
```

Check that it is alive and get a token:

```bash
curl -s localhost:8080/actuator/health

TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Edgar","lastName":"Matos","username":"matos","password":"a-strong-password"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["accessToken"])')

curl -s localhost:8080/api/v1/products -H "Authorization: Bearer $TOKEN"
```

A new user gets the `USER` role: it can read and register sales. Touching the catalog requires
`ADMIN`, which is granted in the `users_roles` table.

Configuration comes from the environment, never from the repository: `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD`, `JWT_SECRET` (**required, no default; at least 48 bytes**), `CORS_ORIGINS`, `PORT`.
The app refuses to start without a valid `JWT_SECRET`: there is no fallback, because a signing
key in a public repository would let anyone forge tokens.

## Running the published image

Every green build on `main` publishes `ghcr.io/matosr96/sisventas-api` tagged `latest` and
`sha-<commit>`; a `vX.Y.Z` tag also publishes `X.Y.Z`.

```bash
docker run --rm -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/bdsisventas" \
  -e DB_USERNAME=root -e DB_PASSWORD=... \
  -e JWT_SECRET="a_string_of_at_least_48_bytes_long_no_default_exists" \
  ghcr.io/matosr96/sisventas-api:latest
```

The image does not migrate the database: apply `migrations/` first, exactly as in local
development.

## Stack

Java 17 · Spring Boot 3.3 · Spring Security with JWT (jjwt 0.11.5) · Spring Data JPA · MySQL 8 ·
springdoc-openapi · OpenPDF · Lombok.

Tooling: Maven (wrapper included), Docker Compose, GitHub Actions, Checkstyle, JaCoCo.

Requirements: Java 17+ and MySQL 8, or just Docker.

## Project structure

Layered architecture with **one file per operation**. Package names mirror the reference
standard rather than Spring's usual vocabulary, so moving between projects has no friction.

```
src/main/java/com/api/sisventas/
├── server/                 # Cross-cutting: GlobalErrorHandler, AuditFilter, OpenApiConfig
├── security/               # JWT, CORS and the authorization matrix (SecurityConfig)
├── routes/<entity>/        # HTTP layer: CreateProductRoute, ListProductsRoute, ...
├── businessLogic/<entity>/ # Domain: CreateProduct, ListProducts, ... (execute method)
├── models/                 # JPA entities
│   └── dtos/<entity>/      # API contract: *Request and *Response (records)
├── dataSources/            # Spring Data repositories + SqlErrors
└── common/                 # DomainError, ErrorCodes, PaginatedResponse, Pagination, Authenticated

migrations/                # Hand-written, numbered SQL (ddl-auto=validate)
scripts/db-migrate.sh       # Applies and tracks migrations
```

The rules that hold the structure together:

- A JPA entity **never** crosses the HTTP boundary: a `*Request` comes in, a `*Response` goes out.
- Routes have no `try/catch`: `GlobalErrorHandler` turns every error into the same contract.
- Authorization lives only in `SecurityConfig`; every new route is protected by default.
- The schema is governed by migrations, never by Hibernate.

## Database and migrations

The schema is written by hand in `migrations/` and `spring.jpa.hibernate.ddl-auto=validate`:
if an entity and its table disagree, the application refuses to start.

```bash
./scripts/db-migrate.sh status    # what is applied and what is pending
./scripts/db-migrate.sh apply     # applies what is pending, in order
```

Requires the `mysql` client and takes its connection from `DB_NAME`, `DB_USER`, `DB_PASSWORD`,
`DB_HOST` and `DB_PORT`. Each file is recorded in `schema_migrations` inside the same
transaction as the SQL it runs, so a failed migration is never marked as applied.

A database created before the model was reworked is recreated, not migrated:

```bash
docker compose exec -T db mysql -uroot -proot \
  -e "DROP DATABASE IF EXISTS bdsisventas; CREATE DATABASE bdsisventas;"
./scripts/db-migrate.sh apply
```

## API

Base URL `http://localhost:8080/api/v1`. JWT in `Authorization: Bearer <token>`, JSON fields in
`camelCase`, interactive docs at `/swagger-ui.html` and the OpenAPI spec at `/api-docs`.

### Common contracts

Paginated lists — `?page=` (1-based) and `?limit=` (10 by default):

```json
{ "count": 42, "page": 1, "pages": 5, "items": [] }
```

Errors — always the same body, with the domain code as the message:

```json
{ "message": "601" }
```

### Error codes

| Code | Meaning | HTTP |
| --- | --- | --- |
| 601 | Product not found | 404 |
| 602 | Category not found | 404 |
| 603 | Sale not found | 404 |
| 604 | User not found | 404 |
| 605 | Supplier not found | 404 |
| 606 | Purchase not found | 404 |
| 607 | Product is inactive and cannot be sold or purchased | 409 |
| 610 | Username already taken | 409 |
| 611 | Invalid credentials | 401 |
| 612 | Default role (`USER`) missing from the database | 500 |
| 613 | Not allowed on this resource | 403 |
| 614 | You cannot change your own role or status | 409 |
| 615 | Current password is wrong | 400 |
| 616 | Role missing from the database | 500 |
| 620 | Category still has products and cannot be deleted | 409 |
| 621 | Not enough stock (sale, adjustment or purchase void) | 409 |
| 622 | SKU already exists | 409 |
| 623 | Category name already exists | 409 |
| 624 | Supplier name already exists | 409 |
| 625 | Supplier is inactive | 409 |
| 630 | Invalid pagination parameters | 400 |
| 631 | Invalid request (validation failed, malformed body, wrong type or missing parameter) | 400 |
| 640 | Too many sign-in or sign-up attempts from this address | 429 |
| 690 | Referential integrity violation | 409 |
| 699 | Unhandled internal error | 500 |

Adding a code means touching `common/ErrorCodes` and this table in the same commit.

### Endpoints

**Authentication** (public)

- `POST /api/v1/auth/signup` — registers a user with the `USER` role and returns their token
- `POST /api/v1/auth/signin` — authenticates and returns the access token

**Products**

- `GET /api/v1/products` — list, paginated — authenticated
- `GET /api/v1/products/{id}` — read one — authenticated
- `POST /api/v1/products` — create, with `initialStock` — `ADMIN`
- `PUT /api/v1/products/{id}` — partial update; stock is **not** editable here — `ADMIN`
- `DELETE /api/v1/products/{id}` — delete or deactivate — `ADMIN`
- `GET /api/v1/products/{id}/movements` — stock ledger, paginated — authenticated
- `POST /api/v1/products/{id}/adjustments` — manual correction with a mandatory reason — `ADMIN`

**Categories**

- `GET /api/v1/categories` — list, paginated — authenticated
- `GET /api/v1/categories/{id}` — read one — authenticated
- `POST /api/v1/categories` — create — `ADMIN`
- `PUT /api/v1/categories/{id}` — partial update — `ADMIN`
- `DELETE /api/v1/categories/{id}` — delete — `ADMIN`

**Sales**

- `GET /api/v1/sales` — list, paginated — authenticated
- `GET /api/v1/sales/{id}` — read one, with its line items — authenticated
- `POST /api/v1/sales` — register — `USER` or `ADMIN`
- `PUT /api/v1/sales/{id}` — correct **the date only** — `USER` or `ADMIN`
- `DELETE /api/v1/sales/{id}` — void the sale and return the stock — `ADMIN`
- `GET /api/v1/sales/{id}/pdf` — the invoice as `application/pdf` — authenticated

**Suppliers**

- `GET /api/v1/suppliers` — list, paginated — authenticated
- `GET /api/v1/suppliers/{id}` — read one — authenticated
- `POST /api/v1/suppliers` — create — `ADMIN`
- `PUT /api/v1/suppliers/{id}` — partial update, including `status` — `ADMIN`
- `DELETE /api/v1/suppliers/{id}` — delete, or deactivate if it has purchases — `ADMIN`

**Purchases**

- `GET /api/v1/purchases` — list, paginated — authenticated
- `GET /api/v1/purchases/{id}` — read one, with its line items — authenticated
- `POST /api/v1/purchases` — register; adds stock and freezes the unit cost — `ADMIN`
- `PUT /api/v1/purchases/{id}` — correct **the date only** — `ADMIN`
- `DELETE /api/v1/purchases/{id}` — void; refused if the goods were already sold — `ADMIN`

```json
POST /api/v1/purchases
{ "supplierId": 1, "items": [ { "productId": 1, "quantity": 20, "unitCost": 1100.50 } ] }
```

**Users**

- `GET /api/v1/users/me` — own profile — authenticated
- `PUT /api/v1/users/me/password` — change own password; requires the current one — authenticated
- `GET /api/v1/users` — list, paginated — `ADMIN`
- `GET /api/v1/users/{id}` — read one — `ADMIN`
- `PUT /api/v1/users/{id}/roles` — replace the role set — `ADMIN`
- `PUT /api/v1/users/{id}/status` — `ACTIVE` / `INACTIVE` — `ADMIN`

A sale is registered with its line items; price and total are set by the server:

```json
POST /api/v1/sales
{ "items": [ { "productId": 1, "quantity": 3 } ] }
```

```json
{
  "id": 1,
  "saleNumber": "F-2026-000001",
  "saleDate": "2026-09-18T14:37:21.153220Z",
  "total": 7502.25,
  "items": [
    { "productId": 1, "productSku": "COCA-350", "quantity": 3,
      "unitPrice": 2500.75, "subtotal": 7502.25 }
  ]
}
```

## Domain rules

Model decisions that are not obvious from the endpoints alone:

- **A sale is immutable except for its date.** `unitPrice` is frozen when the sale is
  registered, so raising a product's price leaves issued invoices untouched. Correcting a sale
  means voiding it (which returns the stock) and registering it again.
- **The server computes the total** by summing the line subtotals. The client never sends it.
- **Selling decrements stock** inside the same transaction: one line without stock rolls back
  the whole sale with code 621.
- **Money is `DECIMAL(12,2)`**, never floating point.
- **A product with ledger entries is deactivated, not deleted** (`status: INACTIVE`): deleting it
  would destroy the entries and line items that reference it. Hard deletes are reserved for
  products that never moved. An inactive product cannot be sold or purchased (607).
- **`purchasePrice` is the last cost paid**: every registered purchase updates it. It is null
  until the first purchase.
- **A category with products cannot be deleted** (code 620).
- **`createdBy` is traceability, not ownership**: nothing filters by it. Every authenticated
  user sees the whole catalog.
- **`saleDate` is the business date** and may be backdated; `createdAt` is when the sale was
  recorded in the system.
- **Stock is a ledger, not a field.** Every change to `currentStock` goes through one
  service that locks the product row first (`SELECT ... FOR UPDATE`), so concurrent sales
  never oversell nor leave contradictory balances, and leaves exactly one `stock_movements` entry (`INITIAL`, `PURCHASE`,
  `PURCHASE_VOID`, `SALE`, `SALE_VOID`, `ADJUSTMENT`) with the balance after it. The balance
  never goes negative: a sale, an adjustment or a purchase void that would take it below zero
  is refused with 621. `initialStock` is simply the `INITIAL` entry.
- **A purchase is immutable except for its date**, like a sale. Voiding it pulls its units
  back out, which is why it is refused once they have been sold.
- **A supplier with purchases is deactivated, not deleted**, and an inactive supplier cannot
  receive new purchases (625).
- **Accounts are deactivated, never deleted**: they sign sales, purchases and ledger entries.
  An `INACTIVE` account cannot sign in and cannot keep using a token that has not expired.
- **Sale and purchase dates may be backdated but never in the future.**
- **Nobody locks themselves out** (614): an admin cannot drop their own `ADMIN` role nor
  deactivate their own account. Changing your own password requires the current one.

Every successful write is recorded in the `audits` table with user, method and resource.

## Tests

```bash
./mvnw test                 # boots the context against in-memory H2: no MySQL or Docker needed
./mvnw package              # compiles, runs Checkstyle and the test suite
./mvnw test jacoco:report   # coverage report in target/site/jacoco
```

Thirteen tests: the stock ledger invariants, a concurrency test (twenty simultaneous sales
over ten units must accept exactly ten), the user-management guards, and HTTP-level tests of
the authorization matrix, the 401/403 contract, token rejection for deactivated accounts,
auditing, the invoice PDF and the rate limits. Migrations do **not** run during tests, so
a mistake in the SQL only shows up when starting against a real MySQL, where `ddl-auto=validate`
compares the schema against the entities.

## Quality and CI

Checkstyle runs in the `validate` phase of every build and the project sits at zero warnings.
JaCoCo produces the coverage report after the tests. The SonarQube plugin is declared in the
`pom.xml` for anyone who wants to point it at their own server (`./mvnw sonar:sonar`), but CI
does not run it.

The GitHub Actions workflow builds with Maven, runs the tests, uploads the results, builds the
Docker image and runs a Snyk security scan (needs the `SNYK_TOKEN` secret; without it, that step
fails).

## Related documentation

- [docs/DOCKER.md](docs/DOCKER.md) — container details.
- [docs/SWAGGER.md](docs/SWAGGER.md) — API documentation.
- [CHANGELOG.md](CHANGELOG.md) — change log.
- [CONTRIBUTING.md](CONTRIBUTING.md) — how to propose changes.

## License

MIT — see [LICENSE](LICENSE).
