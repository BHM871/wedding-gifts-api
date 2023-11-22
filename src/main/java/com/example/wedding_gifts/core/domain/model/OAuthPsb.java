package com.example.wedding_gifts.core.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_oauth_psb")
@Getter
@Setter
public class OAuthPsb {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(unique = true)
    private String authToken;

    private LocalDateTime expiration;

    public OAuthPsb(
        String id,
        String token,
        Long expiration
    ) {
        this.id = generateId();
        this.authToken = token;
        this.expiration = LocalDateTime.now().plusSeconds(expiration);
    }

    public OAuthPsb(
        String token,
        Long expiration
    ) {
        this.id = generateId();
        this.authToken = token;
        this.expiration = LocalDateTime.now().plusSeconds(expiration);
    }

    public OAuthPsb() {
        this.id = generateId();
        this.authToken = null;
        LocalDateTime.now();
    }

    private UUID generateId(){
        return UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    }

}