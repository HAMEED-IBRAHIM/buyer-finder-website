package com.example.buyerfinder.service;

import com.example.buyerfinder.model.Buyer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // gemini-3.5-flash is confirmed working — put it FIRST to skip 503 waste
    // gemini-3.8-flash kept returning 503 (overloaded) — try it only as fallback
    private static final String[] MODELS = {
        "gemini-1.5-flash",
        "gemini-1.5-pro"
    };

    public GeminiService() {
        // 10s connect + 20s read timeout — fail fast instead of hanging
        // Fast timeouts so you never wait long
        factory.setConnectTimeout(3_000);
        factory.setReadTimeout(6_000);
        this.restTemplate = new RestTemplate(factory);
    }

    public List<Buyer> findBuyers(String query) {
        if (geminiApiKey == null || geminiApiKey.contains("your-gemini-api-key")) {
            return getMockData();
        }

        String prompt = "You are a business lead generation assistant. " +
            "Find 5 real or realistic potential buyers/businesses for: '" + query + "'. " +
            "Return ONLY a valid JSON array with no extra text, no markdown, no code fences. " +
            "Each object must have exactly these 5 keys: " +
            "\"name\" (contact person name), " +
            "\"company\" (business name), " +
            "\"email\" (business email - generate realistic one from company domain), " +
            "\"location\" (city and US state, e.g. 'Austin, TX'), " +
            "\"website\" (company website URL). " +
            "Spread results across different US states. " +
            "Output only the raw JSON array, nothing else.";

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode contentObj = contents.addObject();
        ArrayNode parts = contentObj.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode genConfig = requestBody.putObject("generationConfig");
        genConfig.put("temperature", 0.6);
        genConfig.put("maxOutputTokens", 4096);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String bodyJson;
        try {
            bodyJson = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            System.err.println("ERROR serializing request: " + e.getMessage());
            return getMockData();
        }

        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

        for (String model : MODELS) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                       + model + ":generateContent?key=" + geminiApiKey;
            try {
                System.out.println("GEMINI: → " + model);
                ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

                if (response.getBody() != null) {
                    List<Buyer> buyers = parseGeminiResponse(response.getBody());
                    if (!buyers.isEmpty()) {
                        System.out.println("GEMINI: ✓ " + buyers.size() + " buyers from " + model);
                        return buyers;
                    }
                }
            } catch (HttpServerErrorException e) {
                // 503 Overloaded — skip immediately to next model, no delay
                System.err.println("GEMINI: " + model + " → " + e.getStatusCode() + " (skipping)");
            } catch (HttpClientErrorException e) {
                System.err.println("GEMINI: " + model + " → " + e.getStatusCode());
            } catch (Exception e) {
                System.err.println("GEMINI: " + model + " → " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        System.err.println("GEMINI: All models failed → returning mock data");
        return getMockData();
    }

    private List<Buyer> parseGeminiResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText().trim();

                    // Strip markdown code fences
                    text = text.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();

                    // Extract JSON array
                    int start = text.indexOf('[');
                    int end   = text.lastIndexOf(']');
                    if (start >= 0 && end > start) {
                        text = text.substring(start, end + 1);
                    }

                    List<Buyer> buyers = objectMapper.readValue(text, new TypeReference<List<Buyer>>() {});
                    System.out.println("GEMINI: Parsed " + buyers.size() + " buyers");
                    return buyers;
                }
            }
        } catch (Exception e) {
            System.err.println("GEMINI PARSE ERROR: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private List<Buyer> getMockData() {
        List<Buyer> list = new ArrayList<>();
        list.add(new Buyer("Sarah Mitchell",  "Urban Nest Interiors",   "sarah@urbannestinteriors.com",   "Austin, TX",        "www.urbannestinteriors.com"));
        list.add(new Buyer("James Carter",    "Coastal Living Decor",   "james@coastallivingdecor.com",   "Miami, FL",         "www.coastallivingdecor.com"));
        list.add(new Buyer("Emily Zhang",     "Modern Home Studio",     "emily@modernhomestudio.com",     "San Francisco, CA", "www.modernhomestudio.com"));
        list.add(new Buyer("Marcus Johnson",  "Southern Charm Home",    "marcus@southerncharmhome.com",   "Nashville, TN",     "www.southerncharmhome.com"));
        list.add(new Buyer("Olivia Reyes",    "The Design Collective",  "olivia@thedesigncollective.com", "New York, NY",      "www.thedesigncollective.com"));
        list.add(new Buyer("Daniel Park",     "Pacific Home Gallery",   "daniel@pacifichomegallery.com",  "Seattle, WA",       "www.pacifichomegallery.com"));
        list.add(new Buyer("Rachel Torres",   "Bloom & Nest Boutique",  "rachel@bloomandnest.com",        "Denver, CO",        "www.bloomandnest.com"));
        list.add(new Buyer("Kevin Brown",     "Heartland Home Decor",   "kevin@heartlandhome.com",        "Chicago, IL",       "www.heartlandhome.com"));
        list.add(new Buyer("Amanda Patel",    "Luxe Living Interiors",  "amanda@luxelivinginteriors.com", "Scottsdale, AZ",    "www.luxelivinginteriors.com"));
        list.add(new Buyer("Thomas Williams", "New England Home Works", "thomas@nehomeworks.com",         "Boston, MA",        "www.nehomeworks.com"));
        return list;
    }
}
