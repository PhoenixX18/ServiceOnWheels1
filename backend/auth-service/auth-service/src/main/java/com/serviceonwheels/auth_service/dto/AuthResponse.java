package com.serviceonwheels.auth_service.dto;

import com.serviceonwheels.auth_service.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private String userId;
    private String name;
    private String email;
    private Role role;
    private String message;
}
