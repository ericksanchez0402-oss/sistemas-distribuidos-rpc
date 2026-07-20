#!/usr/bin/env bash
# cotizar.cgi — Servicio remoto: cotización de divisa a MXN
set -euo pipefail
 
decode() { printf '%b' "${1//%/\\x}"; }
param()  { echo "${1}" | sed -n "s/.*${2}=\([^&]*\).*/\1/p"; }
 
qs="${QUERY_STRING:-}"
if [ "${REQUEST_METHOD:-GET}" = POST ]; then
  qs=$(head -c "${CONTENT_LENGTH:-0}")
fi
 
divisa=$(decode "$(param "$qs" divisa)")
monto=$(decode "$(param "$qs" monto)")
idemp="${HTTP_IDEMPOTENCY_KEY:-}"
 
declare -A tasa=( [USD]=17.25 [EUR]=18.40 [JPY]=0.117 [CAD]=12.70 [BRL]=3.55 )
 
if [ -z "$divisa" ] || [ -z "$monto" ]; then
  printf 'Status: 400 Bad Request\r\n'
  printf 'Content-Type: application/json; charset=UTF-8\r\n\r\n'
  echo '{"error":"divisa y monto son obligatorios"}'
  exit 0
fi
 
if [ -z "${tasa[$divisa]:-}" ]; then
  printf 'Status: 422 Unprocessable Entity\r\n'
  printf 'Content-Type: application/json; charset=UTF-8\r\n\r\n'
  echo '{"error":"divisa no soportada"}'
  exit 0
fi
 
# Idempotencia trivial: cachear respuesta por clave
cache=/var/tmp/rpc-cache
mkdir -p "$cache"
if [ -n "$idemp" ] && [ -f "$cache/$idemp" ]; then
  printf 'Content-Type: application/json; charset=UTF-8\r\n\r\n'
  cat "$cache/$idemp"
  exit 0
fi
 
mxn=$(awk "BEGIN { printf \"%.4f\", $monto * ${tasa[$divisa]} }")
resp="{\"divisa\":\"$divisa\",\"monto\":$monto,\"tasa\":${tasa[$divisa]},\"mxn\":$mxn,\"fuente\":\"BANXICO-mock\",\"servidor\":\"$(hostname -s)\"}"
 
[ -n "$idemp" ] && echo "$resp" > "$cache/$idemp"
 
printf 'Content-Type: application/json; charset=UTF-8\r\n\r\n'
echo "$resp"
