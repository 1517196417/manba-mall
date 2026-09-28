import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class TestDocumentSerialization {
    public static void main(String[] args) throws Exception {
        // Same ObjectMapper configuration as Spring AI's JsonParser
        ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

        // Create a Document similar to what SimpleVectorStore returns
        Document doc = Document.builder()
            .text("This is a test document about return policy.")
            .metadata("filename", "7天无理由退换货政策.md")
            .metadata("source", "7天无理由退换货政策.md")
            .metadata("charset", "UTF-8")
            .score(0.85)
            .build();

        // List all methods that look like getters
        System.out.println("=== Document getter methods ===");
        for (Method m : Document.class.getMethods()) {
            String name = m.getName();
            if ((name.startsWith("get") && m.getParameterCount() == 0 && !name.equals("getClass")) ||
                (name.startsWith("is") && m.getParameterCount() == 0 && m.getReturnType() == boolean.class)) {
                System.out.println("  " + name + "() -> " + m.getReturnType().getSimpleName());
            }
        }

        // Try to serialize a single Document
        System.out.println("\n=== Serializing single Document ===");
        try {
            String json = objectMapper.writeValueAsString(doc);
            System.out.println("JSON: " + json);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Try to serialize a List<Document> (like what searchPolicyDocs returns)
        System.out.println("\n=== Serializing List<Document> ===");
        List<Document> docs = new ArrayList<>();
        docs.add(doc);
        docs.add(Document.builder()
            .text("Second document about refund policy.")
            .metadata("filename", "退款政策.md")
            .score(0.75)
            .build());

        try {
            String json = objectMapper.writeValueAsString(docs);
            System.out.println("JSON: " + json);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Check what Jackson sees as properties
        System.out.println("\n=== Jackson property Discovery ===");
        com.fasterxml.jackson.databind.BeanDescription bd = objectMapper.getSerializationConfig()
            .introspect(objectMapper.constructType(Document.class));
        System.out.println("Properties found by Jackson:");
        for (com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition p : bd.findProperties()) {
            System.out.println("  " + p.getName() + " -> " + p.getPrimaryType().getRawClass().getSimpleName());
        }
    }
}
