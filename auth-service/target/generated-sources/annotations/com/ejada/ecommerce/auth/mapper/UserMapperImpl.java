package com.ejada.ecommerce.auth.mapper;

import com.ejada.ecommerce.auth.domain.AppUser;
import com.ejada.ecommerce.auth.dto.UserResponse;
import java.time.Instant;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-30T01:16:27+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12 (Ubuntu)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(AppUser user) {
        if ( user == null ) {
            return null;
        }

        Set<String> roles = null;
        Long id = null;
        String username = null;
        String email = null;
        Instant createdAt = null;

        roles = roleNames( user.getRoles() );
        id = user.getId();
        username = user.getUsername();
        email = user.getEmail();
        createdAt = user.getCreatedAt();

        UserResponse userResponse = new UserResponse( id, username, email, roles, createdAt );

        return userResponse;
    }
}
