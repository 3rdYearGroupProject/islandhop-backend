package com.tourism.userservices.security;

import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import java.io.IOException;

public class FirebaseAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public FirebaseAuthenticationFilter() {
        super("/**"); // Apply to all endpoints, adjust as needed
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        // TODO: Implement Firebase token extraction and authentication logic here
        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        chain.doFilter(request, response);
    }
}