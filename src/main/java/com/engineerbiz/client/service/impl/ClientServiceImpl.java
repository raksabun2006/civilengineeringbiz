package com.engineerbiz.client.service.impl;

import com.engineerbiz.client.dto.ClientResponse;
import com.engineerbiz.client.dto.CreateClientRequest;
import com.engineerbiz.client.dto.UpdateClientRequest;
import com.engineerbiz.client.entity.Client;
import com.engineerbiz.client.enums.ClientStatus;
import com.engineerbiz.client.enums.ClientType;
import com.engineerbiz.client.mapper.ClientMapper;
import com.engineerbiz.client.repository.ClientRepository;
import com.engineerbiz.client.service.ClientService;
import com.engineerbiz.common.exception.DuplicateResourceException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.common.pagination.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public ClientResponse createClient(CreateClientRequest request) {
        if (clientRepository.existsByPrimaryContactPhone(request.primaryContactPhone().trim())) {
            throw new DuplicateResourceException("Client", "phone", request.primaryContactPhone().trim());
        }

        Client client = clientMapper.toEntity(request);
        client.setClientCode(generateClientCode());
        client.setStatus(ClientStatus.ACTIVE);

        Client savedClient = clientRepository.save(client);
        log.info("Created client [{}] with code [{}]", savedClient.getName(), savedClient.getClientCode());
        return clientMapper.toResponse(savedClient);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ClientResponse> getAllClients(ClientStatus status, ClientType clientType, String search, Pageable pageable) {
        Page<Client> clientPage = clientRepository.searchClients(status, clientType, search, pageable);
        return PagedResponse.from(clientPage.map(clientMapper::toResponse));
    }

    @Override
    @Transactional
    public ClientResponse updateClient(UUID id, UpdateClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        if (!client.getPrimaryContactPhone().equals(request.primaryContactPhone().trim()) &&
                clientRepository.existsByPrimaryContactPhone(request.primaryContactPhone().trim())) {
            throw new DuplicateResourceException("Client", "phone", request.primaryContactPhone().trim());
        }

        clientMapper.updateEntityFromRequest(request, client);
        Client updatedClient = clientRepository.save(client);
        log.info("Updated client [{}]", updatedClient.getClientCode());
        return clientMapper.toResponse(updatedClient);
    }

    @Override
    @Transactional
    public void deleteClient(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        // Soft deletion
        client.setDeletedAt(java.time.Instant.now());
        client.setStatus(ClientStatus.DORMANT);
        clientRepository.save(client);
        log.info("Soft-deleted client [{}]", client.getClientCode());
    }

    private synchronized String generateClientCode() {
        long count = clientRepository.count() + 1;
        int currentYear = Year.now().getValue();
        String code;
        do {
            code = String.format("CLT-%d-%04d", currentYear, count++);
        } while (clientRepository.existsByClientCode(code));
        return code;
    }
}
