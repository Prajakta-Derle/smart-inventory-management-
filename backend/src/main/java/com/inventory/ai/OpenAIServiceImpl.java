package com.inventory.ai;

import com.inventory.dto.AIQueryRequestDTO;
import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import com.inventory.exception.AIServiceUnavailableException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * OpenAI-backed implementation of {@link AIService}. It reads summary data from
 * repositories (read-only — never mutates any entity, Req 11.2), builds a
 * context prompt, and calls the OpenAI Chat Completions API. Any HTTP failure
 * or timeout is logged server-side and rethrown as
 * {@link AIServiceUnavailableException} (→ HTTP 503) without leaking the raw
 * provider error (Req 11.6).
 */
@Service
public class OpenAIServiceImpl implements AIService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIServiceImpl.class);

    private static final String SYSTEM_PROMPT =
            "You are an inventory management assistant for a small business. "
            + "Use the provided inventory summary to answer the user's question concisely. "
            + "When you recommend an action, clearly phrase it as a suggestion that the user "
            + "should review before acting. Do not fabricate data beyond the summary provided.";

    private final RestTemplate openAiRestTemplate;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;

    @Value("${app.openai.api-key:}")
    private String apiKey;

    @Value("${app.openai.base-url:https://api.openai.com/v1/chat/completions}")
    private String baseUrl;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String model;

    public OpenAIServiceImpl(@Qualifier("openAiRestTemplate") RestTemplate openAiRestTemplate,
                             ProductRepository productRepository,
                             SupplierRepository supplierRepository,
                             StockMovementRepository stockMovementRepository) {
        this.openAiRestTemplate = openAiRestTemplate;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public String query(AIQueryRequestDTO req) {
        String context = buildContext();
        String userContent = context + "\n\nUser question: " + req.getQuestion();

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userContent)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = openAiRestTemplate.postForObject(
                    baseUrl, new HttpEntity<>(body, headers), Map.class);
            return extractContent(response);
        } catch (Exception ex) {
            // Log the raw provider error server-side; never expose it to the client.
            log.error("OpenAI request failed", ex);
            throw new AIServiceUnavailableException("AI service is temporarily unavailable");
        }
    }

    // ── Context building (read-only repository summary) ───────────────────────

    private String buildContext() {
        long activeProducts = productRepository.countByActiveTrue();
        long activeSuppliers = supplierRepository.countByActiveTrue();
        long lowStock = productRepository.countLowStock();
        long outOfStock = productRepository.countOutOfStock();

        StringBuilder sb = new StringBuilder();
        sb.append("Inventory summary:\n");
        sb.append("- Active products: ").append(activeProducts).append('\n');
        sb.append("- Active suppliers: ").append(activeSuppliers).append('\n');
        sb.append("- Low-stock products: ").append(lowStock).append('\n');
        sb.append("- Out-of-stock products: ").append(outOfStock).append('\n');

        List<Product> critical = productRepository
                .findTopCritical(PageRequest.of(0, 10)).getContent();
        if (!critical.isEmpty()) {
            sb.append("Most critical low-stock items (name, SKU, current/min):\n");
            for (Product p : critical) {
                sb.append("  * ").append(p.getName())
                        .append(" (").append(p.getSku()).append("): ")
                        .append(p.getCurrentStock()).append('/').append(p.getMinStockLevel())
                        .append('\n');
            }
        }

        List<StockMovement> recent = stockMovementRepository
                .findAllByOrderByMovementTimestampDesc(PageRequest.of(0, 10));
        if (!recent.isEmpty()) {
            sb.append("Recent stock movements (type, direction, qty, product):\n");
            for (StockMovement m : recent) {
                sb.append("  * ").append(m.getMovementType())
                        .append(' ').append(m.getDirection())
                        .append(' ').append(m.getQuantity())
                        .append(" of ")
                        .append(m.getProduct() != null ? m.getProduct().getName() : "unknown")
                        .append('\n');
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        if (response == null) {
            throw new AIServiceUnavailableException("AI service returned no response");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new AIServiceUnavailableException("AI service returned no choices");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null || message.get("content") == null) {
            throw new AIServiceUnavailableException("AI service returned an empty message");
        }
        return message.get("content").toString().trim();
    }
}
