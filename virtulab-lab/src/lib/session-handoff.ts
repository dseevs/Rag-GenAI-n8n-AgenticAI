export type LabSession = {
  accessToken: string;
  attemptId: string;
  experimentId: string;
  mode: "PRACTICE" | "EXAM" | "DEMO";
  platformOrigin: string;
};

export function parseHandoff(searchParams: URLSearchParams): Omit<LabSession, "accessToken"> {
  return {
    attemptId: searchParams.get("attemptId") ?? "",
    experimentId: searchParams.get("experimentId") ?? searchParams.get("id") ?? "",
    mode: (searchParams.get("mode") as LabSession["mode"]) ?? "PRACTICE",
    platformOrigin: searchParams.get("platformOrigin") ?? "http://localhost:3000",
  };
}

export function listenForSession(onSession: (session: LabSession) => void): () => void {
  function handler(event: MessageEvent) {
    if (event.data?.type !== "VIRTULAB_SESSION") return;
    const { accessToken, attemptId, experimentId, mode } = event.data;
    if (!accessToken || !attemptId) return;
    onSession({
      accessToken,
      attemptId,
      experimentId,
      mode: mode ?? "PRACTICE",
      platformOrigin: event.origin,
    });
  }
  window.addEventListener("message", handler);
  return () => window.removeEventListener("message", handler);
}

export function emitProgress(
  session: LabSession,
  stepId: string,
  progress: Record<string, unknown>,
  timeSpentSec?: number,
) {
  window.parent.postMessage(
    {
      type: "VIRTULAB_PROGRESS",
      attemptId: session.attemptId,
      experimentId: session.experimentId,
      stepId,
      timeSpentSec,
      progress,
    },
    session.platformOrigin,
  );
}

export function trackTabView(session: LabSession, tab: string) {
  window.parent.postMessage(
    { type: "VIRTULAB_ANALYTICS", event: "lab_tab_view", tab, attemptId: session.attemptId },
    session.platformOrigin,
  );
}
