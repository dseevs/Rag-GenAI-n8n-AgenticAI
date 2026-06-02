const GRAPHQL_URL =
  process.env.NEXT_PUBLIC_PLATFORM_GRAPHQL_URL ?? "http://localhost:8097/graphql";
const QUIZ_URL = process.env.NEXT_PUBLIC_PLATFORM_QUIZ_URL ?? "http://localhost:8088";

export async function recordProgress(
  accessToken: string,
  input: {
    experimentId: string;
    attemptId: string;
    stepId: string;
    timeSpentSec?: number;
    progress: Record<string, unknown>;
  },
) {
  const response = await fetch(GRAPHQL_URL, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      query: `mutation RecordProgress($in: ProgressInput!) {
        recordProgress(input: $in) { eventId duplicate }
      }`,
      variables: { in: input },
    }),
  });
  if (!response.ok) throw new Error(`GraphQL HTTP ${response.status}`);
  const body = await response.json();
  if (body.errors?.length) throw new Error(body.errors[0].message);
  return body.data.recordProgress;
}

export async function submitQuiz(
  accessToken: string,
  input: {
    attemptId: string;
    experimentId: string;
    answers: Array<{ questionId: string; answer: string }>;
  },
) {
  const response = await fetch(`${QUIZ_URL}/api/v1/quiz/submit`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Quiz HTTP ${response.status}`);
  }
  return response.json();
}
