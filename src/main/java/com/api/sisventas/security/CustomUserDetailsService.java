package com.api.sisventas.security;

import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.User;
import com.api.sisventas.models.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        boolean enabled = user.getStatus() == UserStatus.ACTIVE;
        // disabled => DaoAuthenticationProvider rechaza el signin; el filtro JWT lo comprueba aparte.
        return new AuthenticatedUser(user.getUsername(), user.getPassword(), enabled,
                authoritiesOf(user.getRoles()), user.getTokenVersion());
    }

    private Collection<GrantedAuthority> authoritiesOf(Set<Role> roles) {
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName().name()))
                .toList();
    }
}
