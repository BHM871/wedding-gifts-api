package com.example.wedding_gifts.core.domain.dtos.image;

import java.awt.image.BufferedImage;
import java.util.UUID;

public record ImageDTO(
    BufferedImage image,
    UUID giftId,
    UUID accountId
){}
