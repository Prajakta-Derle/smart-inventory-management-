package com.inventory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateSupplierRequestDTO {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 255, message = "Supplier name must not exceed 255 characters")
    private String supplierName;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid RFC 5322 address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(
        regexp = "^[+]?[0-9][0-9\\s-]{6,19}$",
        message = "Phone number must be 7 to 20 digits and may include a leading + and spaces or hyphens"
    )
    private String phone;

    @Size(max = 2000, message = "Address must not exceed 2000 characters")
    private String address;

    @Size(max = 50, message = "GST number must not exceed 50 characters")
    private String gstNumber;
}
