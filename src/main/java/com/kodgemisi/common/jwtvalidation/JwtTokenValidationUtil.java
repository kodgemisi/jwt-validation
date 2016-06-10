package com.kodgemisi.common.jwtvalidation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.BiFunction;

@Component
public class JwtTokenValidationUtil implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BiFunction<Claims, String, String> getString = (claims, key) -> {
        Object v = claims.get(key);
        return v != null ? String.valueOf(v) : null;
    };

    @Value("${jwt.publicKey}")
    private String publicKeyString;

    private RSAPublicKey publicKey;

    @PostConstruct
    private void initialize() {
        this.publicKey = loadPublicKey(publicKeyString);
    }

    public String getAClaimFromToken(final String token, final String claimKey) {
        String result = null;
        try {
            final Claims claims = getClaimsFromToken(token);
            result = this.getString.apply(claims, claimKey);
        } catch (Exception e) {
            logger.error("Exception", e);
        }
        return result;
    }

    public boolean validateToken(final String token) {
        try{
            return !isTokenExpired(token);
        }
        catch (io.jsonwebtoken.SignatureException e) {
            logger.error("SignatureException", e);
            return false;
        }
    }

    public Boolean isTokenExpired(final String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public  Date getExpirationDateFromToken(final String token) {
        final Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    private Claims getClaimsFromToken(final String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .getBody();

        return claims;
    }

    private RSAPublicKey loadPublicKey(String publicKeyContent) {
        publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
        final KeyFactory fact;
        try {
            fact = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        final X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        final RSAPublicKey pubKey;
        try {
            pubKey = (RSAPublicKey) fact.generatePublic(keySpecX509);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        return pubKey;
    }
}
