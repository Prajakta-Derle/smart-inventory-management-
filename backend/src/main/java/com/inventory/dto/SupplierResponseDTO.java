package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDTO {

    private Long id;
    private String supplierName;
    private String companyName;
    private String email;
    private String phone;
    private String address;
    private String gstNumber;
    private boolean active;
    private long totalPurchaseOrderCount;
    private LocalDateTime createdAt;
}
