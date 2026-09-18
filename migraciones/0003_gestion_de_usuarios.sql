-- 0003_gestion_de_usuarios.sql
-- Gestion de usuarios por la API. Hasta aqui la unica forma de asignar un rol o de
-- retirar a alguien era editar la base a mano.
--
-- users.status permite desactivar una cuenta sin borrarla: el usuario firma ventas,
-- compras y asientos de stock, y ese historico no puede quedar huerfano. Un usuario
-- INACTIVE no puede iniciar sesion ni usar un token que aun no haya caducado.

ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE'));
