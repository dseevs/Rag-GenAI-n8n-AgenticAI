package com.virtulab.platform.rag.corpus;

import java.util.Map;

public record CorpusDocument(
        String content,
        Map<String, String> metadata,
        String sourceFile
) {}
