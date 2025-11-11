package com.email.emailwriter.servie;

import com.email.emailwriter.model.EmailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@Slf4j
public class EmailService {

    private final WebClient webClient;
    private final String apiKey;


    public EmailService(WebClient.Builder webClientBuilder, @Value("${gemini.api.url}") String baseUrl,@Value("${gemini.api.key}") String geminiApiKey) {

        this.apiKey = geminiApiKey;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public String generateEmailReply(EmailRequest emailRequest) {

        String prompt =  buildPrompt(emailRequest);
        log.info( ":::::::::::::::::::: Generated Prompt in Service: {}", prompt);

        String requestBody = String.format("""
                {
                    "contents": [
                      {
                        "parts": [
                          {
                            "text": "%s"
                          }
                        ]
                      }
                    ]
                }
                """, prompt);
        log.info(":::::::::::::::::::: Request Body: {}", requestBody);

        String response = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/v1beta/models/gemini-2.5-flash:generateContent").build())
                .header("x-goog-api-key",apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();


        log.info( ":::::::::::::::::::: Generated Response from Gemini API: {}", response);
        return extractEmailFromResponse(response);
    }

    private String extractEmailFromResponse(String response) {
        try {

            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(response); // convert string to json

            JsonNode textNode = rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode
                    .asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim(); // coverts to string

            log.info("::::::::::::::::::: Extracted Clean AI Content: {}", jsonContent);

            return jsonContent;

        } catch (Exception e) {
            e.printStackTrace();
            return "Network error processing response";
        }

    }

    private String buildPrompt(EmailRequest emailRequest) {

        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("Write an email reply to the following email: ");

        String tone = emailRequest.getTone();
        String length = emailRequest.getLength();
        String suggestions = emailRequest.getSuggestions();

        if(tone != null && !tone.isEmpty()){
            promptBuilder.append("Write an email reply with a ")
                    .append(tone).append(" tone.");
        }
        if(length != null && !length.isEmpty()){
            promptBuilder.append(" The reply should be ")
                    .append(length).append(" in length.");
        }

        promptBuilder.append(" Original Email: ")
                .append(emailRequest.getEmailContent());

        if(suggestions != null && !suggestions.isEmpty()){
            promptBuilder.append(" Additionally, consider the following suggestions while drafting the reply: ")
                    .append(suggestions);
        }

        return promptBuilder.toString();
    }

}
