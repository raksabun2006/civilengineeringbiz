package com.engineerbiz.client.mapper;

import com.engineerbiz.client.dto.ClientResponse;
import com.engineerbiz.client.dto.CreateClientRequest;
import com.engineerbiz.client.dto.UpdateClientRequest;
import com.engineerbiz.client.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse toResponse(Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    Client toEntity(CreateClientRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientCode", ignore = true)
    void updateEntityFromRequest(UpdateClientRequest request, @MappingTarget Client client);
}
