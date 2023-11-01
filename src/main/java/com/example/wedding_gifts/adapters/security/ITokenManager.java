package com.example.wedding_gifts.adapters.security;

import com.example.wedding_gifts.core.domain.model.Account;

public interface ITokenManager {

    public String generatorToken(Account account) throws Exception;

    public String validateToken(String token);

}