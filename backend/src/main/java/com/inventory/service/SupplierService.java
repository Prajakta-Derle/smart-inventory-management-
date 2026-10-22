package com.inventory.service;

import com.inventory.dto.CreateSupplierRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.SupplierResponseDTO;
import com.inventory.dto.UpdateSupplierRequestDTO;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    SupplierResponseDTO createSupplier(CreateSupplierRequestDTO request);

    SupplierResponseDTO updateSupplier(Long id, UpdateSupplierRequestDTO request);

    SupplierResponseDTO deactivateSupplier(Long id);

    SupplierResponseDTO getSupplierById(Long id);

    PageResponseDTO<SupplierResponseDTO> listSuppliers(String supplierName,
                                                       String companyName,
                                                       Boolean active,
                                                       Pageable pageable);
}
