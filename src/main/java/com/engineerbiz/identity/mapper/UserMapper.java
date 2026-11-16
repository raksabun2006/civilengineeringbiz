package com.engineerbiz.identity.mapper;

import com.engineerbiz.identity.dto.PermissionResponse;
import com.engineerbiz.identity.dto.UserResponse;
import com.engineerbiz.identity.entity.Permission;
import com.engineerbiz.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "permissions", source = "permissions")
    @Mapping(target = "role", source = "user.role")
    UserResponse toResponse(User user, Set<String> permissions);

    PermissionResponse toResponse(Permission permission);

    List<PermissionResponse> toPermissionResponseList(List<Permission> permissions);
}
