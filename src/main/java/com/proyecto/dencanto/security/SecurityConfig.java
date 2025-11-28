package com.proyecto.dencanto.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    // AuthenticationManager para uso en AuthController
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas - autenticación y páginas públicas
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/admin/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/", "/index", "/index.html").permitAll()
                .requestMatchers("/FAQ", "/FAQ.html").permitAll()
                .requestMatchers("/productos", "/productos.html").permitAll()
                .requestMatchers("/nosotros", "/nosotros.html").permitAll()
                .requestMatchers("/ubicanos", "/ubicanos.html").permitAll()
                .requestMatchers("/intranet/login").permitAll()
                // Recursos estáticos públicos (CSS, JS, imágenes)
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                // API de imágenes de productos (público - para página de productos)
                .requestMatchers("/api/imagen/**").permitAll()
                // API endpoints protegidos - requieren JWT
                .requestMatchers("/intranet/api/**").authenticated()
                // Rutas intranet protegidas
                .requestMatchers("/intranet/**").authenticated()
                // Protege todo lo demás
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Añadir filtro JWT antes del filtro de autenticación por username/password
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
