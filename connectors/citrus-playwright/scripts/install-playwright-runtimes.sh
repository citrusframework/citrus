#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

if [[ $# -gt 0 ]]; then
  PLAYWRIGHT_ARGS=("$@")
elif [[ -n "${PLAYWRIGHT_INSTALL_ARGS:-}" ]]; then
  read -r -a PLAYWRIGHT_ARGS <<< "${PLAYWRIGHT_INSTALL_ARGS}"
else
  PLAYWRIGHT_ARGS=(install chromium)
fi

cd "${REPO_ROOT}"

./mvnw -pl citrus-playwright \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="${PLAYWRIGHT_ARGS[*]}" \
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java
