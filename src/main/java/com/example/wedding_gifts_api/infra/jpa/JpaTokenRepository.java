package com.example.wedding_gifts_api.infra.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.wedding_gifts_api.core.domain.model.Token;


public interface JpaTokenRepository extends JpaRepository<Token, UUID> {

    public Optional<Token> findByTokenValue(String token);

    @Query(nativeQuery = true, value = "SELECT * " +
                                        "FROM tb_token " +
                                        "WHERE account_id = :account")
    public Optional<Token> findByAccount(@Param("account" )UUID account);
    
}
