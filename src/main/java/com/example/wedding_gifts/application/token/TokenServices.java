package com.example.wedding_gifts.application.token;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.wedding_gifts.core.domain.dtos.token.SaveTokenDTO;
import com.example.wedding_gifts.core.usecases.token.ITokenRepository;
import com.example.wedding_gifts.core.usecases.token.ITokenUseCase;

@Service
public class TokenServices implements ITokenUseCase {

    @Autowired
    private ITokenRepository repository;

    @Override
    public String saveToken(SaveTokenDTO tokenDTO) throws Exception {
        return repository.saveToken(tokenDTO);
    }

    @Override
    public String validateToken(String token) throws Exception {
        return repository.getToken(token);
    }

    @Override
    public String getTokenByAccount(UUID accounId) throws Exception {
        return repository.getTokenByAccount(accounId);
    }

    @Override
    public void deleteToken(String token) {
        repository.deleteToken(token);
    }

    @Override
    public void deleteTokenByAccount(UUID accountId) {
        repository.deleteTokenByAccount(accountId);
    }
    
}
