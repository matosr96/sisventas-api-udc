-- 0002_inventory.sql
-- Reposiciones y compras a proveedor. Hasta aqui el stock solo bajaba al vender y subia
-- al anular, y products.current_stock se podia editar a mano en PUT /products: un numero
-- que cualquiera cambiaba sin dejar rastro.
--
-- * suppliers y purchases/purchase_items son el espejo de las ventas: lineas con costo
--   unitario congelado, total calculado por el servidor, inmutables salvo la fecha.
-- * stock_movements es el libro mayor: TODA variacion de current_stock deja exactamente
--   un asiento con el saldo resultante. El stock de un producto es la suma de sus asientos.
--   A partir de ahora current_stock no se edita: solo se mueve por compra, venta,
--   anulacion o ajuste con motivo.
-- * document_counters generaliza sale_counters para que ventas (F-) y compras (P-)
--   compartan el mecanismo de correlativo por ano.

CREATE TABLE IF NOT EXISTS document_counters (
  id VARCHAR(32) PRIMARY KEY,
  last_number BIGINT NOT NULL
) ENGINE=InnoDB;

INSERT INTO document_counters (id, last_number)
SELECT CONCAT('F-', counter_year), last_number FROM sale_counters
ON DUPLICATE KEY UPDATE last_number = VALUES(last_number);

DROP TABLE IF EXISTS sale_counters;

CREATE TABLE IF NOT EXISTS suppliers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  tax_id VARCHAR(50),
  phone VARCHAR(30),
  email VARCHAR(120),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_by BIGINT,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NULL,
  CONSTRAINT uk_suppliers_name UNIQUE (name),
  CONSTRAINT fk_suppliers_created_by FOREIGN KEY (created_by) REFERENCES users (id),
  CONSTRAINT ck_suppliers_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS purchases (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  purchase_number VARCHAR(20) NOT NULL,
  purchase_date DATETIME(6) NOT NULL,
  supplier_id BIGINT NOT NULL,
  total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  user_id BIGINT,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NULL,
  CONSTRAINT uk_purchases_number UNIQUE (purchase_number),
  CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
  CONSTRAINT fk_purchases_user FOREIGN KEY (user_id) REFERENCES users (id),
  INDEX idx_purchases_purchase_date (purchase_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS purchase_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  purchase_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_cost DECIMAL(12, 2) NOT NULL,
  subtotal DECIMAL(12, 2) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_purchase_items_purchase FOREIGN KEY (purchase_id) REFERENCES purchases (id),
  CONSTRAINT fk_purchase_items_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT ck_purchase_items_quantity CHECK (quantity > 0),
  INDEX idx_purchase_items_purchase (purchase_id),
  INDEX idx_purchase_items_product (product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS stock_movements (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  -- Con signo: positivo entra, negativo sale.
  quantity INT NOT NULL,
  stock_after INT NOT NULL,
  reference VARCHAR(30),
  reason VARCHAR(255),
  user_id BIGINT,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_stock_movements_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT ck_stock_movements_type CHECK (type IN
    ('INITIAL', 'PURCHASE', 'PURCHASE_VOID', 'SALE', 'SALE_VOID', 'ADJUSTMENT')),
  CONSTRAINT ck_stock_movements_quantity CHECK (quantity <> 0),
  CONSTRAINT ck_stock_movements_stock_after CHECK (stock_after >= 0),
  INDEX idx_stock_movements_product_created (product_id, created_at)
) ENGINE=InnoDB;
