package com.altspot.local.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    private String username;
    private String password;
    private Set<String> roles;

    public SignupRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
