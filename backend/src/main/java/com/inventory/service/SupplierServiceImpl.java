package com.inventory.service;

import com.inventory.dto.CreateSupplierRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.SupplierResponseDTO;
import com.inventory.dto.UpdateSupplierRequestDTO;
import com.inventory.entity.Supplier;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    // ── Create (Req 5.1, 5.2) ─────────────────────────────────────────────────

    @Override
    @Transactional
    public SupplierResponseDTO createSupplier(CreateSupplierRequestDTO request) {
        if (supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("email", request.getEmail());
        }

        Supplier supplier = Supplier.builder()
                .supplierName(request.getSupplierName())
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .gstNumber(request.getGstNumber())
                .active(true)
                .build();

        Supplier saved = supplierRepository.save(supplier);
        return toResponseDTO(saved);
    }

    // ── Update (Req 5.3) ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public SupplierResponseDTO updateSupplier(Long id, UpdateSupplierRequestDTO request) {
        Supplier supplier = findSupplierOrThrow(id);

        // Reject an email clash with a *different* supplier.
        supplierRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("email", request.getEmail());
                });

        supplier.setSupplierName(request.getSupplierName());
        supplier.setCompanyName(request.getCompanyName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setGstNumber(request.getGstNumber());

        Supplier saved = supplierRepository.save(supplier);
        return toResponseDTO(saved);
    }

    // ── Deactivate / soft-delete (Req 5.4) ────────────────────────────────────

    @Override
    @Transactional
    public SupplierResponseDTO deactivateSupplier(Long id) {
        Supplier supplier = findSupplierOrThrow(id);
        supplier.setActive(false);
        Supplier saved = supplierRepository.save(supplier);
        return toResponseDTO(saved);
    }

    // ── Get by ID (Req 5.6) ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public SupplierResponseDTO getSupplierById(Long id) {
        return toResponseDTO(findSupplierOrThrow(id));
    }

    // ── Paginated list with filters (Req 5.5) ─────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SupplierResponseDTO> listSuppliers(String supplierName,
                                                              String companyName,
                                                              Boolean active,
                                                              Pageable pageable) {
        Page<SupplierResponseDTO> page = supplierRepository
                .search(normalize(supplierName), normalize(companyName), active, pageable)
                .map(this::toResponseDTO);
        return PageResponseDTO.from(page);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Supplier findSupplierOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SupplierResponseDTO toResponseDTO(Supplier supplier) {
        return SupplierResponseDTO.builder()
                .id(supplier.getId())
                .supplierName(supplier.getSupplierName())
                .companyName(supplier.getCompanyName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .gstNumber(supplier.getGstNumber())
                .active(supplier.isActive())
                .totalPurchaseOrderCount(
                        supplierRepository.countPurchaseOrdersBySupplierId(supplier.getId()))
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}
