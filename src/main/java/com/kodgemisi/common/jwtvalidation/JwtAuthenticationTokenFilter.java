package com.kodgemisi.common.jwtvalidation;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JwtAuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JwtTokenValidationUtil jwtTokenUtil;

    @Value("${jwt.header}")
    private String tokenHeader;


    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final String authTokenHeaderValue = httpRequest.getHeader(this.tokenHeader);
        final Authentication existingAuthentication = SecurityContextHolder.getContext().getAuthentication();

        if(existingAuthentication == null && authTokenHeaderValue != null && authTokenHeaderValue.startsWith("Bearer ")) {
            final String authToken = authTokenHeaderValue.substring(7);

            if (jwtTokenUtil.validateToken(authToken)) {
                final String username = jwtTokenUtil.getAClaimFromToken(authToken, Claims.SUBJECT);
                final String roles = jwtTokenUtil.getAClaimFromToken(authToken, "rol");

                Collection<String> roleCollection = null;
                if(!isNullOrEmpty(roles)) {
                    roleCollection = Stream.of(roles.split(",")).filter(s -> !isNullOrEmpty(s)).collect(Collectors.toList());
                }

                final UserDetails userDetails = new UserDetailsImpl(roleCollection, username);

                final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else {
                // TODO differentiate fraudulent attempts (signature exceptions) and expired tokens!

                logger.error("Fraudulent authentication attempt!");

                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private Boolean isNullOrEmpty(String str) {
        return (str == null && str.isEmpty());
    }

}
