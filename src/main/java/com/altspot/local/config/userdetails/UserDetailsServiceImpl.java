package com.altspot.local.config.userdetails;

import com.altspot.local.model.User;
import com.altspot.local.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =  userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + username));

        List<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
                                                        .map(role -> new SimpleGrantedAuthority(role.getRole().name()))
                                                        .collect(Collectors.toList());

        return new UserDetailsImpl(user.getUsername(), user.getPassword(), grantedAuthorities);

    }
}
