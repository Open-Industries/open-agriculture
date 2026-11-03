#!/usr/bin/env bash
# Lightweight syntax gate for the core bundle: every YAML and JSON file
# must parse. This mirrors the upstream Makefile `validate` step (which only
# checks JSON validity) and extends it to YAML. Run from the bundle root.
# Usage: tests/validate.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
fail=0
while IFS= read -r -d '' file; do
  case "$file" in
    *.yaml|*.yml)
      python3 -c "import sys, yaml; yaml.safe_load(open(sys.argv[1]))" "$file" \
        || { echo "INVALID YAML: $file"; fail=1; }
      ;;
    *.json)
      python3 -c "import sys, json; json.load(open(sys.argv[1]))" "$file" \
        || { echo "INVALID JSON: $file"; fail=1; }
      ;;
  esac
done < <(find "$ROOT" -type f \( -name '*.yaml' -o -name '*.yml' -o -name '*.json' \) \
  -not -path '*/target/*' -print0)
if [ "$fail" -eq 0 ]; then
  echo "validate: all YAML/JSON files parse"
else
  echo "validate: FAILED"
  exit 1
fi
