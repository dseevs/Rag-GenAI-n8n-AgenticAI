"use client";

import { useEffect, useMemo, useState } from "react";
import {
  emitProgress,
  listenForSession,
  parseHandoff,
  trackTabView,
  type LabSession,
} from "@/lib/session-handoff";
import { recordProgress, submitQuiz } from "@/lib/platform-client";

const QUESTIONS = [
  { id: "q1", prompt: "What pH indicates a neutral solution?", placeholder: "e.g. 7" },
  { id: "q2", prompt: "Acids donate which ion?", placeholder: "e.g. H+" },
  { id: "q3", prompt: "Bases accept protons — true or false?", placeholder: "true / false" },
];

type Tab = "theory" | "procedure" | "simulation" | "quiz";

export function LabShell({
  experimentId,
  searchParams,
}: {
  experimentId: string;
  searchParams: Record<string, string | string[] | undefined>;
}) {
  const params = useMemo(() => {
    const sp = new URLSearchParams();
    Object.entries(searchParams).forEach(([k, v]) => {
      if (typeof v === "string") sp.set(k, v);
    });
    return sp;
  }, [searchParams]);

  const [session, setSession] = useState<LabSession | null>(null);
  const [tab, setTab] = useState<Tab>("theory");
  const [simStep, setSimStep] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [quizResult, setQuizResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const partial = parseHandoff(params);
    if (partial.attemptId) {
      setSession((prev) =>
        prev ?? {
          ...partial,
          experimentId: partial.experimentId || experimentId,
          accessToken: "",
        },
      );
    }
    return listenForSession(setSession);
  }, [params, experimentId]);

  async function reportProgress(stepId: string, progress: Record<string, unknown>) {
    if (!session?.accessToken) return;
    try {
      await recordProgress(session.accessToken, {
        experimentId: session.experimentId,
        attemptId: session.attemptId,
        stepId,
        progress,
      });
    } catch (e) {
      setError(e instanceof Error ? e.message : "Progress failed");
    }
    emitProgress(session, stepId, progress);
  }

  function selectTab(next: Tab) {
    setTab(next);
    if (session) trackTabView(session, next);
    void reportProgress(`tab-${next}`, { tab: next, percentage: tabProgress(next) });
  }

  function tabProgress(t: Tab): number {
    const map: Record<Tab, number> = { theory: 25, procedure: 50, simulation: 75, quiz: 100 };
    return map[t];
  }

  async function advanceSimulation() {
    const next = Math.min(simStep + 1, 3);
    setSimStep(next);
    await reportProgress(`sim-step-${next}`, {
      tab: "simulation",
      percentage: 50 + next * 8,
      step: next,
    });
  }

  async function handleQuizSubmit() {
    if (!session?.accessToken) {
      setError("Waiting for session token from platform shell…");
      return;
    }
    try {
      const result = await submitQuiz(session.accessToken, {
        attemptId: session.attemptId,
        experimentId: session.experimentId,
        answers: QUESTIONS.map((q) => ({ questionId: q.id, answer: answers[q.id] ?? "" })),
      });
      setQuizResult(`Score: ${result.score}% (${result.correctCount}/${result.totalQuestions})`);
      await reportProgress("quiz-complete", { tab: "quiz", percentage: 100, score: result.score });
    } catch (e) {
      setError(e instanceof Error ? e.message : "Quiz submit failed");
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-4xl flex-col">
      <header className="border-b border-zinc-200 bg-white px-6 py-4">
        <p className="text-xs uppercase tracking-wide text-emerald-700">VirtuLab simulation</p>
        <h1 className="text-xl font-semibold">{experimentId}</h1>
        <p className="mt-1 text-sm text-zinc-600">
          Mode: {session?.mode ?? "…"} · Attempt: {session?.attemptId || "waiting for handoff"}
        </p>
      </header>

      <nav className="flex gap-1 border-b border-zinc-200 bg-white px-4 py-2">
        {(["theory", "procedure", "simulation", "quiz"] as Tab[]).map((t) => (
          <button
            key={t}
            type="button"
            onClick={() => selectTab(t)}
            className={`rounded-lg px-3 py-2 text-sm capitalize ${
              tab === t ? "bg-emerald-600 text-white" : "text-zinc-600 hover:bg-zinc-100"
            }`}
          >
            {t}
          </button>
        ))}
      </nav>

      <main className="flex-1 px-6 py-8">
        {error ? (
          <p className="mb-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        ) : null}

        {tab === "theory" ? (
          <article className="prose prose-sm max-w-none text-zinc-800">
            <h2>Acids &amp; bases</h2>
            <p>
              The pH scale measures acidity from 0 (strong acid) to 14 (strong base). Neutral
              solutions have pH 7.
            </p>
          </article>
        ) : null}

        {tab === "procedure" ? (
          <ol className="list-decimal space-y-2 pl-5 text-sm text-zinc-800">
            <li>Calibrate the pH probe with buffer solution 7.</li>
            <li>Add acid dropwise and record pH after each addition.</li>
            <li>Plot pH vs volume in the simulation tab.</li>
          </ol>
        ) : null}

        {tab === "simulation" ? (
          <div className="space-y-4">
            <p className="text-sm text-zinc-700">
              Titration step {simStep}/3 — click advance to simulate adding acid.
            </p>
            <div className="h-40 rounded-xl border border-dashed border-emerald-300 bg-emerald-50" />
            <button
              type="button"
              onClick={() => void advanceSimulation()}
              className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white"
            >
              Advance titration
            </button>
          </div>
        ) : null}

        {tab === "quiz" ? (
          <div className="space-y-6">
            {QUESTIONS.map((q) => (
              <label key={q.id} className="block text-sm">
                <span className="font-medium text-zinc-900">{q.prompt}</span>
                <input
                  className="mt-2 w-full rounded-lg border border-zinc-300 px-3 py-2"
                  placeholder={q.placeholder}
                  value={answers[q.id] ?? ""}
                  onChange={(e) => setAnswers((prev) => ({ ...prev, [q.id]: e.target.value }))}
                  disabled={session?.mode === "EXAM" && !!quizResult}
                />
              </label>
            ))}
            <button
              type="button"
              onClick={() => void handleQuizSubmit()}
              className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white"
            >
              Submit quiz
            </button>
            {quizResult ? <p className="text-sm font-medium text-emerald-700">{quizResult}</p> : null}
          </div>
        ) : null}
      </main>
    </div>
  );
}
