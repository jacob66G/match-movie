package io.github.jacob66g.matchmovie.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Locale;

public class UserProvisioningFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final LocaleResolver localeResolver;

    public UserProvisioningFilter(UserService userService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver, LocaleResolver localeResolver) {
        this.userService = userService;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.localeResolver = localeResolver;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            try {
                Locale locale = localeResolver.resolveLocale(request);
                userService.syncUserFromToken(jwt, locale.toString());
            } catch (Exception ex) {
                handlerExceptionResolver.resolveException(request, response, null, ex);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
