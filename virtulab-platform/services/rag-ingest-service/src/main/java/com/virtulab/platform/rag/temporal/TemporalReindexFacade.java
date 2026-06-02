package com.virtulab.platform.rag.temporal;

import com.virtulab.platform.contracts.ai.RagJobStatusResponse;
import com.virtulab.platform.contracts.ai.RagReindexResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@ConditionalOnProperty(name = "virtulab.temporal.enabled", havingValue = "true")
public class TemporalReindexFacade {

    private final WorkflowClient client;
    private final String taskQueue;

    public TemporalReindexFacade(
            WorkflowClient client,
            @Value("${virtulab.temporal.task-queue:virtulab-rag}") String taskQueue
    ) {
        this.client = client;
        this.taskQueue = taskQueue;
    }

    public Mono<RagReindexResponse> start() {
        return Mono.fromCallable(() -> {
                    String workflowId = "rag-reindex-" + java.util.UUID.randomUUID();
                    ReindexWorkflow wf = client.newWorkflowStub(
                            ReindexWorkflow.class,
                            WorkflowOptions.newBuilder()
                                    .setWorkflowId(workflowId)
                                    .setTaskQueue(taskQueue)
                                    .build());
                    WorkflowClient.start(wf::run);
                    return new RagReindexResponse(workflowId, "RUNNING");
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<RagJobStatusResponse> status(String jobId) {
        return Mono.fromCallable(() -> {
                    try {
                        ReindexWorkflow wf = client.newWorkflowStub(ReindexWorkflow.class, jobId);
                        return wf.status();
                    } catch (WorkflowNotFoundException e) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown job: " + jobId);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}

