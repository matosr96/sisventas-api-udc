#!/usr/bin/env bash
# Aplica las migraciones de migraciones/ en orden y deja constancia en schema_migrations.
#
#   ./scripts/db-migrate.sh status                      que falta por aplicar
#   ./scripts/db-migrate.sh apply                       aplica lo pendiente
#   ./scripts/db-migrate.sh baseline 0001_....sql       marca como aplicada sin ejecutarla
#
# Conexion por entorno: DB_NAME, DB_USER, DB_PASSWORD, DB_HOST, DB_PORT.
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIR_MIGRACIONES="$RAIZ/migraciones"

DB_NAME="${DB_NAME:-bdsisventas}"
DB_USER="${DB_USER:-root}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"

mysql_exec() {
  if [ -n "${DB_PASSWORD:-}" ]; then
    mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --password="$DB_PASSWORD" "$@"
  else
    mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" "$@"
  fi
}

asegurar_registro() {
  mysql_exec --database="$DB_NAME" -e "
    CREATE TABLE IF NOT EXISTS schema_migrations (
      filename VARCHAR(255) PRIMARY KEY,
      applied_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ) ENGINE=InnoDB;"
}

ya_aplicada() {
  local archivo="$1"
  local encontrada
  encontrada=$(mysql_exec --database="$DB_NAME" --skip-column-names --batch \
    -e "SELECT COUNT(*) FROM schema_migrations WHERE filename = '$archivo';")
  [ "$encontrada" -gt 0 ]
}

listar() {
  find "$DIR_MIGRACIONES" -maxdepth 1 -name '[0-9][0-9][0-9][0-9]_*.sql' | sort
}

comando_status() {
  asegurar_registro
  for ruta in $(listar); do
    archivo="$(basename "$ruta")"
    if ya_aplicada "$archivo"; then
      echo "  aplicada   $archivo"
    else
      echo "  PENDIENTE  $archivo"
    fi
  done
}

comando_apply() {
  asegurar_registro
  for ruta in $(listar); do
    archivo="$(basename "$ruta")"
    if ya_aplicada "$archivo"; then
      continue
    fi
    echo "aplicando $archivo"
    # El SQL y el registro entran en la misma transaccion: si el SQL falla, la
    # migracion no queda marcada como aplicada.
    {
      echo "START TRANSACTION;"
      cat "$ruta"
      echo "INSERT INTO schema_migrations (filename) VALUES ('$archivo');"
      echo "COMMIT;"
    } | mysql_exec --database="$DB_NAME"
  done
  echo "listo"
}

comando_baseline() {
  local archivo="${1:-}"
  if [ -z "$archivo" ]; then
    echo "uso: db-migrate.sh baseline <archivo.sql>" >&2
    exit 1
  fi
  if [ ! -f "$DIR_MIGRACIONES/$archivo" ]; then
    echo "no existe migraciones/$archivo" >&2
    exit 1
  fi
  asegurar_registro
  mysql_exec --database="$DB_NAME" \
    -e "INSERT IGNORE INTO schema_migrations (filename) VALUES ('$archivo');"
  echo "marcada como aplicada: $archivo"
}

case "${1:-}" in
  status) comando_status ;;
  apply) comando_apply ;;
  baseline) comando_baseline "${2:-}" ;;
  *)
    echo "uso: db-migrate.sh {status|apply|baseline <archivo.sql>}" >&2
    exit 1
    ;;
esac
