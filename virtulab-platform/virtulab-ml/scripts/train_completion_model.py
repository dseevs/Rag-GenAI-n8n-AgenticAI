#!/usr/bin/env python3
"""
VirtuLab Phase 9 — offline completion model training (EDA + logistic regression).

Reads analytics.fact_progress + fact_ai from local Postgres, trains sklearn
LogisticRegression, prints AUC/accuracy, suggests weights for ml.model_registry.

Usage:
  pip install psycopg2-binary scikit-learn pandas
  python virtulab-ml/scripts/train_completion_model.py
"""
from __future__ import annotations

import json
import os

import pandas as pd
import psycopg2
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, roc_auc_score
from sklearn.model_selection import train_test_split

DB = dict(
    host=os.environ.get("DB_HOST", "localhost"),
    port=int(os.environ.get("DB_PORT", "5434")),
    dbname=os.environ.get("DB_NAME", "virtulab"),
    user=os.environ.get("DB_USER", "virtulab"),
    password=os.environ.get("DB_PASSWORD", "virtulab"),
)

SQL = """
WITH progress AS (
    SELECT attempt_id,
           MAX(progress_pct) AS max_progress_pct,
           COUNT(*) AS progress_event_count
    FROM analytics.fact_progress
    GROUP BY attempt_id
),
ai AS (
    SELECT attempt_id, COUNT(*) AS ai_event_count
    FROM analytics.fact_ai
    WHERE attempt_id IS NOT NULL
    GROUP BY attempt_id
)
SELECT p.attempt_id,
       COALESCE(p.max_progress_pct, 0) AS max_progress_pct,
       COALESCE(p.progress_event_count, 0) AS progress_event_count,
       COALESCE(a.ai_event_count, 0) AS ai_event_count,
       CASE WHEN COALESCE(p.max_progress_pct, 0) >= 100 THEN 1 ELSE 0 END AS completed
FROM progress p
LEFT JOIN ai a ON a.attempt_id = p.attempt_id
"""


def main() -> None:
    with psycopg2.connect(**DB) as conn:
        df = pd.read_sql(SQL, conn)

    if len(df) < 5:
        print(f"Only {len(df)} attempts — post more progress events first.")
        return

    features = ["max_progress_pct", "progress_event_count", "ai_event_count"]
    X = df[features]
    y = df["completed"]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)
    model = LogisticRegression(max_iter=1000)
    model.fit(X_train, y_train)

    proba = model.predict_proba(X_test)[:, 1]
    preds = (proba >= 0.5).astype(int)

    auc = roc_auc_score(y_test, proba) if y_test.nunique() > 1 else float("nan")
    acc = accuracy_score(y_test, preds)

    print(f"Samples: {len(df)}  Test AUC: {auc:.3f}  Accuracy: {acc:.3f}")
    weights = {
        "bias": float(model.intercept_[0]),
        "maxProgressPct": float(model.coef_[0][0]),
        "progressEventCount": float(model.coef_[0][1]),
        "aiEventCount": float(model.coef_[0][2]),
    }
    print("Suggested weights_json for ml.model_registry:")
    print(json.dumps(weights, indent=2))


if __name__ == "__main__":
    main()
