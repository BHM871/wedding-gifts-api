package com.example.wedding_gifts_api.application.gift;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wedding_gifts_api.adapters.security.TokenManagerAdapter;
import com.example.wedding_gifts_api.common.Validation;
import com.example.wedding_gifts_api.core.domain.exceptions.common.MyException;
import com.example.wedding_gifts_api.core.domain.exceptions.gift.GiftExecutionException;
import com.example.wedding_gifts_api.core.domain.exceptions.gift.GiftInvalidValueException;
import com.example.wedding_gifts_api.core.domain.exceptions.gift.GiftNotNullableException;
import com.example.wedding_gifts_api.core.usecases.gift.IGiftController;
import com.example.wedding_gifts_api.core.usecases.gift.IGiftUseCase;
import com.example.wedding_gifts_api.infra.dtos.commun.MessageDTO;
import com.example.wedding_gifts_api.infra.dtos.exception.ExceptionResponseDTO;
import com.example.wedding_gifts_api.infra.dtos.gift.CreateGiftDTO;
import com.example.wedding_gifts_api.infra.dtos.gift.GiftResponseDTO;
import com.example.wedding_gifts_api.infra.dtos.gift.UpdateGiftDTO;
import com.example.wedding_gifts_api.infra.dtos.gift.searchers.SearcherDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag; 

@RestController
@RequestMapping("/gift")
@Tag(name = "Gift")
public class GiftController implements IGiftController {

    @Autowired
    private IGiftUseCase services;
    @Autowired
    private TokenManagerAdapter tokenManager;

    @Override
    @PostMapping(value = "/create/{account}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Create a new gift",
        description = "Authentication is necessary. Send to image in base64 format. Limit image size is 5MB"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = GiftResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "422", description = "Invalid param or invalid value in request body", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<GiftResponseDTO> createGift(
        @RequestHeader("Authorization") String token,
        @PathVariable UUID account,
        @RequestBody CreateGiftDTO gift
    ) throws Exception {
        try{
            tokenManager.validateSessionId(token, account);

            validData(gift);

            return ResponseEntity.status(HttpStatus.CREATED).body(services.createGift(account, gift));
        } catch (MyException e){
            e.setPath("/gift/create");
            throw e;
        } catch (Exception e){
            GiftExecutionException exception = new GiftExecutionException("Some error", e);
            exception.setPath("/gift/create");
            throw exception;
        }
    }

    @Override
    @PutMapping(value = "/update/{account}/{gift}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Update a gift",
        description = "Authentication is necessary, values can be null."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = MessageDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Unauthorizated", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Gift or Account not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "422", description = "Invalid param or invalid value in request body", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<MessageDTO> updateGift(
        @RequestHeader("Authorization") String token,
        @PathVariable UUID account,
        @PathVariable UUID gift,
        @RequestBody UpdateGiftDTO update
    ) throws Exception {
        try{
            tokenManager.validateSessionId(token, account);

            services.updateGift(account, gift, update);

            return ResponseEntity.ok(new MessageDTO("successfully"));
        } catch (MyException e){
            e.setPath("/gift/update");
            throw e;
        } catch (Exception e){
            GiftExecutionException exception = new GiftExecutionException("Some error", e);
            exception.setPath("/gift/update");
            throw exception;
        }
    }

    @Override
    @DeleteMapping(value = "/delete/{account}/{gift}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Delete a gift by ID",
        description = "Authentication is necessary."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = MessageDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Unauthorizated", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Gift or Account not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "422", description = "Invalid param or invalid value in request body", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<MessageDTO> deleteGift(
        @RequestHeader("Authorization") String token,
        @PathVariable UUID account,
        @PathVariable UUID gift
    ) throws Exception {
        try{
            tokenManager.validateSessionId(token, account);

            services.deleteGift(account, gift);
            
            return ResponseEntity.ok(new MessageDTO("successfully"));
        } catch (MyException e){
            e.setPath("/gift/delete");
            throw e;
        } catch (Exception e){
            GiftExecutionException exception = new GiftExecutionException("Some error", e);
            exception.setPath("/gift/delete");
            throw exception;
        }
    }

    @Override
    @DeleteMapping(value = "/delete/all/{account}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Delete all gifts by Account ID",
        description = "Authentication is necessary."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = MessageDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Unauthorizated", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Gift or Account not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "422", description = "Invalid param or invalid value in request body", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<MessageDTO> deleteAllByAccount(
        @RequestHeader("Authorization") String token,
        @PathVariable UUID account
    ) throws Exception {
        try{
            tokenManager.validateSessionId(token, account);

            services.deleteAllByAccount(account);
            return ResponseEntity.ok(new MessageDTO("successfully"));
        } catch (MyException e){
            e.setPath("/gift/delete/all");
            throw e;
        } catch (Exception e){
            GiftExecutionException exception = new GiftExecutionException("Some error", e);
            exception.setPath("/gift/delete/all");
            throw exception;
        }
    }

    @Override
    @GetMapping(value = "/{account}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all gifts by Account ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = List.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<Page<GiftResponseDTO>> getAllGifts(
        @PathVariable UUID account,
        @PageableDefault Pageable paging
    ) throws Exception {
        try{
            return ResponseEntity.ok(services.getAllGifts(account, paging));
        } catch (MyException e){
            e.setPath("/gift");
            throw e;
        } catch (Exception e){
            GiftExecutionException exception = new GiftExecutionException("Some error", e);
            exception.setPath("/gift");
            throw exception;
        }
    }


    @Override
    @GetMapping(value = "/filter/{account}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all gifts by Account ID with filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = List.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
    })
    public ResponseEntity<Page<GiftResponseDTO>> getWithFilter(
        @RequestBody SearcherDTO searcher,
        @PathVariable UUID account,
        @PageableDefault Pageable paging
    ) throws Exception {
        try{
            return ResponseEntity.ok(services.getWithFilter(searcher, account, paging));
        } catch (MyException e){
            e.setPath("/gift/filter");
            throw e;
        } catch (Exception e){
            GiftExecutionException exception = new GiftExecutionException("Some error", e);
            exception.setPath("/gift/filter");
            throw exception;
        }
    }

    private void validData(CreateGiftDTO data) throws Exception {
        String invalid = "%s is invalid";
        String isNull = "%s is null";
        
        if(data.title() == null || data.title().isEmpty()) throw new GiftNotNullableException(String.format(isNull, "'title'"));
        if(data.price() == null) throw new GiftNotNullableException(String.format(isNull, "price"));
        if(data.categories() == null || data.categories().isEmpty()) throw new GiftNotNullableException(String.format(isNull, "'categories'"));

        if(!Validation.title(data.title())) throw new GiftInvalidValueException(String.format(invalid, "'title'"));
        if(data.giftDescription() != null && !Validation.description(data.giftDescription())) throw new GiftInvalidValueException(String.format(invalid, "'description'"));
        if(!Validation.price(data.price())) throw new GiftInvalidValueException(String.format(invalid, "'price'"));

    }
}
