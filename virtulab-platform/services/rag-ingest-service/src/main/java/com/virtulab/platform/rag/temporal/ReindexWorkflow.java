package com.virtulab.platform.rag.temporal;

import com.virtulab.platform.contracts.ai.RagJobStatusResponse;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ReindexWorkflow {

    @WorkflowMethod
    RagJobStatusResponse run();

    @QueryMethod(name = "status")
    RagJobStatusResponse status();
}

