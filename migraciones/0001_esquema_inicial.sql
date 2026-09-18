-- 0001_esquema_inicial.sql
-- Esquema completo de SisVentas.
--
-- Decisiones que conviene no reaprender por las malas:
--
-- * El dinero es DECIMAL(12,2), nunca DOUBLE: la coma flotante acumula error de
--   redondeo y esto factura.
-- * Una venta tiene LINEAS (sale_items) con cantidad y precio unitario congelado. Una
--   relacion N-N entre ventas y productos no puede expresar dos unidades del mismo
--   producto y obliga a leer el precio del catalogo, con lo que subir un precio
--   reescribiria el valor de todas las facturas pasadas.
-- * sales.total es la suma de los subtotales y lo calcula el servidor.
-- * products.created_by y categories.created_by son trazabilidad, no propiedad: nadie
--   filtra por ellos.
-- * status y roles.name tienen valores cerrados por CHECK. La matriz de autorizacion
--   compara contra roles.name, asi que un typo ahi seria un agujero de permisos.

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  photo VARCHAR(512),
  username VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NULL,
  CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(20) NOT NULL,
  CONSTRAINT uk_roles_name UNIQUE (name),
  CONSTRAINT ck_roles_name CHECK (name IN ('USER', 'ADMIN'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  icon VARCHAR(255),
  created_by BIGINT,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NULL,
  CONSTRAINT uk_categories_name UNIQUE (name),
  CONSTRAINT fk_categories_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(64) NOT NULL,
  name VARCHAR(120) NOT NULL,
  purchase_price DECIMAL(12, 2),
  sale_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  current_stock INT NOT NULL DEFAULT 0,
  -- Stock del alta. Dato historico: el sistema todavia no modela reposiciones.
  initial_stock INT,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  image VARCHAR(512),
  -- Umbral de reposicion, no una cantidad vendida.
  low_stock INT,
  category_id BIGINT,
  created_by BIGINT,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NULL,
  CONSTRAINT uk_products_sku UNIQUE (sku),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
  CONSTRAINT fk_products_created_by FOREIGN KEY (created_by) REFERENCES users (id),
  CONSTRAINT ck_products_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
  CONSTRAINT ck_products_stock CHECK (current_stock >= 0),
  INDEX idx_products_category (category_id),
  INDEX idx_products_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sales (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  -- Correlativo del ano, formato F-2026-000001. Lo entrega sale_counters.
  sale_number VARCHAR(20) NOT NULL,
  -- Fecha del negocio (puede ser retroactiva); created_at es cuando se registro.
  sale_date DATETIME(6) NOT NULL,
  total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  user_id BIGINT,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NULL,
  CONSTRAINT uk_sales_number UNIQUE (sale_number),
  CONSTRAINT fk_sales_user FOREIGN KEY (user_id) REFERENCES users (id),
  INDEX idx_sales_sale_date (sale_date)
) ENGINE=InnoDB;

-- Correlativo de facturas, una fila por año. El numero no puede derivarse del id:
-- con claves IDENTITY el id solo existe despues del INSERT y sale_number es NOT NULL.
CREATE TABLE IF NOT EXISTS sale_counters (
  year INT PRIMARY KEY,
  last_number BIGINT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sale_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sale_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  -- Precio congelado en el momento de la venta, no una lectura del catalogo.
  unit_price DECIMAL(12, 2) NOT NULL,
  subtotal DECIMAL(12, 2) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales (id),
  CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT ck_sale_items_quantity CHECK (quantity > 0),
  INDEX idx_sale_items_sale (sale_id),
  INDEX idx_sale_items_product (product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audits (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  method VARCHAR(10) NOT NULL,
  resource VARCHAR(255) NOT NULL,
  -- Nunca el cuerpo de la peticion: ahi viajan las contrasenas.
  detail VARCHAR(1000),
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_audits_username (username),
  INDEX idx_audits_created_at (created_at)
) ENGINE=InnoDB;

INSERT INTO roles (name) SELECT 'USER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');
INSERT INTO roles (name) SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');
