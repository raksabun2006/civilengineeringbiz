package com.engineerbiz.client.service;

import com.engineerbiz.client.dto.ClientResponse;
import com.engineerbiz.client.dto.CreateClientRequest;
import com.engineerbiz.client.entity.Client;
import com.engineerbiz.client.enums.ClientStatus;
import com.engineerbiz.client.enums.ClientType;
import com.engineerbiz.client.mapper.ClientMapper;
import com.engineerbiz.client.repository.ClientRepository;
import com.engineerbiz.client.service.impl.ClientServiceImpl;
import com.engineerbiz.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientMapper clientMapper;

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientService = new ClientServiceImpl(clientRepository, clientMapper);
    }

    @Test
    @DisplayName("Should create client successfully and assign generated code")
    void testCreateClientSuccess() {
        CreateClientRequest request = new CreateClientRequest(
                "Urban Living Solutions Ltd",
                "យូនីក លីវីង សូលូសិន",
                ClientType.CORPORATE,
                "K008-987654",
                "Mr. John Smith",
                "john@uls.com.kh",
                "+855 23 888 999",
                "Toul Kork, Phnom Penh",
                "Phnom Penh",
                30,
                new BigDecimal("50000.00"),
                "High rise development partner"
        );

        when(clientRepository.existsByPrimaryContactPhone("+855 23 888 999")).thenReturn(false);
        when(clientRepository.count()).thenReturn(5L);
        when(clientRepository.existsByClientCode(any())).thenReturn(false);

        Client mapped = new Client();
        mapped.setName(request.name());
        when(clientMapper.toEntity(request)).thenReturn(mapped);

        Client saved = new Client();
        saved.setId(UUID.randomUUID());
        saved.setClientCode("CLT-2026-0006");
        saved.setName(request.name());
        saved.setStatus(ClientStatus.ACTIVE);
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        ClientResponse expectedResponse = new ClientResponse(
                saved.getId(), "CLT-2026-0006", request.name(), request.nameKhmer(),
                ClientType.CORPORATE, ClientStatus.ACTIVE, request.vatTin(),
                request.primaryContactName(), request.primaryContactEmail(),
                request.primaryContactPhone(), request.billingAddress(),
                request.city(), 30, new BigDecimal("50000.00"), request.notes(), null, null
        );
        when(clientMapper.toResponse(saved)).thenReturn(expectedResponse);

        ClientResponse response = clientService.createClient(request);

        assertThat(response).isNotNull();
        assertThat(response.clientCode()).isEqualTo("CLT-2026-0006");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException on existing phone")
    void testCreateClientDuplicatePhone() {
        CreateClientRequest request = new CreateClientRequest(
                "Another Corp", null, ClientType.CORPORATE, null, null, null,
                "+855 23 888 999", null, null, null, null, null
        );

        when(clientRepository.existsByPrimaryContactPhone("+855 23 888 999")).thenReturn(true);

        assertThatThrownBy(() -> clientService.createClient(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("phone");
    }
}
