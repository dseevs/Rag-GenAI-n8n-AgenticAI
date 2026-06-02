# Git & GitHub — setup and push guide

Complete instructions for publishing **Rag-GenAI-n8n-AgenticAI** (VirtuLab monorepo) to GitHub and pushing updates safely.

**Repository:** https://github.com/dseevs/Rag-GenAI-n8n-AgenticAI

---

## Table of contents

1. [Prerequisites](#1-prerequisites)
2. [Create the GitHub repository](#2-create-the-github-repository)
3. [Authentication (choose one)](#3-authentication-choose-one)
4. [First-time push from your machine](#4-first-time-push-from-your-machine)
5. [Cursor IDE: push without password prompt](#5-cursor-ide-push-without-password-prompt)
6. [Everyday workflow (after first push)](#6-everyday-workflow-after-first-push)
7. [Pre-push security checklist](#7-pre-push-security-checklist)
8. [What is excluded from Git](#8-what-is-excluded-from-git)
9. [Troubleshooting](#9-troubleshooting)
10. [Clone on another machine](#10-clone-on-another-machine)

---

## 1. Prerequisites

| Tool | Purpose |
|------|---------|
| [Git](https://git-scm.com/) | Version control |
| GitHub account | Remote hosting (`dseevs`) |
| SSH key **or** Personal Access Token (PAT) | Authenticate pushes |

Verify Git:

```bash
git --version
```

---

## 2. Create the GitHub repository

1. Sign in to GitHub.
2. Open **https://github.com/new**
3. Set:
   - **Repository name:** `Rag-GenAI-n8n-AgenticAI`
   - **Visibility:** Public or Private (your choice)
4. **Do not** check:
   - Add a README file
   - Add .gitignore
   - Choose a license  
   (This project already includes `README.md` and `.gitignore` at the root.)
5. Click **Create repository**.

---

## 3. Authentication (choose one)

GitHub does **not** accept your account password for `git push` over HTTPS. Use **SSH** (recommended) or a **Personal Access Token**.

### Option A — SSH (recommended)

Works well with Cursor and avoids password prompts after setup.

#### A1. Generate a key (skip if you already have `~/.ssh/id_ed25519`)

```bash
ssh-keygen -t ed25519 -C "your-email@example.com" -f ~/.ssh/id_ed25519 -N ""
```

#### A2. Add the public key to GitHub

```bash
cat ~/.ssh/id_ed25519.pub
```

Copy the full line (`ssh-ed25519 AAAA...`).

1. Open **https://github.com/settings/ssh/new**
2. **Title:** e.g. `my-laptop`
3. **Key:** paste the public key
4. **Add SSH key**

#### A3. Optional: SSH config

Create or edit `~/.ssh/config`:

```
Host github.com
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519
  IdentitiesOnly yes
```

#### A4. Test

```bash
ssh -T git@github.com
```

Expected: `Hi dseevs! You've successfully authenticated...`

#### A5. Set remote URL to SSH

```bash
cd ~/AgenticAi
git remote add origin git@github.com:dseevs/Rag-GenAI-n8n-AgenticAI.git
# If origin already exists:
git remote set-url origin git@github.com:dseevs/Rag-GenAI-n8n-AgenticAI.git
```

---

### Option B — HTTPS + Personal Access Token (PAT)

#### B1. Create a token

1. **https://github.com/settings/tokens**
2. **Generate new token** → **Generate new token (classic)**
3. Note: e.g. `virtulab-push`
4. Expiration: your choice
5. Scopes: enable **`repo`**
6. Generate and copy the token (`ghp_...`) — shown only once.

#### B2. Set remote URL to HTTPS

```bash
cd ~/AgenticAi
git remote add origin https://github.com/dseevs/Rag-GenAI-n8n-AgenticAI.git
# Or update:
git remote set-url origin https://github.com/dseevs/Rag-GenAI-n8n-AgenticAI.git
```

#### B3. Push and enter credentials

- **Username:** `dseevs`
- **Password:** paste the **`ghp_...` token** (not your GitHub login password)

#### B4. Save credentials (optional)

```bash
git config --global credential.helper store
```

After one successful push, credentials are stored in `~/.git-credentials`.

---

## 4. First-time push from your machine

Run from the **repository root** (folder containing `README.md`, `virtulab-platform/`, etc.).

### 4.1 Verify private files are ignored

```bash
cd ~/AgenticAi
git status
```

You should **not** see:

- `virtulab-platform/docs/`
- `virtulab-platform/rag-corpus/`
- `virtulab-lab/content/`
- `virtulab-frontend/.env.local`
- `*.plan.md`, `VIRTULAB_MASTER_LEARNING_PLATFORM_PLAN.md`

If any appear, check root [`.gitignore`](.gitignore) before continuing.

### 4.2 Initialize Git (new clone / first publish only)

Skip this section if the repo is already initialized and committed.

```bash
git init
git add .
git status
git commit -m "Initial commit: VirtuLab RAG, GenAI, n8n, and agentic platform"
git branch -M main
```

### 4.3 Add remote and push

```bash
# SSH (recommended):
git remote add origin git@github.com:dseevs/Rag-GenAI-n8n-AgenticAI.git

# OR HTTPS:
# git remote add origin https://github.com/dseevs/Rag-GenAI-n8n-AgenticAI.git

git push -u origin main
```

Success looks like:

```
 * [new branch]      main -> main
branch 'main' set up to track 'origin/main'.
```

---

## 5. Cursor IDE: push without password prompt

### Problem

In Cursor’s integrated terminal you may see:

```
Missing or invalid credentials.
Error: Bad status code: 401
... askpass-main.js ...
fatal: Authentication failed
```

Cursor’s Git **askpass** helper tries to authenticate silently and fails — it often **does not** show a username/password dialog.

### Solutions

**Best:** Use **SSH** (Section 3, Option A). After the key is on GitHub, `git push` works without prompts.

**If you must use HTTPS in Cursor**, disable askpass for that command:

```bash
cd ~/AgenticAi
env -u GIT_ASKPASS -u SSH_ASKPASS -u VSCODE_GIT_ASKPASS \
  GIT_TERMINAL_PROMPT=1 \
  git -c core.askPass= push -u origin main
```

When prompted: username `dseevs`, password = your **`ghp_...` PAT**.

**Alternative:** Use the system terminal (Ubuntu Terminal) instead of Cursor’s panel for the first authenticated push.

---

## 6. Everyday workflow (after first push)

```bash
cd ~/AgenticAi

# 1. See what changed
git status

# 2. Stage changes
git add .                    # all changes
# git add path/to/file       # or specific files

# 3. Commit
git commit -m "Describe what you changed"

# 4. Push
git push
```

### Useful commands

| Command | Purpose |
|---------|---------|
| `git status` | Uncommitted / unstaged files |
| `git diff` | Unstaged changes |
| `git diff --cached` | Staged changes |
| `git log --oneline -5` | Recent commits |
| `git pull` | Fetch and merge remote changes |
| `git remote -v` | Show remote URLs |

### Pull before push (if working on multiple machines)

```bash
git pull origin main
git push
```

---

## 7. Pre-push security checklist

Run before every commit (especially before sharing the repo publicly):

```bash
git status
git diff --cached
```

**Never commit:**

| Item | Risk |
|------|------|
| `.env`, `.env.local` | API keys, `AUTH_SECRET` |
| `deploy/k8s/secret.yaml` (real values) | DB passwords, JWT secrets |
| `rag-corpus/` | Course / proprietary content |
| `virtulab-lab/content/` | Lab scenarios |
| `virtulab-platform/docs/` | Internal runbooks, demo notes |
| `*.plan.md`, master plan files | Internal planning |
| `.cursor/`, agent transcripts | Tooling metadata |
| `node_modules/`, `target/`, `.next/` | Build artifacts (large, unnecessary) |

### Accidentally staged a secret?

```bash
git reset HEAD path/to/file
echo "path/to/file" >> .gitignore
git add .gitignore
git commit -m "chore: keep sensitive file out of repo"
```

If a secret was **already pushed**, rotate the credential immediately on GitHub/services and consider `git filter-repo` or GitHub secret scanning — do not only delete the file in a new commit.

---

## 8. What is excluded from Git

Root [`.gitignore`](.gitignore) enforces a **public-safe** repository. Private assets are documented in [`local-setup/README.md`](local-setup/README.md).

| Path | Reason |
|------|--------|
| `**/docs/` | Internal phase docs, test plans |
| `**/rag-corpus/` | RAG training markdown |
| `virtulab-lab/content/` | Lab content |
| `.env*` (except `.env.example`) | Secrets |
| `**/secret.yaml` | K8s dev secrets |
| `*.plan.md`, `*MASTER*PLAN*` | Planning artifacts |
| `.metadata/`, `.sixth/`, `.cursor/` | IDE / agent tooling |

---

## 9. Troubleshooting

### `Authentication failed` / `401`

- HTTPS: use a **PAT**, not your GitHub password.
- SSH: ensure public key is added at https://github.com/settings/keys
- Run `ssh -T git@github.com` to test SSH.

### `remote: Repository not found`

- Repo name or owner typo in `git remote -v`
- You lack access to the repository
- Repo not created yet on GitHub

### `failed to push some refs` / `non-fast-forward`

Someone else (or GitHub) changed `main`. Pull first:

```bash
git pull origin main --rebase
git push
```

### `remote origin already exists`

```bash
git remote set-url origin git@github.com:dseevs/Rag-GenAI-n8n-AgenticAI.git
```

### Push is huge or slow

Ensure `node_modules/`, `target/`, `.next/` are ignored:

```bash
git check-ignore -v virtulab-frontend/node_modules
```

### Cursor still blocks HTTPS login

Switch to SSH:

```bash
git remote set-url origin git@github.com:dseevs/Rag-GenAI-n8n-AgenticAI.git
git push -u origin main
```

---

## 10. Clone on another machine

```bash
# SSH
git clone git@github.com:dseevs/Rag-GenAI-n8n-AgenticAI.git
cd Rag-GenAI-n8n-AgenticAI

# HTTPS
# git clone https://github.com/dseevs/Rag-GenAI-n8n-AgenticAI.git
```

Then add **local-only** folders (not in Git):

- `virtulab-platform/rag-corpus/`
- `virtulab-lab/content/`
- `virtulab-frontend/.env.local` (from `.env.example`)
- Optional: private copy of `virtulab-platform/docs/`

See [`local-setup/README.md`](local-setup/README.md).

---

## Quick reference

```bash
# One-time (already done if you pushed successfully)
git init && git add . && git commit -m "Initial commit"
git branch -M main
git remote add origin git@github.com:dseevs/Rag-GenAI-n8n-AgenticAI.git
git push -u origin main

# Routine
git add .
git commit -m "your message"
git push
```

**Live repo:** https://github.com/dseevs/Rag-GenAI-n8n-AgenticAI
