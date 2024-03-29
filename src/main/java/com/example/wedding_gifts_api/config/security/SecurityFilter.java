package com.example.wedding_gifts_api.config.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.wedding_gifts_api.adapters.security.TokenManagerAdapter;
import com.example.wedding_gifts_api.core.usecases.account.IAccountRepository;
import com.example.wedding_gifts_api.core.usecases.token.ITokenUseCase;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@SuppressWarnings("null")
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenManagerAdapter tokenManager;
    @Autowired
    private IAccountRepository accountRepository;
    @Autowired
    private ITokenUseCase tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recoverToken(request);

        try {
            if(token != null && tokenService.validateToken(token) != null) {
                
                String subject = tokenManager.validateToken(token);
                String email = subject.split(",")[1];
                
                UserDetails userDetails = accountRepository.getByEmail(email);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
    
}
