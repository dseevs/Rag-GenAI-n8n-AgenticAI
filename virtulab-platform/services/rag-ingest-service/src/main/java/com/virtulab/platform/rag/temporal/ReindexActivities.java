package com.virtulab.platform.rag.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ReindexActivities {

    @ActivityMethod
    int reindexCorpus();
}

