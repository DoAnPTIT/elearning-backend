package com.doanptit.elearing_backend_service.configuration;

import com.doanptit.elearing_backend_service.service.JwtBlacklistService;
import io.jsonwebtoken.JwtException;
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
import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                username = jwtUtil.extractUsername(token);
            } catch (JwtException e) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, handleJwtExceptionMessage(e));
                return;
            }
        }

        if (token != null && jwtBlacklistService.isTokenBlacklisted(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token đã bị vô hiệu hóa (Blacklisted).");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ hoặc đã bị thay đổi.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String handleJwtExceptionMessage(JwtException e) {
        if (e instanceof io.jsonwebtoken.ExpiredJwtException) {
            return "Token đã hết hạn.";
        } else if (e instanceof io.jsonwebtoken.MalformedJwtException) {
            return "Token không đúng định dạng.";
        } else if (e instanceof io.jsonwebtoken.SignatureException) {
            return "Chữ ký token không hợp lệ.";
        } else {
            return "Token không hợp lệ.";
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.write("{\"error\": \"" + message + "\"}");
        writer.flush();
    }
}