package com.virtulab.platform.rag.job;

import com.virtulab.platform.contracts.ai.RagJobStatusResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ReindexJobStore {

    private final Map<String, RagJobStatusResponse> jobs = new ConcurrentHashMap<>();

    public void put(RagJobStatusResponse status) {
        jobs.put(status.jobId(), status);
    }

    public RagJobStatusResponse get(String jobId) {
        return jobs.get(jobId);
    }
}
