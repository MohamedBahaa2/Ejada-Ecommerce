package com.ejada.ecommerce.auth.mapper;

import com.ejada.ecommerce.auth.domain.AppUser;
import com.ejada.ecommerce.auth.domain.Role;
import com.ejada.ecommerce.auth.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleNames")
    UserResponse toResponse(AppUser user);

    @Named("roleNames")
    default Set<String> roleNames(Set<Role> roles) {
        return roles == null ? Set.of()
                : roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}