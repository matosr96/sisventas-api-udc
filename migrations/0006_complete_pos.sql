-- 0006_complete_pos.sql
-- Lo que le faltaba al punto de venta para ser un sistema completo.
--
-- * Cobro: sales guarda como se pago (efectivo, tarjeta, transferencia), cuanto entrego el
--   cliente y el cambio; el subtotal, el descuento y el impuesto se guardan por separado del
--   total para que la factura se explique sola. Las ventas anteriores quedan con
--   subtotal = total, sin descuento ni impuesto y pagadas en efectivo.
-- * Devoluciones parciales: sale_returns / sale_return_items. Cada linea devuelta vuelve al
--   stock con un asiento SALE_RETURN y sale_items.returned_quantity lleva la cuenta para que
--   nunca se devuelva mas de lo vendido.
-- * users.token_version: subirlo invalida todos los tokens emitidos (cerrar sesion en todos
--   los dispositivos, reinicio de contrasena por un administrador).

ALTER TABLE sales
  ADD COLUMN subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER sale_date,
  ADD COLUMN discount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER subtotal,
  ADD COLUMN tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00 AFTER discount,
  ADD COLUMN tax DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER tax_rate,
  ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH' AFTER total,
  ADD COLUMN amount_paid DECIMAL(12, 2) NULL AFTER payment_method,
  ADD COLUMN change_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER amount_paid,
  ADD COLUMN customer_name VARCHAR(120) NULL AFTER change_amount,
  ADD CONSTRAINT ck_sales_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'TRANSFER'));

UPDATE sales SET subtotal = total WHERE subtotal = 0.00;

ALTER TABLE sale_items
  ADD COLUMN returned_quantity INT NOT NULL DEFAULT 0 AFTER quantity;

CREATE TABLE IF NOT EXISTS sale_returns (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  return_number VARCHAR(20) NOT NULL,
  sale_id BIGINT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  user_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uk_sale_returns_number UNIQUE (return_number),
  CONSTRAINT fk_sale_returns_sale FOREIGN KEY (sale_id) REFERENCES sales (id),
  CONSTRAINT fk_sale_returns_user FOREIGN KEY (user_id) REFERENCES users (id),
  INDEX idx_sale_returns_sale (sale_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sale_return_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sale_return_id BIGINT NOT NULL,
  sale_item_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(12, 2) NOT NULL,
  subtotal DECIMAL(12, 2) NOT NULL,
  CONSTRAINT fk_sale_return_items_return FOREIGN KEY (sale_return_id) REFERENCES sale_returns (id) ON DELETE CASCADE,
  CONSTRAINT fk_sale_return_items_sale_item FOREIGN KEY (sale_item_id) REFERENCES sale_items (id),
  CONSTRAINT fk_sale_return_items_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT ck_sale_return_items_quantity CHECK (quantity > 0)
) ENGINE=InnoDB;

ALTER TABLE stock_movements DROP CHECK ck_stock_movements_type;
ALTER TABLE stock_movements ADD CONSTRAINT ck_stock_movements_type CHECK (type IN
  ('INITIAL', 'PURCHASE', 'PURCHASE_VOID', 'SALE', 'SALE_VOID', 'SALE_RETURN', 'ADJUSTMENT'));

ALTER TABLE users ADD COLUMN token_version INT NOT NULL DEFAULT 0 AFTER status;

INSERT IGNORE INTO document_counters (id, last_number) VALUES
  ('R-2024', 0), ('R-2025', 0), ('R-2026', 0), ('R-2027', 0), ('R-2028', 0), ('R-2029', 0),
  ('R-2030', 0), ('R-2031', 0), ('R-2032', 0), ('R-2033', 0), ('R-2034', 0), ('R-2035', 0),
  ('R-2036', 0), ('R-2037', 0), ('R-2038', 0), ('R-2039', 0), ('R-2040', 0);
