-- 0005_siembra_de_contadores.sql
-- Los contadores de documentos existen de antemano para que emitir un numero sea solo una
-- lectura con bloqueo. Crearlos en la transaccion del documento provocaba deadlocks
-- (INSERT IGNORE + FOR UPDATE) y en transaccion anidada agotaba el pool bajo carga. La API
-- ademas siembra el ano en curso y el siguiente al arrancar y cada dia, por si esta lista
-- se queda corta.

INSERT IGNORE INTO document_counters (id, last_number) VALUES
  ('F-2024', 0),
  ('P-2024', 0),
  ('F-2025', 0),
  ('P-2025', 0),
  ('F-2026', 0),
  ('P-2026', 0),
  ('F-2027', 0),
  ('P-2027', 0),
  ('F-2028', 0),
  ('P-2028', 0),
  ('F-2029', 0),
  ('P-2029', 0),
  ('F-2030', 0),
  ('P-2030', 0),
  ('F-2031', 0),
  ('P-2031', 0),
  ('F-2032', 0),
  ('P-2032', 0),
  ('F-2033', 0),
  ('P-2033', 0),
  ('F-2034', 0),
  ('P-2034', 0),
  ('F-2035', 0),
  ('P-2035', 0),
  ('F-2036', 0),
  ('P-2036', 0),
  ('F-2037', 0),
  ('P-2037', 0),
  ('F-2038', 0),
  ('P-2038', 0),
  ('F-2039', 0),
  ('P-2039', 0),
  ('F-2040', 0),
  ('P-2040', 0);
