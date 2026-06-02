# SLIDE 6 — RAG pipeline (LLD)

## Headline
**LLD — How RAG works here**

## Steps
1. Markdown corpus (`rag-corpus/`)
2. Chunk + YAML metadata
3. **nomic-embed-text** → 768-dim vectors
4. Store in **pgvector**
5. On ask: retrieve top-K → prompt LLM → **citations**

## Diagram
**Export:** `diagrams/06-rag-pipeline.mmd` → PNG

## API
`POST /api/v1/rag/reindex` · `POST /api/v1/ai/ask`
