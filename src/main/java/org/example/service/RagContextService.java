package org.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

/**
 * Service for managing contextual data for Retrieval-Augmented Generation (RAG) processes.
 * This service allows the addition of file data to a context that can be utilized for
 * document retrieval and text processing operations.
 *
 * Files are managed in an in-memory list for simplicity.
 */
@Service
public class RagContextService {

  private final VectorStore vectorStore;
  private final List<String> filesInContext = new ArrayList<>();

  public RagContextService(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  public void addFileToContext(InputStream inputStream, String filename) throws IOException {
    var resource = new InputStreamResource(inputStream);
    vectorStore.write(new TokenTextSplitter().apply(new TikaDocumentReader(resource).read()));

    filesInContext.add(filename);
  }

  public List<String> getFilesInContext() {
    return filesInContext;
  }
}
