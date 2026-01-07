package com.altspot.local.config;


import com.altspot.local.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    private RoleRepository roleRepository;

    public AppConfig(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


}
