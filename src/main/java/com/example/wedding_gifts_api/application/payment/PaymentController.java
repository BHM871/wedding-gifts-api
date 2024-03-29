package com.example.wedding_gifts_api.application.payment;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wedding_gifts_api.adapters.security.TokenManagerAdapter;
import com.example.wedding_gifts_api.common.Validation;
import com.example.wedding_gifts_api.core.domain.exceptions.common.MyException;
import com.example.wedding_gifts_api.core.domain.exceptions.payment.PaymentExecutionException;
import com.example.wedding_gifts_api.core.domain.model.Payment;
import com.example.wedding_gifts_api.core.usecases.payment.IPaymentController;
import com.example.wedding_gifts_api.core.usecases.payment.IPaymentUseCase;
import com.example.wedding_gifts_api.infra.dtos.commun.MessageDTO;
import com.example.wedding_gifts_api.infra.dtos.exception.ExceptionResponseDTO;
import com.example.wedding_gifts_api.infra.dtos.payment.CreatePaymentDTO;
import com.example.wedding_gifts_api.infra.dtos.payment.GetPaymentByPaidDTO;
import com.example.wedding_gifts_api.infra.dtos.payment.PaymentResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/payment")
@Tag(name = "Payment")
public class PaymentController implements IPaymentController {

    @Autowired
    private IPaymentUseCase service;
    @Autowired
    private TokenManagerAdapter tokenManager;

    @Override
    @PostMapping(value = "/create/{gift}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generate a Payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = PaymentResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Gift ID sent not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "502", description = "Some error in processable request in a gateway", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
    })
    public ResponseEntity<PaymentResponseDTO> createPayment(
        @PathVariable(name = "gift") UUID gift,
        @RequestBody CreatePaymentDTO payment
    ) throws Exception {
        try {
            validData(payment);
            
            Payment newPayment = service.createPayment(gift, payment);
            PaymentResponseDTO responsePayment = new PaymentResponseDTO(
                newPayment.getId(), 
                gift, 
                payment.method(), 
                newPayment.getTransactionId(), 
                newPayment.getPaymentValue(), 
                newPayment.getPaymentDescription(), 
                newPayment.getPaymentCode(), 
                newPayment.getCreation(), 
                newPayment.getExpiration()
            ); 

            return ResponseEntity.status(HttpStatus.CREATED).body(responsePayment);
        } catch (MyException e){
            e.setPath("/payment/create");
            throw e;
        } catch (Exception e) {
            PaymentExecutionException exception = new PaymentExecutionException("Some error", e);
            exception.setPath("/payment/create");
            
            throw exception;
        }
    }

    @Override
    @GetMapping(value = "/isPaid/{payment}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verific if a payment is paid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = MessageDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Payment ID sent not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "502", description = "Some error in processable request in a gateway", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
    })
    public ResponseEntity<MessageDTO> isPaid(
        @PathVariable UUID payment
    ) throws Exception {
        try {
            String message = service.isPaid(payment) ? "YES" : "NO";

            return ResponseEntity.ok(new MessageDTO(message));
        } catch (MyException e){
            e.setPath("/payment/isPaid");
            throw e;
        } catch (Exception e) {
            PaymentExecutionException exception = new PaymentExecutionException("Some error", e);
            exception.setPath("/payment/isPaid");

            throw exception;
        }
    }

    @Override
    @GetMapping(value = "/isExpired/{payment}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verific if a payment is expired")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = MessageDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Payment ID sent not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "502", description = "Some error in processable request in a gateway", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
    })
    public ResponseEntity<MessageDTO> isExpired(
        @PathVariable UUID payment
    ) throws Exception {try {
            String message = service.isExpired(payment) ? "YES" : "NO";

            return ResponseEntity.ok(new MessageDTO(message));
        } catch (MyException e){
            e.setPath("/payment/isExpired");
            throw e;
        } catch (Exception e) {
            PaymentExecutionException exception = new PaymentExecutionException("Some error", e);
            exception.setPath("/payment/isExpired");

            throw exception;
        }
    }

    @Override
    @GetMapping(value = "/{account}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all Payment's")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = PaymentResponseDTO.class))),
    })
    public ResponseEntity<Page<Payment>> getAll(
        @RequestHeader("Authorization") String token,
        @PathVariable UUID account, 
        Pageable paging
    ) throws Exception {
        tokenManager.validateSessionId(token, account);

        return ResponseEntity.ok(service.getAllPayments(account, paging));
    }

    @Override
    @GetMapping(value = "/paid/{account}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Payment's by 'isPaid' attribute")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = PaymentResponseDTO.class))),
    })
    public ResponseEntity<Page<Payment>> getByIsPaid(
        @RequestHeader("Authorization") String token,
        @PathVariable UUID account,
        @RequestBody GetPaymentByPaidDTO paidFilter, 
        Pageable paging
    ) throws Exception {
        try {
            tokenManager.validateSessionId(token, account);

            validData(paidFilter);

            return ResponseEntity.ok(service.getByIsPaid(account, paidFilter, paging));
        } catch (MyException e){
            e.setPath("/payment/paid");
            throw e;
        } catch (Exception e) {
            PaymentExecutionException exception = new PaymentExecutionException("Some error", e);
            exception.setPath("/payment/paid");

            throw exception;
        }
    }

    private void validData(CreatePaymentDTO data) throws Exception {
        String invalid = "%s is invalid";
        String isNull = "%s is null";
        
        if((data.cpf() == null || data.cpf().isEmpty()) && (data.cnpj() == null || data.cnpj().isEmpty())) throw new Exception(String.format(isNull, "cpf and cnpj"));
        if(data.name() == null || data.name().isEmpty()) throw new Exception(String.format(isNull, "name"));

        if(data.cpf() != null && data.cnpj() != null) throw new Exception("cpf and cnpj can't be not null at same time");
        if(data.cpf() != null && !Validation.cpf(data.cpf()))  throw new Exception(String.format(invalid, "cpf"));
        if(data.cnpj() != null && !Validation.cnpj(data.cnpj()))  throw new Exception(String.format(invalid, "cnpj"));
        if(!Validation.name(data.name()))  throw new Exception(String.format(invalid, "name"));

    }

    private void validData(GetPaymentByPaidDTO data) throws Exception {
        String isNull = "%s is null";
        
        if(data.isPaid() == null) throw new Exception(String.format(isNull, "isPaid"));

    }

}