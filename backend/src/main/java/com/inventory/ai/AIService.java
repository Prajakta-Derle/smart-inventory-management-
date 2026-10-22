package com.inventory.ai;

import com.inventory.dto.AIQueryRequestDTO;

/**
 * Isolated AI assistant abstraction. Replacing the AI provider requires only
 * a new implementation of this interface and rebinding the bean — no other
 * module changes (Requirement 11.8).
 */
public interface AIService {

    /**
     * Answers a natural-language inventory question using live summary data
     * plus the configured AI provider. Returns the provider's response text.
     */
    String query(AIQueryRequestDTO req);
}
