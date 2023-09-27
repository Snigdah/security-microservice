package com.example.securityservice.service;

import com.example.securityservice.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {
    public static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return  claimsResolver.apply(claims);
    }

    public Collection<? extends GrantedAuthority> extractAuthoritiesFromToken(String token) {
        Claims claims = extractAllClaims(token);

        // Extract roles from the "roles" claim in the JWT token
        List<String> roles = claims.get("roles", List.class);

        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        // Prefix each role with "ROLE_" and create SimpleGrantedAuthority objects
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }


    public String generateToken(UserDetails userDetails){
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("username", userDetails.getUsername());
//        claims.put("role", userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList()));
//
//        return generateToken(claims, userDetails);
        return generateToken(new HashMap<>(), userDetails);
    }



    // Generate token
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ){
        // Extract user roles as a list of strings
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Add roles to the extra claims
        extraClaims.put("roles", roles);
        return Jwts
                .builder()
                .setClaims(extraClaims)
                //.claim("role", userDetails.getAuthorities())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // check token valid
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String userName = extractUsername(token);

        if (!userName.equals(userDetails.getUsername()) || isTokenExpired(token)) {
            throw new AuthenticationException(HttpStatus.UNAUTHORIZED, "Invalid  token");
        }
        return true; // Token is valid
    }

    // check token expired
    private boolean isTokenExpired(String token) {
        if (extractExpiration(token).before(new Date())) {
            throw new AuthenticationException(HttpStatus.UNAUTHORIZED, "Token has expired");
        }
        return false; // Token is not expired
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // extract all claim
    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
