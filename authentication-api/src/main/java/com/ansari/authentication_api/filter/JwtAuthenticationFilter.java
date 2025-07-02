package com.ansari.authentication_api.filter;

import com.ansari.authentication_api.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    public static final List<String> PUBLIC_URLS = List.of(
            "/login", "/register", "/send-reset-otp", "/reset-password", "/logout",
            "/swagger-ui.html", "/swagger-ui/", "/swagger-ui/index.html",
            "/v3/api-docs", "/v3/api-docs/", "/v3/api-docs/swagger-config",
            "/v3/api-docs/**", "/swagger-ui/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        System.out.println("Path: " + path);
        // check if url is public no need to authenticate
        if (isPublicPath(path)) {
            System.out.println("isPublic: " + isPublicPath(path));
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = null;
        String email = null;

        final String authHeader = request.getHeader("Authorization");
        //check authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }
        //if token not found in header , check in cookies
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }
        //validate the token and set the security context.
        if (jwt != null) {

            email = jwtUtil.extractUsername(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_URLS.stream().anyMatch(publicPath ->
                publicPath.endsWith("/**") ?
                        path.startsWith(publicPath.replace("/**", "")) :
                        path.equals(publicPath)
        );
    }

}

