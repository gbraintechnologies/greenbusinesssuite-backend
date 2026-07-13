package com.mesh_suite.security;

import com.mesh_suite.dao.company.UserCompanyRepository;
import com.mesh_suite.domain.form.ApiKey;
import com.mesh_suite.domain.user.Permission;
import com.mesh_suite.interceptor.TenantContext;
import com.mesh_suite.service.user.RoleService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtUserDetailsService userDetailsService;
    private final RoleService roleService;
    private final UserCompanyRepository userCompanyRepository;

    public static final String TOKEN_TYPE_API = "API_CLIENT";
    public static final String TOKEN_TYPE_USER = "USER";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long accessTokenValidity;

    private Key getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            log.warn("JWT secret not base64; falling back to raw bytes.");
            return Keys.hmacShaKeyFor(secretKey.getBytes());
        }
    }

    // ===========================
    // USER TOKENS
    // ===========================
    public String createAccessToken(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity);
        String tenantId = TenantContext.getCurrentTenant();

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", authorities)
                .claim("tenantid", tenantId)
                .claim("type", TOKEN_TYPE_USER)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createAccessTokenFromUsername(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity);
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        String tenantId = TenantContext.getCurrentTenant();

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", authorities)
                .claim("tenantid", tenantId)
                .claim("type", TOKEN_TYPE_USER)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ===========================
    // EXTERNAL API TOKENS
    // ===========================
    public String generateApiToken(ApiKey apiKey) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TOKEN_TYPE_API);
        claims.put("companyId", apiKey.getId()); // use ApiKey.id as companyId

        Date now = new Date();
        Date expiry = new Date(now.getTime() + 3600000); // 1 hour

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(apiKey.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String getUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        List<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
        userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(roleName -> roleName != null && !roleName.isBlank())
                .forEach(roleName -> {
                    String roleKey = roleName.startsWith("ROLE_") ? roleName.replaceFirst("ROLE_", "") : roleName;
                    List<Permission> permissions = roleService.getRolePermission(roleKey);
                    authorities.addAll(permissions.stream()
                            .map(p -> new SimpleGrantedAuthority(p.getName()))
                            .toList());
                });

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    public String getTenantIdFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("tenantid", String.class);
        } catch (Exception e) {
            log.trace("No tenantid claim present in token: {}", e.getMessage());
            return null;
        }
    }

    /** Extract tenantId claim (may be null) */
    public String getTenantId(String token) {

        return extractClaim(token, c -> c.get("tenantid", String.class));
    }
    /**
     * Validate token signature and expiry.
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public boolean isApiToken(String token) {
        return TOKEN_TYPE_API.equals(extractClaim(token, claims -> claims.get("type", String.class)));
    }

}
