import { LabShell } from "@/components/LabShell";

export default async function ExperimentPage({
  params,
  searchParams,
}: {
  params: Promise<{ experimentId: string }>;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const { experimentId } = await params;
  const sp = await searchParams;
  return <LabShell experimentId={experimentId} searchParams={sp} />;
}
