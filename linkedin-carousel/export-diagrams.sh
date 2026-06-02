#!/usr/bin/env bash
# Export Mermaid diagrams to PNG for LinkedIn carousel
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/exported"
mkdir -p "$OUT"

manual_instructions() {
  echo ""
  echo "Export manually:"
  echo "  1. Open https://mermaid.live"
  echo "  2. Paste each file from linkedin-carousel/diagrams/*.mmd"
  echo "  3. Export PNG → linkedin-carousel/exported/"
  echo ""
  echo "Or: npm install -g @mermaid-js/mermaid-cli && ./export-diagrams.sh"
}

export_one() {
  local f="$1"
  local base
  base=$(basename "$f" .mmd)
  local puppeteer_cfg="$ROOT/puppeteer-config.json"
  if command -v mmdc >/dev/null 2>&1; then
    mmdc -p "$puppeteer_cfg" -i "$f" -o "$OUT/${base}.png" -b white -w 1920 -H 1080 \
      || mmdc -i "$f" -o "$OUT/${base}.png"
  else
    npx -y @mermaid-js/mermaid-cli@11 -p "$puppeteer_cfg" -i "$f" -o "$OUT/${base}.png" -b white -w 1920 -H 1080
  fi
  if [[ -s "$OUT/${base}.png" ]]; then
    echo "  OK $OUT/${base}.png"
  else
    echo "  FAIL $base (use mermaid.live — see LINKEDIN_CAROUSEL.md)"
    rm -f "$OUT/${base}.png"
    return 1
  fi
}

if command -v mmdc >/dev/null 2>&1; then
  echo "Using mmdc..."
elif command -v npx >/dev/null 2>&1; then
  echo "Using npx @mermaid-js/mermaid-cli..."
else
  manual_instructions
  exit 0
fi

for f in "$ROOT"/diagrams/*.mmd; do
  export_one "$f" || echo "  WARN failed: $f"
done

echo ""
echo "Done. PNGs in: $OUT"
