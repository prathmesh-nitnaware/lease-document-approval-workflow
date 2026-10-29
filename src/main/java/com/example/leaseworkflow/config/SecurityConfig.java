package com.example.leaseworkflow.config;

import com.example.leaseworkflow.model.UserRole;
import com.example.leaseworkflow.service.PersonaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        String encodedPassword = passwordEncoder.encode("password123");

        UserDetails tenant = User.builder()
                .username("john.tenant")
                .password(encodedPassword)
                .roles("TENANT")
                .build();

        UserDetails prathmesh = User.builder()
                .username("prathmesh")
                .password(encodedPassword)
                .roles("TENANT")
                .build();

        UserDetails legal = User.builder()
                .username("sarah.legal")
                .password(encodedPassword)
                .roles("LEGAL_REVIEWER", "REVIEWER")
                .build();

        UserDetails finance = User.builder()
                .username("marcus.finance")
                .password(encodedPassword)
                .roles("FINANCE_MANAGER", "REVIEWER")
                .build();

        UserDetails admin = User.builder()
                .username("elena.admin")
                .password(encodedPassword)
                .roles("ADMIN", "REVIEWER", "LEGAL_REVIEWER", "FINANCE_MANAGER", "TENANT")
                .build();

        UserDetails adminUser = User.builder()
                .username("admin")
                .password(encodedPassword)
                .roles("ADMIN", "REVIEWER", "LEGAL_REVIEWER", "FINANCE_MANAGER", "TENANT")
                .build();

        return new InMemoryUserDetailsManager(tenant, prathmesh, legal, finance, admin, adminUser);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PersonaService personaService) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Static resources & Health check
                .requestMatchers("/css/**", "/js/**", "/images/**", "/actuator/**", "/error").permitAll()
                // Landing page, Quick Switcher, and Login page
                .requestMatchers("/", "/home", "/login", "/quick-login", "/switch-persona").permitAll()
                // Public and Demo Workflow paths
                .requestMatchers("/submit-request", "/track-request/**", "/requests/**", "/reviewer-queue", "/review-queue", "/reviewer/**", "/dashboard", "/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    String username = authentication.getName();
                    personaService.setCurrentPersona(request.getSession(), username);
                    request.getSession().setAttribute("USER_LOGGED_IN", true);
                    response.sendRedirect("/dashboard");
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
