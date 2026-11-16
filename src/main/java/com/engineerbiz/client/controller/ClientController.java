package com.engineerbiz.client.controller;

import com.engineerbiz.client.dto.ClientResponse;
import com.engineerbiz.client.dto.CreateClientRequest;
import com.engineerbiz.client.dto.UpdateClientRequest;
import com.engineerbiz.client.enums.ClientStatus;
import com.engineerbiz.client.enums.ClientType;
import com.engineerbiz.client.service.ClientService;
import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client Management", description = "Corporate and Individual Client Directory")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENT_CREATE')")
    @Operation(summary = "Register a new client")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(@Valid @RequestBody CreateClientRequest request) {
        ClientResponse response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_VIEW')")
    @Operation(summary = "Get client details by ID")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(@PathVariable UUID id) {
        ClientResponse response = clientService.getClientById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENT_VIEW')")
    @Operation(summary = "List and search clients with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<ClientResponse>>> getAllClients(
            @RequestParam(required = false) ClientStatus status,
            @RequestParam(required = false) ClientType clientType,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<ClientResponse> response = clientService.getAllClients(status, clientType, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_UPDATE')")
    @Operation(summary = "Update client information")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientRequest request) {
        ClientResponse response = clientService.updateClient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Client updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_DELETE')")
    @Operation(summary = "Soft-delete client")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.success("Client deleted successfully"));
    }
}
