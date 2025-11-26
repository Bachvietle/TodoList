package com.example.TodoList.Config;

import com.example.TodoList.Service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check Header xem có Jwt ko
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        try{
            // Lấy jwt
            jwt = authHeader.substring(7);

            // Lấy email (nếu jwt đã hết hạn, sẽ throw Exception)
            userEmail = jwtService.extractSubject(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                    // Tạo Authentication token và set vào SecurityContext
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,// Credentials (password) không cần vì đã validate JWT
                            userDetails.getAuthorities() // Các roles/authorities của user
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                /*
                Method buildDetails extract thông tin từ request:
                - Remote address: Địa chỉ IP của client.
                - Session ID: ID của session HTTP nếu tồn tại (giúp track session).

                rồi gắn ttin này vào authToken
                 */

                    // Set vào trong SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
        } catch (Exception e){
            logger.error("Lỗi xác thực JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
