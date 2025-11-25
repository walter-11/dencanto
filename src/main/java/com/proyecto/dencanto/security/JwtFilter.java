package com.proyecto.dencanto.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        String username = null;

        // DEBUG: Log de la petición
        String path = request.getRequestURI();
        logger.info("🔍 Procesando petición: " + path);

        // Intentar obtener el token del header Authorization
        final String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            logger.info("✅ Token encontrado en header Authorization");
        }

        // Si no hay token en el header, intentar obtener de la cookie
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        logger.info("✅ Token encontrado en cookie jwt_token");
                        break;
                    }
                }
            } else {
                logger.warn("⚠️ No hay cookies en la petición");
            }
        }

        // Validar el token si existe
        if (token != null) {
            try {
                username = jwtUtil.extractUsername(token);
                logger.info("✅ Token válido para usuario: " + username);
            } catch (Exception e) {
                logger.warn("❌ JWT inválido: " + e.getMessage());
            }
        } else {
            logger.warn("❌ No se encontró token en header ni en cookie");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("✅ Autenticación establecida para: " + username);
                } else {
                    logger.warn("❌ Validación de token falló");
                }
            } catch (Exception e) {
                logger.error("❌ Error al procesar usuario: " + e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }
}

