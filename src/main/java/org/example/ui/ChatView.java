package org.example.ui;


import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.example.service.RagContextService;
import org.example.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;

@Route("") // This makes it the root page
public class ChatView extends VerticalLayout {


  private final ChatClient chatClient;


  public ChatView(WeatherTools weatherTools, RagContextService ragContextService, VectorStore vectorStore, ChatClient.Builder builder, ChatMemory chatMemory) {
    this.chatClient = buildChatClient(builder, vectorStore, chatMemory, weatherTools);
    // Heading
    H1 title = new H1("Spring AI Assistant");
    titleStyle(title);

    // Input field for user messages
    TextField input = new TextField();
    input.setPlaceholder("Your Question");
    input.setWidthFull();

    // Ask button
    Button send = new Button("Send");
    sendStyle(send);

    // Wrap input and button in horizontal layout
    HorizontalLayout inputLayout = new HorizontalLayout(input, send);
    inputLayout.setWidthFull();
    inputLayout.setSpacing(true);

    // Area to display chat history
    VerticalLayout chatArea = new VerticalLayout();
    chatArea.setWidthFull();
    chatAreaStyle(chatArea);

    // Button click logic
    send.addClickListener(e -> {
      String userText = input.getValue();
      if (userText == null || userText.isBlank()) {
        return;
      }

      chatArea.add(buildUserMessage(userText));
      String chatResponse = this.invokeChatModel(userText);

      chatArea.getElement().executeJs(
          "const el = this;" +
              "setTimeout(() => { el.scrollTop = el.scrollHeight; }, 0);"
      );

      chatArea.add(buildHtmlResponse(chatResponse));

      // Scroll to bottom
   /*   chatArea.getElement()
          .callJsFunction("scrollTo", 0, chatArea.getElement().getProperty("scrollHeight"));
*/
      // Clear input
      input.clear();
    });
    MemoryBuffer buffer = new MemoryBuffer();
    Upload upload = new Upload(buffer);

    upload.addSucceededListener(event -> {
      try (InputStream inputStream = buffer.getInputStream()) {
        String filename = event.getFileName();
        ragContextService.addFileToContext(inputStream, filename); // call your adapted method
        Notification.show(filename + " added to context!");
      } catch (IOException e) {
        e.printStackTrace();
        Notification.show("Failed to add file to context");
      }
    });

    upload.setMaxFiles(10);
    upload.setMaxFileSize(10 * 1024 * 1024);
    upload.setAcceptedFileTypes(".txt", ".pdf", ".md", ".doc", ".docx");

    // Add all components to layout
    add(title, chatArea, inputLayout, upload);
    setAlignItems(Alignment.CENTER);

  }

  private ChatClient buildChatClient(Builder builder, VectorStore vectorStore,
      ChatMemory chatMemory, WeatherTools weatherTools) {
    return
        builder
            .defaultTools(weatherTools)
            .defaultAdvisors(

                // Absolutely don't let people ask about PHP 😆
                new SafeGuardAdvisor(List.of("PHP")),

                // Remember the conversation
                MessageChatMemoryAdvisor.builder(chatMemory).build(),

                // Define RAG pipeline
                // See
                // https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html#modules
                RetrievalAugmentationAdvisor.builder()
                    .queryTransformers(
                        // Rewrite the query for better search results
                        RewriteQueryTransformer.builder()
                            .chatClientBuilder(builder.build().mutate())
                            .build())
                    // Allow empty context (so you can try the assistant without context and
                    // compare)
                    .queryAugmenter(
                        ContextualQueryAugmenter.builder().allowEmptyContext(true).build())

                    // Use the vector store to retrieve documents
                    .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                            .similarityThreshold(0.50)
                            .vectorStore(vectorStore)
                            .build())
                    .build())
            .build();
  }

  private String invokeChatModel(String userText) {

    ChatClientRequestSpec chatPrompt = chatClient
        .prompt()
        .user(
            u -> {
              u.text(userText);
            });

    ChatResponse chatResponse = chatPrompt.call().chatResponse();
    return chatResponse.getResult().getOutput().getText();
  }


  private static Div buildUserMessage(String userText) {
    // Add user message to chat area
    Div userDiv = new Div();
    userDiv.setText("You: " + userText);
    userDiv.getStyle().set("font-weight", "bold");
    return userDiv;
  }

  private static Html buildHtmlResponse(String chatResponse) {
    Parser parser = Parser.builder().build();
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    String replyHtml = renderer.render(parser.parse(chatResponse));
    return new Html("<div style='color:blue'> AI: " + replyHtml + "</div>");
  }

  private static void titleStyle(H1 title) {
    title.getStyle().set("color", "#2E86C1");      // Blue color
    title.getStyle().set("font-size", "36px");    // Bigger font
    title.getStyle().set("font-family", "Arial, sans-serif"); // Custom font
    title.getStyle().set("font-weight", "bold");
    title.getStyle().set("text-align", "center");
  }

  private static void chatAreaStyle(VerticalLayout chatArea) {
    chatArea.getStyle().set("overflow-y", "auto");
    chatArea.getStyle().set("height", "600px");
    chatArea.getStyle().set("border", "1px solid #ccc");
    chatArea.getStyle().set("padding", "10px");
  }

  private static void sendStyle(Button send) {
    send.getStyle().set("background-color", "lightgray");
    send.getStyle().set("color", "black"); // Text color
    send.getStyle().set("font-weight", "bold");
    send.getStyle().set("border", "none");
    send.getStyle().set("padding", "6px 12px");
    send.getStyle().set("border-radius", "4px");
  }


}

