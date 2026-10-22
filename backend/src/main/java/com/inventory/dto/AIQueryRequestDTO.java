package com.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AIQueryRequestDTO {

    @NotBlank(message = "Question is required")
    @Size(max = 2000, message = "Question must not exceed 2000 characters")
    private String question;
}
