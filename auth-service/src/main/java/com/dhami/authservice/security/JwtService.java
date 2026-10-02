package com.dhami.authservice.security;

import com.dhami.authservice.config.JwtProperties;
import com.dhami.authservice.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey(){
       byte[] keyBytes =  Decoders.BASE64.decode(jwtProperties.secret());
       return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiry =
                now.plusSeconds(
                        jwtProperties.accessTtlSeconds()
                );

        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("email", user.getEmail())
                .claim("userId", String.valueOf(user.getId()))
                .claim("roles", user.getRoles().stream().map(r -> r.getName().name()).toList())
                .claim("typ", "access")
                .signWith(getSigningKey())
                .compact();
    }

    // generate refresh token.
    public String generateRefreshToken(UserEntity user, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(user.getEmail())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.refreshTtlSeconds())))
                .claim("typ", "refresh")
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public Date extractExpiration(String token) {
        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token)
                .before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {

        String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                &&
                !isTokenExpired(token);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);  // getId() reads the "jti" claim
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("typ", String.class));
    }

    /*
     For use in your security filter — access tokens only.
     Rejects refresh tokens even if signature is valid.
     */
    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            String typ      = extractTokenType(token);
            return "access".equals(typ)
                    && username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /*
     Verifies signature and typ claim only — no DB involved.
     DB state (revoked, expired) is checked in RefreshTokenService.
     */
    public boolean isRefreshTokenSignatureValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("typ", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
