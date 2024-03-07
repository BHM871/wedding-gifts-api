package com.example.wedding_gifts.infra.dtos.image;

import java.util.List;
import java.util.UUID;

public record InsertImagesDTO(
    UUID giftId,
    UUID accountId,
    List<String> images
){}