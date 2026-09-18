-- 0004_autoria_obligatoria.sql
-- Quien registra una venta, una compra o un asiento de stock siempre se conoce: la API
-- lo toma del token y nunca lo deja vacio. Las columnas eran nullable por descuido, lo
-- que permitia en teoria filas sin autor. Se cierra esa puerta.

ALTER TABLE sales MODIFY COLUMN user_id BIGINT NOT NULL;
ALTER TABLE purchases MODIFY COLUMN user_id BIGINT NOT NULL;
ALTER TABLE stock_movements MODIFY COLUMN user_id BIGINT NOT NULL;
