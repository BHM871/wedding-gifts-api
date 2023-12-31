package com.example.wedding_gifts.core.usecases.gift;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import com.example.wedding_gifts.core.domain.dtos.commun.MessageDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.CreateGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.DeleteGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.GiftResponseDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.UpdateGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherDTO;

public interface IGiftController {
    
    public ResponseEntity<GiftResponseDTO> createGift(CreateGiftDTO gift) throws Exception;

    public ResponseEntity<MessageDTO> updateGift(UpdateGiftDTO gift) throws Exception;

    public ResponseEntity<MessageDTO> deleteGift(DeleteGiftDTO ids) throws Exception;

    public ResponseEntity<MessageDTO> deleteAllByAccount(UUID accountId) throws Exception;

    public ResponseEntity<Page<GiftResponseDTO>> getAllGifts(UUID accountId, Pageable page) throws Exception;

    public ResponseEntity<Page<GiftResponseDTO>> getWithFilter(SearcherDTO searcher, UUID accountId, Pageable page) throws Exception;

}
