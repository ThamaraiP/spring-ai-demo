# Spring AI RAG Chat (Vaadin) Demo

An end-to-end demo that showcases:
- A conversational UI built with Vaadin
- Spring AI’s ChatClient with tool-calling and conversation memory
- Retrieval-Augmented Generation (RAG) from user-uploaded documents
- Simple guardrails to filter unwanted topics

Access the app at http://localhost:8080 after starting it.

## Features

- Chat UI in the browser with Markdown rendering for AI replies
- Upload multiple files to enrich the AI’s retrieval context (txt, pdf, md, doc, docx)
- Conversation memory across turns
- Optional tool-calling (example: a simple weather tool)
- Pluggable vector store for RAG (in-memory or external)

## Prerequisites

- Java 21 or newer (tested with Java 24)
- Maven 3.9+
- An AI model provider credential (choose one):
    - OpenAI API key
    - Azure OpenAI endpoint + key
    - Local model via Ollama

Note: Do not hardcode credentials. Use environment variables or a secure secrets manager.

## Quick Start

1) Clone and enter the project directory:
```shell script
git clone <your-repo-url>
cd spring-ai-demo
```


2) Provide model credentials (choose one option):

- OpenAI:
```shell script
export SPRING_AI_OPENAI_API_KEY=<YOUR_OPENAI_API_KEY>
```


- Azure OpenAI:
```shell script
export SPRING_AI_AZURE_OPENAI_API_KEY=<YOUR_AZURE_OPENAI_API_KEY>
export SPRING_AI_AZURE_OPENAI_ENDPOINT=<YOUR_AZURE_OPENAI_ENDPOINT>
export SPRING_AI_AZURE_OPENAI_DEPLOYMENT_NAME=<YOUR_MODEL_DEPLOYMENT_NAME>
```


- Ollama (local):
```shell script
export SPRING_AI_OLLAMA_BASE_URL=http://localhost:11434
# Make sure an Ollama model is pulled, e.g.:
# ollama pull llama3
```


3) Run the application:
```shell script
mvn spring-boot:run
```


4) Open the UI:
- http://localhost:8080

## Configuration

You can configure the model and options in application.yml or via environment variables. Examples:

- OpenAI (YAML):
```yaml
spring:
  ai:
    openai:
      api-key: ${SPRING_AI_OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.2
```


- Azure OpenAI (YAML):
```yaml
spring:
  ai:
    azure:
      openai:
        api-key: ${SPRING_AI_AZURE_OPENAI_API_KEY}
        endpoint: ${SPRING_AI_AZURE_OPENAI_ENDPOINT}
        chat:
          options:
            deployment-name: ${SPRING_AI_AZURE_OPENAI_DEPLOYMENT_NAME}
            temperature: 0.2
```


- Ollama (YAML):
```yaml
spring:
  ai:
    ollama:
      base-url: ${SPRING_AI_OLLAMA_BASE_URL:http://localhost:11434}
      chat:
        options:
          model: llama3
          temperature: 0.2
```


RAG Vector Store:
- The application wires a VectorStore for retrieval. You can use an in-memory store for local testing or configure a persistent store (e.g., PostgreSQL with pgvector, a vector DB, etc.) through your Spring configuration. Make sure your embeddings configuration matches your chosen model/provider.

## Using the App

- Ask questions in the input field and click “Send”.
- Upload documents to enrich the assistant’s knowledge base:
    - Supported: .txt, .pdf, .md, .doc, .docx
    - Up to 10 files, 10 MB each
- The assistant will optionally use uploaded content to answer queries and may call available tools when appropriate (e.g., weather).

Tip: Try asking a general question first, then upload a document and ask a question related to its content to see RAG in action.

## Build and Run a Jar

- Build:
```shell script
mvn -DskipTests=true clean package
```


- Run:
```shell script
java -jar target/*.jar
```


## Development Notes

- The Vaadin UI runs inside the Spring Boot app; the first run may trigger frontend processing.
- Use your IDE’s Spring Boot Run Configuration for a quick dev loop.
- If you change environment variables, restart the application to pick them up.

## Troubleshooting

- 401/403 from provider: Double-check your API key and any required endpoint or deployment names.
- Empty or generic answers: Ensure documents were uploaded successfully and your vector store/embeddings are configured.
- Large file uploads fail: Verify file size/type limits and server logs.
- Local models (Ollama): Confirm the model is downloaded and the service is reachable at the configured base URL.

## Security

- Never commit secrets. Use environment variables or a secret store.
- Rotate credentials regularly.
- Limit network egress in production environments.

## License

Add your chosen license here.

## Acknowledgements

- Spring AI
- Vaadin Framework