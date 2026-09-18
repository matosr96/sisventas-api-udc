#!/usr/bin/env bash
# Aplica las migraciones de migrations/ en orden y deja constancia en schema_migrations.
#
#   ./scripts/db-migrate.sh status                      qué falta por aplicar
#   ./scripts/db-migrate.sh apply                       aplica lo pendiente
#   ./scripts/db-migrate.sh baseline 0001_....sql       marca como aplicada sin ejecutarla
#
# Conexión por entorno: DB_NAME, DB_USER, DB_PASSWORD, DB_HOST, DB_PORT. Requiere el cliente mysql.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MIGRATIONS_DIR="$ROOT/migrations"

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

ensure_registry() {
  mysql_exec --database="$DB_NAME" -e "
    CREATE TABLE IF NOT EXISTS schema_migrations (
      filename VARCHAR(255) PRIMARY KEY,
      applied_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ) ENGINE=InnoDB;"
}

is_applied() {
  local file="$1"
  local found
  found=$(mysql_exec --database="$DB_NAME" --skip-column-names --batch \
    -e "SELECT COUNT(*) FROM schema_migrations WHERE filename = '$file';")
  [ "$found" -gt 0 ]
}

list_migrations() {
  find "$MIGRATIONS_DIR" -maxdepth 1 -name '[0-9][0-9][0-9][0-9]_*.sql' | sort
}

command_status() {
  ensure_registry
  for path in $(list_migrations); do
    file="$(basename "$path")"
    if is_applied "$file"; then
      echo "  applied   $file"
    else
      echo "  PENDING   $file"
    fi
  done
}

command_apply() {
  ensure_registry
  for path in $(list_migrations); do
    file="$(basename "$path")"
    if is_applied "$file"; then
      continue
    fi
    echo "applying $file"
    # El SQL y el registro entran en la misma transacción: si el SQL falla, la
    # migración no queda marcada como aplicada.
    {
      echo "START TRANSACTION;"
      cat "$path"
      echo "INSERT INTO schema_migrations (filename) VALUES ('$file');"
      echo "COMMIT;"
    } | mysql_exec --database="$DB_NAME"
  done
  echo "done"
}

command_baseline() {
  local file="${1:-}"
  if [ -z "$file" ]; then
    echo "usage: db-migrate.sh baseline <file.sql>" >&2
    exit 1
  fi
  if [ ! -f "$MIGRATIONS_DIR/$file" ]; then
    echo "migrations/$file does not exist" >&2
    exit 1
  fi
  ensure_registry
  mysql_exec --database="$DB_NAME" \
    -e "INSERT IGNORE INTO schema_migrations (filename) VALUES ('$file');"
  echo "marked as applied: $file"
}

case "${1:-}" in
  status) command_status ;;
  apply) command_apply ;;
  baseline) command_baseline "${2:-}" ;;
  *)
    echo "usage: db-migrate.sh {status|apply|baseline <file.sql>}" >&2
    exit 1
    ;;
esac
