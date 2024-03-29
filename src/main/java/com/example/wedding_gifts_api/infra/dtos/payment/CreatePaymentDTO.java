package com.example.wedding_gifts_api.infra.dtos.payment;

import com.example.wedding_gifts_api.core.domain.model.util.MethodOfPayment;

public record CreatePaymentDTO(
    String name,
    String cpf,
    String cnpj,
    MethodOfPayment method
){}
