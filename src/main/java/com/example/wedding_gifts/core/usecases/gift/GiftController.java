package com.example.wedding_gifts.core.usecases.gift;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.example.wedding_gifts.core.domain.dtos.gift.CreateGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.UpdateGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherDTO;
import com.example.wedding_gifts.core.domain.model.Gift;

public interface GiftController {
    
    public ResponseEntity<Gift> createGift(CreateGiftDTO gift) throws Exception;

    public ResponseEntity<String> updateGift(UpdateGiftDTO gift, UUID id) throws Exception;

    public ResponseEntity<String> deleteGift(UUID id) throws Exception;

    public ResponseEntity<List<Gift>> getAllGifts();

    public ResponseEntity<List<Gift>> getWithFilter(SearcherDTO searcher) throws Exception;

}