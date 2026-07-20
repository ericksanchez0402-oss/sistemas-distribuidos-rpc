#!/usr/bin/env bash
# calc.cgi — Reto Práctica 1: Calculadora remota con bc e Idempotencia
set -euo pipefail

# Función para extraer parámetros de la URL
param() { echo "${1:-}" | sed -n "s/.*${2}=\([^&]*\).*/\1/p"; }

qs="${QUERY_STRING:-}"
op=$(param "$qs" "op")
a=$(param "$qs" "a")
b=$(param "$qs" "b")
idemp="${HTTP_IDEMPOTENCY_KEY:-}"

# Verificación de idempotencia
cache=/var/tmp/rpc-calc-cache
mkdir -p "$cache"
if [ -n "$idemp" ] && [ -f "$cache/$idemp" ]; then
  printf 'Content-Type: application/json; charset=UTF-8\r\n\r\n'
  cat "$cache/$idemp"
  exit 0
fi

# Validar que los operandos no estén vacíos
if [ -z "$a" ] || [ -z "$b" ]; then
    printf 'Status: 400 Bad Request\r\nContent-Type: application/json\r\n\r\n{"error":"Faltan operandos"}'
    exit 0
fi

# Operaciones matemáticas usando 'bc'
res=""
case "$op" in
  sum) res=$(echo "$a + $b" | bc -l) ;;
  res) res=$(echo "$a - $b" | bc -l) ;;
  mul) res=$(echo "$a * $b" | bc -l) ;;
  div)
    if [ "$(echo "$b == 0" | bc -l)" -eq 1 ]; then
      printf 'Status: 400 Bad Request\r\nContent-Type: application/json\r\n\r\n{"error":"Division entre cero"}'
      exit 0
    fi
    res=$(echo "$a / $b" | bc -l)
    ;;
  *)
    printf 'Status: 400 Bad Request\r\nContent-Type: application/json\r\n\r\n{"error":"Operacion no soportada"}'
    exit 0
    ;;
esac

# Formatear salida JSON
json="{\"operacion\":\"$op\",\"a\":$a,\"b\":$b,\"resultado\":$res}"

# Guardar en caché si viene la llave de idempotencia
[ -n "$idemp" ] && echo "$json" > "$cache/$idemp"

# Imprimir respuesta
printf 'Content-Type: application/json; charset=UTF-8\r\n\r\n'
echo "$json"
