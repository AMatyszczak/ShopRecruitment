package com.example.friendly_fishstick;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    public static final String ROLE_PREFIX = "ROLE_";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.GET, "/api/v1/store/**").hasAnyRole(Role.ADMIN.name(), Role.CUSTOMER.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/store/**").hasAnyRole(Role.ADMIN.name(), Role.CUSTOMER.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole(Role.ADMIN.name())
                )
                .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
                .httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationEventPublisher.class)
    DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(ApplicationEventPublisher delegate) {
        return new DefaultAuthenticationEventPublisher(delegate);
    }

    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("customer")
                .password("password")
                .roles(Role.CUSTOMER.name())
                .build();
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles(Role.ADMIN.name())
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        var hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(ROLE_PREFIX + Role.ADMIN + ">" + ROLE_PREFIX + Role.CUSTOMER);
        return hierarchy;
    }
}

