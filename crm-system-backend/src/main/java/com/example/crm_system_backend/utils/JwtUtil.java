package com.example.crm_system_backend.utils;

import com.example.crm_system_backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "mysecretkeymysecretkeymysecretkeymysecretkey"; // must be 32+ chars
    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    /**
     * Generates and returns the signing key used for creating or validating JWTs.
     *
     * @return the signing key derived from the secret key using
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Generates a JWT (JSON Web Token) for the given user.
     *
     * @param user the user object for whom the token is being generated. It must contain
     *             the user's ID, email, and role, which will be included as claims in the token.
     * @return a signed JWT as a String containing information about the user's ID, email,
     * and role, along with issuance and expiration dates.
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the provided JWT (JSON Web Token) and extracts the subject from it.
     * The method parses the token to verify its signature and ensure it is valid.
     *
     * @param token the JWT as a String that needs to be validated and parsed
     * @return the subject of the token if validation is successful; returns null if the token is invalid or an error occurs
     */
    public String validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Extracts all claims from the provided JWT (JSON Web Token).
     * The method parses the token to retrieve all information encapsulated
     * within the claims section of the token, including standard and custom claims.
     *
     * @param token the JWT as a String from which claims need to be extracted
     * @return the claims object that contains all the extracted claims from the token
     */
    public Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    /**
     * Extracts and returns the user ID from the provided JWT (JSON Web Token).
     * The ID is retrieved from the "subject" claim of the token after parsing it.
     *
     * @param token the JWT as a String
     */
    public Long getId(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    /**
     * Extracts and returns the email address from the provided JSON Web Token (JWT).
     * The email address is retrieved from the "email" claim within the token.
     *
     * @param token the JSON Web Token (JWT) as a String from which the email claim needs to be extracted
     * @return the email address as a String if it exists in the token, or null if the claim is missing or invalid
     */
    public String getEmail(String token) {
        return extractAllClaims(token).get("email").toString();
    }

    /**
     * Extracts and returns the role of a user from the given JWT (JSON Web Token).
     * The role is retrieved from the "role" claim within the token.
     *
     * @param token the JSON Web Token (JWT) as a String from which the role claim needs to be extracted
     * @return the role of
     */
    public String getRole(String token) {
        return extractAllClaims(token).get("role").toString();
    }

    /**
     * Checks whether the provided JWT (JSON Web Token) has expired.
     * The method determines the expiration status by extracting the expiration
     * date claim from the token and comparing it with the current date and time.
     *
     * @param token the JWT as a String to be checked for expiration
     * @return true if the token has expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            // Token is expired, return true without throwing exception
            return true;
        }
    }

}
