# VirtuLab Lab App

Separate Next.js simulation UI — embeds in the platform shell via iframe.

## Run

```bash
npm install
npm run dev   # http://localhost:3002
```

## Env

```env
NEXT_PUBLIC_PLATFORM_GRAPHQL_URL=http://localhost:8097/graphql
NEXT_PUBLIC_PLATFORM_QUIZ_URL=http://localhost:8088
```

Integrated from shell at `/lab/[experimentId]` — see `virtulab-platform/docs/LAB_INTEGRATION.md`.
