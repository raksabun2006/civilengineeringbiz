package com.engineerbiz.client.service;

import com.engineerbiz.client.dto.ClientResponse;
import com.engineerbiz.client.dto.CreateClientRequest;
import com.engineerbiz.client.dto.UpdateClientRequest;
import com.engineerbiz.client.enums.ClientStatus;
import com.engineerbiz.client.enums.ClientType;
import com.engineerbiz.common.pagination.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClientService {

    ClientResponse createClient(CreateClientRequest request);

    ClientResponse getClientById(UUID id);

    PagedResponse<ClientResponse> getAllClients(ClientStatus status, ClientType clientType, String search, Pageable pageable);

    ClientResponse updateClient(UUID id, UpdateClientRequest request);

    void deleteClient(UUID id);
}
