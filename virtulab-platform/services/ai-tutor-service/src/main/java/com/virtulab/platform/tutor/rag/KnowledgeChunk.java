package com.virtulab.platform.tutor.rag;

import java.util.UUID;

public record KnowledgeChunk(UUID id, String content, String docType, String experimentId) {}
