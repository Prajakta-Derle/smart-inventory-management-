package com.inventory.dto;

import com.inventory.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateStatusRequestDTO {

    @NotNull(message = "Target status is required")
    private OrderStatus status;
}
