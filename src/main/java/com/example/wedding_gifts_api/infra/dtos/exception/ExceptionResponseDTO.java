package com.example.wedding_gifts_api.infra.dtos.exception;

import java.time.LocalDateTime;

public record ExceptionResponseDTO(
    LocalDateTime timestamp,
    Integer status,
    String error,
    String exception,
    String message,
    String cause,
    String path
){}
