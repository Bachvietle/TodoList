package com.example.TodoList.Service;

import com.example.TodoList.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@Service
public class JwtService {

    private final SecretKey jwtSecretKey;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final Clock clock;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration}") long accessExpiration,
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration,
            Clock clock ) {

        this.jwtSecretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.clock = clock;
    }

    public String generateAccessJwt(User user){

        return Jwts.builder()
                .setSubject(user.getEmail()) // object unique
                .setId(UUID.randomUUID().toString()) // id
                .claim("role", user.getRole()) // role
                .setIssuedAt(Date.from(Instant.now())) // thời điểm tạo
                .setExpiration(Date.from(Instant.now().plusMillis(accessExpiration))) // thời điểm hết hạn
                .signWith(jwtSecretKey) // encoder Jwt
                .compact();
    }

    public String generateRefreshJwt(User user){

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setId(UUID.randomUUID().toString())
                .claim("role", user.getRole())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(refreshExpiration)))
                .signWith(jwtSecretKey)
                .compact();
    }

    /*
    Refresh token cần setID vì user có thể đăng nhập trên nhiều thiết bị cùng 1 lúc, vì vậy setSub là ko đủ để phân biệt.

  => Khi cần đăng xuất tk khỏi 1 thiết bị, chỉ cần xóa token của thiết bị đó.
     */


    // Các hàm cần thiết trong JwtAuthenFilter
    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token) //Ktra Chữ ký (Signature): Xem token có bị sửa đổi không
                                       //Ktra expiryDate
                .getBody();
    }

    public String extractSubject(String token){
        return extractAllClaims(token).getSubject();
    }
}
