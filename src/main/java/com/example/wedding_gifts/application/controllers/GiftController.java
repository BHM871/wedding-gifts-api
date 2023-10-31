package com.example.wedding_gifts.application.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.wedding_gifts.common.Validation;
import com.example.wedding_gifts.core.domain.dtos.commun.MessageDTO;
import com.example.wedding_gifts.core.domain.dtos.exception.ExceptionResponseDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.CreateGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.DeleteGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.GiftResponseDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.UpdateGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherDTO;
import com.example.wedding_gifts.core.domain.dtos.image.UpdateImageDTO;
import com.example.wedding_gifts.core.domain.exceptions.account.AccountNotNullableException;
import com.example.wedding_gifts.core.domain.exceptions.common.MyException;
import com.example.wedding_gifts.core.domain.exceptions.gift.GiftInvalidValueException;
import com.example.wedding_gifts.core.domain.exceptions.gift.GiftNotNullableException;
import com.example.wedding_gifts.core.domain.exceptions.image.ImageNotNullableException;
import com.example.wedding_gifts.core.usecases.gift.IGiftController;
import com.example.wedding_gifts.core.usecases.gift.IGiftUseCase;

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

    @Override
    @PostMapping(value = "/create", produces = {"application/json"}, consumes = {"multipart/form-data"})
    @Operation(
        summary = "Create a new gift",
        description = "Authentication is necessary."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = GiftResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "422", description = "Invalid param or invalid value in request body", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<GiftResponseDTO> createGift(
        CreateGiftDTO gift,
        @RequestPart(required = false) MultipartFile images[]
    ) throws Exception {
        try{
            validData(gift);

            return ResponseEntity.status(HttpStatus.CREATED).body(services.createGift(gift, images));
        } catch (MyException e){
            e.setPath("/gift/create");
            throw e;
        }
    }

    @Override
    @PutMapping(value = "/update", produces = {"application/json"}, consumes = {"application/json"})
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
        @RequestBody UpdateGiftDTO gift
    ) throws Exception {
        try{
            validData(gift);

            services.updateGift(gift);
            return ResponseEntity.ok(new MessageDTO("successfully"));
        } catch (MyException e){
            e.setPath("/gift/update");
            throw e;
        }
    }

    @Override
    @PutMapping(value = "/update/image", produces = {"application/json"}, consumes = {"multipart/form-data"})
    @Operation(
        summary = "Update image of a gift",
        description = "Authentication is necessary. 'imagesId' and 'images' can be null, but, not at the same time. The images in 'imagesId' will be deleted and 'images' will be added."
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
        UpdateImageDTO update,
        @RequestPart(required = false) MultipartFile images[]
    ) throws Exception {
        try{
            validData(update, images);

            services.updateGift(update, images);
            return ResponseEntity.ok(new MessageDTO("successfully"));
        } catch (MyException e){
            e.setPath("/gift/update/image");
            throw e;
        }
    }

    @Override
    @DeleteMapping(value = "/delete", produces = {"application/json"}, consumes = {"application/json"})
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
        @RequestBody DeleteGiftDTO ids
    ) throws Exception {
        try{
            validData(ids);

            services.deleteGift(ids);
            return ResponseEntity.ok(new MessageDTO("successfully"));
        } catch (MyException e){
            e.setPath("/gift/delete");
            throw e;
        }
    }

    @Override
    @DeleteMapping(value = "/delete/all/{account}", produces = {"application/json"})
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
        @PathVariable UUID account
    ) throws Exception {
        try{
            if(account == null) throw new AccountNotNullableException("Account id is null");

            services.deleteAllByAccount(account);
            return ResponseEntity.ok(new MessageDTO("successfully"));
        } catch (MyException e){
            e.setPath("/gift/delete/all");
            throw e;
        }
    }

    @Override
    @GetMapping(value = "/{account}", produces = {"application/json"})
    @Operation(summary = "Get all gifts by Account ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = List.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<List<GiftResponseDTO>> getAllGifts(
        @PathVariable UUID account
    ) throws Exception {
        try{
            return ResponseEntity.ok(services.getAllGifts(account));
        } catch (MyException e){
            e.setPath("/gift");
            throw e;
        }
    }


    @Override
    @GetMapping(value = "/filter/{account}", produces = {"application/json"})
    @Operation(summary = "Get all gifts by Account ID with filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = List.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
    })
    public ResponseEntity<List<GiftResponseDTO>> getWithFilter(
        @RequestBody SearcherDTO searcher,
        @PathVariable UUID account
    ) throws Exception {
        try{
            return ResponseEntity.ok(services.getWithFilter(searcher, account));
        } catch (MyException e){
            e.setPath("/gift/filter");
            throw e;
        }
    }
    
    private void validData(CreateGiftDTO data) throws Exception {
        String invalid = "%s is invalid";
        String isNull = "%s is null";
        
        if(data.title() == null || data.title().isEmpty()) throw new GiftNotNullableException(String.format(isNull, "title"));
        if(data.price() == null) throw new GiftNotNullableException(String.format(isNull, "price"));
        if(data.categories() == null || data.categories().isEmpty()) throw new GiftNotNullableException(String.format(isNull, "categories"));

        if(!Validation.title(data.title())) throw new GiftInvalidValueException(String.format(invalid, "title"));
        if(!Validation.description(data.giftDescription())) throw new GiftInvalidValueException(String.format(invalid, "description"));
        if(!Validation.price(data.price())) throw new GiftInvalidValueException(String.format(invalid, "price"));

    }

    private void validData(UpdateGiftDTO data) throws Exception {
        String isNull = "%s is null";

        if(data.giftId() == null) throw new GiftNotNullableException(String.format(isNull, "giftId"));
        if(data.accountId() == null) throw new GiftNotNullableException(String.format(isNull, "account"));

    }

    private void validData(UpdateImageDTO data, MultipartFile images[]) throws Exception {
        String isNull = "%s is null";

        if(data.giftId() == null) throw new GiftNotNullableException(String.format(isNull, "giftId"));
        if(data.accountId() == null) throw new GiftNotNullableException(String.format(isNull, "account"));
        if(data.imagesId() == null && images == null) throw new ImageNotNullableException(String.format(isNull, "images").replace("is", "are"));

    }

    private void validData(DeleteGiftDTO data) throws Exception {
        String isNull = "%s is null";

        if(data.giftId() == null) throw new GiftNotNullableException(String.format(isNull, "giftId"));
        if(data.accountId() == null) throw new GiftNotNullableException(String.format(isNull, "account"));

    }

}
