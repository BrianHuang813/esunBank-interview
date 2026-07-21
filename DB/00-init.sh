#!/usr/bin/env bash
set -e

for script_group in ddl procedures dml; do
  for sql_file in "/docker-entrypoint-initdb.d/${script_group}"/*.sql; do
    if [[ -f "${sql_file}" ]]; then
      docker_process_sql < "${sql_file}"
    fi
  done
done
