package com.seb33.digitalWizardserver.auth.refresh;

import com.seb33.digitalWizardserver.auth.jwt.JwtTokenizer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping("/refresh")
@AllArgsConstructor
public class RefreshController {
    private final JwtTokenizer jwtTokenizer;

    @PostMapping
    public ResponseEntity<String> refreshAccessToken(HttpServletRequest request) { // 리프레쉬 토큰 받으면 엑세스 토큰 재발급
        String refreshToken = request.getHeader("Refresh");
        if (refreshToken != null) { // && refreshTokenHeader.startsWith("WishJWT ") 나중에 추가
//            String refreshToken = refreshTokenHeader.substring(8);
            try {
                Jws<Claims> claims = jwtTokenizer.getClaims(refreshToken, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()));

                String email = claims.getBody().getSubject();
                Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
                String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

                String accessToken = jwtTokenizer.generateAccessToken(claims.getBody(), email, expiration, base64EncodedSecretKey);
                return ResponseEntity.ok().header("Authorization", "WishJWT " + accessToken).body("Access token refreshed");
            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing refresh token");
        }
    }
} // 나중에 리플래쉬 토큰도 같이 재발급해줄까?..상의해보자