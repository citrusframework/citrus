#!/bin/sh
# Usage: run-footprint.sh <module-dir> <classpath-file> <evidence-dir> [runs=5] [requests=1000]
# JAVA_HOME or VERTX_EVAL_JAVA_HOME must name a JDK 21 installation.
set -eu
script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec python3 "$script_dir/run-footprint.py" "$@"
