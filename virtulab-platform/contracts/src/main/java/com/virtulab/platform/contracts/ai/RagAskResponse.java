package com.virtulab.platform.contracts.ai;

import java.util.List;

public record RagAskResponse(
        String answer,
        List<RagCitation> citations,
        String source
) {}
