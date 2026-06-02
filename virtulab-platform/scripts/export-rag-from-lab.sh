#!/usr/bin/env bash
# Export RAG corpus markdown from virtulab-lab content/ into platform rag-corpus/
set -euo pipefail
ROOT="$(dirname "$0")/.."
LAB_ROOT="${1:-$(dirname "$ROOT")/virtulab-lab}"
EXPERIMENT_ID="${2:-v1-chemistry}"
SRC="$LAB_ROOT/content/$EXPERIMENT_ID"
DEST="$ROOT/rag-corpus/$EXPERIMENT_ID"

if [[ ! -d "$SRC" ]]; then
  echo "Source not found: $SRC"
  echo "Usage: $0 [path-to-virtulab-lab] [experimentId]"
  exit 1
fi

mkdir -p "$DEST"
cp -r "$SRC/"* "$DEST/"
echo "Exported $(find "$DEST" -type f | wc -l) files to $DEST"
