package com.example.wedding_gifts_api.application.auth;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wedding_gifts_api.adapters.security.TokenManagerAdapter;
import com.example.wedding_gifts_api.common.Validation;
import com.example.wedding_gifts_api.core.domain.exceptions.account.AccountExecutionException;
import com.example.wedding_gifts_api.core.domain.exceptions.account.AccountForbiddenException;
import com.example.wedding_gifts_api.core.domain.exceptions.account.AccountInvalidValueException;
import com.example.wedding_gifts_api.core.domain.exceptions.account.AccountNotNullableException;
import com.example.wedding_gifts_api.core.domain.exceptions.change_request.ChangeRequestExecutionException;
import com.example.wedding_gifts_api.core.domain.exceptions.change_request.ChangeRequestInvalidValueException;
import com.example.wedding_gifts_api.core.domain.exceptions.change_request.ChangeRequestNotNullableException;
import com.example.wedding_gifts_api.core.domain.exceptions.common.MyException;
import com.example.wedding_gifts_api.core.domain.exceptions.token.TokenInvalidValueException;
import com.example.wedding_gifts_api.core.domain.model.Account;
import com.example.wedding_gifts_api.core.domain.model.Token;
import com.example.wedding_gifts_api.core.usecases.account.IAccountRepository;
import com.example.wedding_gifts_api.core.usecases.auth.IAuthenticationController;
import com.example.wedding_gifts_api.core.usecases.auth.IAuthenticationService;
import com.example.wedding_gifts_api.core.usecases.token.ITokenUseCase;
import com.example.wedding_gifts_api.infra.dtos.account.AccountResponseAccountDTO;
import com.example.wedding_gifts_api.infra.dtos.account.CreateAccountDTO;
import com.example.wedding_gifts_api.infra.dtos.account.LoginDTO;
import com.example.wedding_gifts_api.infra.dtos.authentication.AuthenticationResponseDTO;
import com.example.wedding_gifts_api.infra.dtos.authentication.ChangePassLoggedDTO;
import com.example.wedding_gifts_api.infra.dtos.authentication.ChangePassNotLoggedDTO;
import com.example.wedding_gifts_api.infra.dtos.authentication.ForgotPassDTO;
import com.example.wedding_gifts_api.infra.dtos.change_request.ChangeRequestDTO;
import com.example.wedding_gifts_api.infra.dtos.commun.MessageDTO;
import com.example.wedding_gifts_api.infra.dtos.exception.ExceptionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthenticationController implements IAuthenticationController {

    @Autowired
    private IAccountRepository repository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private IAuthenticationService service;
    @Autowired
    private TokenManagerAdapter tokenManager;
    @Autowired
    private ITokenUseCase tokenService;

    @Override
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a Account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = AccountResponseAccountDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Already email"),
        @ApiResponse(responseCode = "422", description = "Invalid value in request body", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<AccountResponseAccountDTO> register(
        @RequestBody CreateAccountDTO account
    ) throws Exception {
        try{
            validData(account);

            if(repository.getByEmail(account.email()) != null) return ResponseEntity.status(HttpStatus.CONFLICT).build();

            String encrypPassword = new BCryptPasswordEncoder().encode(account.password());

            CreateAccountDTO createAccount = new CreateAccountDTO(
                account.firstName(), 
                account.lastName(), 
                account.brideGroom(), 
                account.weddingDate(), 
                account.email(), 
                encrypPassword, 
                account.pixKey()
            );

            Account savedAccount = repository.createAccount(createAccount);

            AccountResponseAccountDTO newAccount = new AccountResponseAccountDTO(
                savedAccount.getId(), 
                savedAccount.getBrideGroom(), 
                savedAccount.getWeddingDate(), 
                savedAccount.getFirstName(), 
                savedAccount.getLastName(), 
                savedAccount.getEmail());


            return ResponseEntity.status(HttpStatus.CREATED).body(newAccount);
        } catch (MyException e){
            e.setPath("/auth/register");
            throw e;
        } catch (Exception e){
            AccountExecutionException exception = new AccountExecutionException("Some error", e);
            exception.setPath("/auth/register");
            throw exception;
        }
    }

    @Override
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "To do login")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = AuthenticationResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processable request", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "422", description = "Invalid value in request body", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<AuthenticationResponseDTO> login(
        @RequestBody LoginDTO login
    ) throws Exception {
        try{
            validData(login);

            UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(login.email(), login.password());
            Authentication auth = this.authenticationManager.authenticate(usernamePassword);

            Token token = tokenManager.generatorToken((Account) auth.getPrincipal());

            if(auth.isAuthenticated()) return ResponseEntity.ok(
                new AuthenticationResponseDTO(
                    "Bearer", 
                    token.getTokenValue(), 
                    token.getLimitHour()
                )
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (MyException e){
            e.setPath("/auth/login");
            throw e;
        } catch (InternalAuthenticationServiceException e){
            MyException exception = new AccountForbiddenException("Not authenticate", e);
            exception.setPath("/auth/login");
            throw exception;
        } catch (Exception e){
            AccountExecutionException exception = new AccountExecutionException("Some error", e);
            exception.setPath("/auth/login");
            throw exception;
        }
    }

    @Override
    @PatchMapping(value = "/logout/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "To do logout")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = MessageDTO.class))),
        @ApiResponse(responseCode = "422", description = "Invalid param", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<MessageDTO> logout(
        @PathVariable String token
    ) throws Exception {
        try{
            if(tokenManager.validateToken(token) == null ) throw new TokenInvalidValueException("Token is invalid");
            
            tokenService.deleteToken(token);

            return ResponseEntity.ok(new MessageDTO("successfully"));
        } catch (MyException e){
            e.setPath("/auth/logout");
            throw e;
        } catch (Exception e){
            AccountExecutionException exception = new AccountExecutionException("Some error", e);
            exception.setPath("/auth/logout");
            throw exception;
        }
    }

    @Override
    @PostMapping(value = "/forget", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "This endpoint must be called if an user forget your password, and can't do login")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = ChangeRequestDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processing", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "422", description = "Some value is invalid", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<ChangeRequestDTO> forgotPassword(
        @RequestBody ForgotPassDTO forgetRequest
    ) throws Exception {
        try {
            validData(forgetRequest);

            ChangeRequestDTO request = service.forgotPassword(forgetRequest);

            return ResponseEntity.status(HttpStatus.OK).body(request);
        } catch (MyException e){
            e.setPath("/auth/forget");
            throw e;
        } catch (Exception e) {
            AccountExecutionException exception = new AccountExecutionException("Some error", e);
            exception.setPath("/auth/forget");
            throw exception;
        }
    }

    @Override
    @PutMapping(value = "/change/password/{request}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Change a password",
        description = "When an user that not know your password wants change your password, after of call '/auth/forget' endpoint, it must be use request's UUID to call this"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = MessageDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processing", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Forbbiden, someone is trying change something that are not its", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Request not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "422", description = "Some value is invalid", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<MessageDTO> changePassword(
        @PathVariable UUID request, 
        @RequestBody ChangePassNotLoggedDTO change
    ) throws Exception {
        try {
            validData(change);

            String message = service.changePassword(request, change);

            return ResponseEntity.status(HttpStatus.OK).body(new MessageDTO(message));
        } catch (MyException e){
            e.setPath("/auth/password");
            throw e;
        } catch (Exception e) {
            ChangeRequestExecutionException exception = new ChangeRequestExecutionException("Some error", e);
            exception.setPath("/auth/change/password");
            throw exception;
        }
    }

    @Override
    @PutMapping(value = "/password/{account}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Change a password", 
        description = "When an user wants change your password, even though he knows his password"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully", content = @Content(schema = @Schema(type = "object", implementation = MessageDTO.class))),
        @ApiResponse(responseCode = "400", description = "Some error in processing", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Forbbiden, someone is trying change something that are not its", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Request not found", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "406", description = "Some value is null", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "422", description = "Some value is invalid", content = @Content(schema = @Schema(type = "object", implementation = ExceptionResponseDTO.class)))
    })
    public ResponseEntity<MessageDTO> changePassword(
        @RequestHeader("Authorization") String token, 
        @PathVariable UUID account, 
        @RequestBody ChangePassLoggedDTO change
    ) throws Exception {
        try {
            tokenManager.validateSessionId(token, account);

            validData(change);

            String message = service.changePassword(account, change);

            return ResponseEntity.status(HttpStatus.OK).body(new MessageDTO(message));
        } catch (MyException e){
            e.setPath("/auth/password");
            throw e;
        }  catch (Exception e) {
            ChangeRequestExecutionException exception = new ChangeRequestExecutionException("Some error", e);
            exception.setPath("/auth/password");
            throw exception;
        }
    }

    private void validData(CreateAccountDTO data) throws Exception {
        String invalid = "'%s' is invalid";
        String isNull = "'%s' value is null";
        
        if(data.brideGroom() == null || data.brideGroom().isEmpty()) throw new AccountNotNullableException(String.format(isNull, "brideGroom"));
        if(data.weddingDate() == null) throw new AccountNotNullableException(String.format(isNull, "weddingDate"));
        if(data.firstName() == null || data.firstName().isEmpty()) throw new AccountNotNullableException(String.format(isNull, "firstName"));
        if(data.lastName() == null || data.lastName().isEmpty()) throw new AccountNotNullableException(String.format(isNull, "lastName"));
        if(data.email() == null || data.email().isEmpty()) throw new AccountNotNullableException(String.format(isNull, "email"));
        if(data.password() == null || data.password().isEmpty()) throw new AccountNotNullableException(String.format(isNull, "password"));
        if(data.pixKey() == null || data.pixKey().isEmpty()) throw new AccountNotNullableException(String.format(isNull, "pixKey"));
        
        if(!Validation.brideGroom(data.brideGroom())) throw new AccountInvalidValueException(String.format(invalid, "brideGroom"));
        if(!Validation.date(data.weddingDate()) || data.weddingDate().before(new Date())) throw new AccountInvalidValueException(String.format(invalid, "weddingDate"));
        if(!Validation.name(data.firstName(), data.lastName())) throw new AccountInvalidValueException(String.format(invalid, "firstName", "and/or", "lastName").replace("is", "are"));
        if(!Validation.email(data.email())) throw new AccountInvalidValueException(String.format(invalid, "email"));
        if(!Validation.password(data.password())) throw new AccountInvalidValueException(String.format(invalid, "password"));
        if(!Validation.pixKey(data.pixKey())) throw new AccountInvalidValueException(String.format(invalid, "pixKey"));
   
    }

    private void validData(LoginDTO data) throws Exception {
        String invalid = "'%s' is invalid";
        String isNull = "'%s' is null";

        if(data.email() == null || data.email().isEmpty()) throw new AccountNotNullableException(String.format(isNull, "email"));
        if(data.password() == null || data.password().isEmpty()) throw new AccountNotNullableException(String.format(isNull, "password"));
        
        if(!Validation.email(data.email())) throw new AccountInvalidValueException(String.format(invalid, "email"));
        if(!Validation.password(data.password())) throw new AccountInvalidValueException(String.format(invalid, "password"));
        
    }

    private void validData(ForgotPassDTO data) throws Exception {
        String invalid = "'%s' is invalid";

        if(
            (data.email() != null && data.email().isEmpty()) && 
            (data.brideGroom() != null && data.brideGroom().isEmpty())
        ) throw new AccountNotNullableException(("'email' and 'brideGroom' can't be null at the shame time"));

        if(
            data.brideGroom() != null && 
            !Validation.brideGroom(data.brideGroom())
        ) throw new AccountNotNullableException(String.format(invalid, "brideGroom"));
        if(
            data.email() != null && 
            !Validation.email(data.email())
        ) throw new AccountNotNullableException(String.format(invalid, "email"));
    }

    private void validData(ChangePassNotLoggedDTO data) throws Exception {
        String invalid = "'%s' is invalid";
        String isNull = "'%s' is null";

        if(data.password() == null || data.password().isEmpty()) throw new ChangeRequestNotNullableException(String.format(isNull, "password"));

        if(!Validation.password(data.password())) throw new ChangeRequestInvalidValueException(String.format(invalid, "password"));
    }

    private void validData(ChangePassLoggedDTO data) throws Exception {
        String invalid = "'%s' is invalid";
        String isNull = "'%s' is null";

        if(data.email() == null || data.email().isEmpty()) throw new ChangeRequestNotNullableException(String.format(isNull, "email"));
        if(data.lastPass() == null || data.lastPass().isEmpty()) throw new ChangeRequestNotNullableException(String.format(isNull, "lastPass"));
        if(data.newPass() == null || data.newPass().isEmpty()) throw new ChangeRequestNotNullableException(String.format(isNull, "newPass"));

        if(!Validation.email(data.email())) throw new ChangeRequestInvalidValueException(String.format(isNull, "email"));
        if(!Validation.password(data.lastPass())) throw new ChangeRequestInvalidValueException(String.format(invalid, "lastPass"));
        if(!Validation.password(data.newPass())) throw new ChangeRequestInvalidValueException(String.format(invalid, "newPass"));
    }
    
}
