package com.engineerbiz.service.service.impl;

import com.engineerbiz.common.exception.DuplicateResourceException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.service.dto.CreateEngineeringServiceRequest;
import com.engineerbiz.service.dto.EngineeringServiceResponse;
import com.engineerbiz.service.dto.UpdateEngineeringServiceRequest;
import com.engineerbiz.service.entity.EngineeringService;
import com.engineerbiz.service.enums.ServiceCategory;
import com.engineerbiz.service.mapper.EngineeringServiceMapper;
import com.engineerbiz.service.repository.EngineeringServiceRepository;
import com.engineerbiz.service.service.EngineeringServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EngineeringServiceServiceImpl implements EngineeringServiceService {

    private final EngineeringServiceRepository repository;
    private final EngineeringServiceMapper mapper;

    @Override
    @Transactional
    public EngineeringServiceResponse createService(CreateEngineeringServiceRequest request) {
        String normalizedCode = request.code().toUpperCase().trim();
        if (repository.existsByCode(normalizedCode)) {
            throw new DuplicateResourceException("EngineeringService", "code", normalizedCode);
        }

        EngineeringService service = mapper.toEntity(request);
        service.setCode(normalizedCode);
        EngineeringService saved = repository.save(service);
        log.info("Created engineering service [{}] ({})", saved.getName(), saved.getCode());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EngineeringServiceResponse getServiceById(UUID id) {
        EngineeringService service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EngineeringService", id));
        return mapper.toResponse(service);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EngineeringServiceResponse> getAllServices(ServiceCategory category, Boolean isActive, String search, Pageable pageable) {
        Page<EngineeringService> page = repository.searchServices(category, isActive, search, pageable);
        return PagedResponse.from(page.map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EngineeringServiceResponse> getActiveServices() {
        return repository.findAllByIsActiveTrue().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EngineeringServiceResponse updateService(UUID id, UpdateEngineeringServiceRequest request) {
        EngineeringService service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EngineeringService", id));

        mapper.updateEntityFromRequest(request, service);
        EngineeringService updated = repository.save(service);
        log.info("Updated engineering service [{}]", updated.getCode());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteService(UUID id) {
        EngineeringService service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EngineeringService", id));

        service.setDeletedAt(Instant.now());
        service.setIsActive(false);
        repository.save(service);
        log.info("Soft-deleted engineering service [{}]", service.getCode());
    }
}
