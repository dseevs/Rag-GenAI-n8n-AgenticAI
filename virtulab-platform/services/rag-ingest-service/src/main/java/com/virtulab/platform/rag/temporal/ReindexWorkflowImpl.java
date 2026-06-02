package com.virtulab.platform.rag.temporal;

import com.virtulab.platform.contracts.ai.RagJobStatusResponse;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class ReindexWorkflowImpl implements ReindexWorkflow {

    private RagJobStatusResponse current =
            new RagJobStatusResponse("unknown", "RUNNING", 0, "Started");

    private final ReindexActivities activities = Workflow.newActivityStub(
            ReindexActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(10))
                    .setRetryOptions(
                            RetryOptions.newBuilder()
                                    .setInitialInterval(Duration.ofSeconds(2))
                                    .setBackoffCoefficient(2.0)
                                    .setMaximumInterval(Duration.ofSeconds(30))
                                    .setMaximumAttempts(8)
                                    .build())
                    .build());

    @Override
    public RagJobStatusResponse run() {
        String jobId = Workflow.getInfo().getWorkflowId();
        current = new RagJobStatusResponse(jobId, "RUNNING", 0, "Started");
        try {
            int count = activities.reindexCorpus();
            current = new RagJobStatusResponse(jobId, "COMPLETED", count, "Indexed " + count + " chunks");
        } catch (Exception e) {
            current = new RagJobStatusResponse(jobId, "FAILED", 0, e.getMessage());
        }
        return current;
    }

    @Override
    public RagJobStatusResponse status() {
        return current;
    }
}

