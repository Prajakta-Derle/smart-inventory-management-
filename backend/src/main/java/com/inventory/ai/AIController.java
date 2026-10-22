package com.inventory.ai;

import com.inventory.dto.AIQueryRequestDTO;
import com.inventory.dto.AIQueryResponseDTO;
import com.inventory.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    /**
     * ADMIN, STAFF — natural-language inventory query. JWT required
     * (unauthenticated requests are rejected with 401 by Spring Security).
     * The response text is returned in the standard envelope; a provider
     * failure surfaces as HTTP 503 via the global handler.
     */
    @PostMapping("/query")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<AIQueryResponseDTO>> query(
            @Valid @RequestBody AIQueryRequestDTO request) {

        String answer = aiService.query(request);
        AIQueryResponseDTO response = AIQueryResponseDTO.builder().answer(answer).build();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "AI response generated", response));
    }
}
