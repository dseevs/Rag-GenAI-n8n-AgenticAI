package com.virtulab.platform.rag.corpus;

import com.virtulab.platform.rag.config.RagProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class CorpusLoader {

    private final RagProperties props;

    public CorpusLoader(RagProperties props) {
        this.props = props;
    }

    public List<CorpusDocument> loadAll() throws IOException {
        Path root = Path.of(props.corpusPath()).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            throw new IOException("Corpus path not found: " + root);
        }
        List<CorpusDocument> docs = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(p -> p.toString().endsWith(".md"))
                    .forEach(p -> {
                        try {
                            docs.addAll(parseFile(p, root));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return docs;
    }

    private List<CorpusDocument> parseFile(Path file, Path root) throws IOException {
        String raw = Files.readString(file);
        Map<String, String> meta = new HashMap<>();
        String body = raw;
        if (raw.startsWith("---")) {
            int end = raw.indexOf("---", 3);
            if (end > 0) {
                String front = raw.substring(3, end).trim();
                body = raw.substring(end + 3).trim();
                for (String line : front.split("\n")) {
                    int idx = line.indexOf(':');
                    if (idx > 0) {
                        meta.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
                    }
                }
            }
        }
        meta.putIfAbsent("experimentId", "unknown");
        meta.putIfAbsent("lang", "en");
        meta.putIfAbsent("docType", "theory");
        meta.put("sourceFile", root.relativize(file).toString());

        List<CorpusDocument> chunks = new ArrayList<>();
        for (String part : chunk(body, 900)) {
            chunks.add(new CorpusDocument(part, Map.copyOf(meta), file.getFileName().toString()));
        }
        return chunks;
    }

    static List<String> chunk(String text, int maxLen) {
        List<String> parts = new ArrayList<>();
        String[] paragraphs = text.split("\n\n+");
        StringBuilder buf = new StringBuilder();
        for (String para : paragraphs) {
            if (buf.length() + para.length() + 2 > maxLen && buf.length() > 0) {
                parts.add(buf.toString().trim());
                buf = new StringBuilder();
            }
            if (para.length() > maxLen) {
                if (buf.length() > 0) {
                    parts.add(buf.toString().trim());
                    buf = new StringBuilder();
                }
                for (int i = 0; i < para.length(); i += maxLen) {
                    parts.add(para.substring(i, Math.min(i + maxLen, para.length())).trim());
                }
            } else {
                if (buf.length() > 0) {
                    buf.append("\n\n");
                }
                buf.append(para);
            }
        }
        if (buf.length() > 0) {
            parts.add(buf.toString().trim());
        }
        return parts;
    }
}
