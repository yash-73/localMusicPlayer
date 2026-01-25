package com.altspot.local.payload;

import com.altspot.local.model.AppRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String username;
    private String password;
    Set<AppRole> roles;

    public UserDTO(String username, String password){
        this.username = username;
        this.password = password;
    }

}
