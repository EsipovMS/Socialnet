package socialnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import socialnet.api.request.EmailRq;
import socialnet.api.request.PasswordSetRq;
import socialnet.api.request.PersonSettingsRq;
import socialnet.api.request.RegisterRq;
import socialnet.api.response.*;
import socialnet.aspects.OnlineStatusUpdatable;
import socialnet.service.AccountService;
import socialnet.service.EmailService;
import socialnet.service.PersonService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "account-controller", description = "Working with password, email and registration")
public class AccountController {
    private final AccountService accountService;
    private final EmailService emailService;
    private final PersonService personService;

    @OnlineStatusUpdatable
    @PutMapping("/email/recovery")
    @Operation(summary = "user email recovery")
    public void emailSet(@RequestHeader @Parameter(description = "Access Token", example = "JWT Token")
                         String authorization) {
        emailService.shiftEmailConfirm(authorization);
    }

    @PutMapping("/email")
    @Operation(summary = "set email")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = RegisterRs.class))}),
            @ApiResponse(responseCode = "400", description = "Name of error", content = {@Content(schema = @Schema(implementation = ErrorRs.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {@Content(schema = @Schema())})})
    public RegisterRs setNewEmail(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "emailRq")
                                      @RequestBody EmailRq emailRq) {
        return personService.setNewEmail(emailRq);
    }

    @OnlineStatusUpdatable
    @PutMapping("/password/recovery")
    @Operation(summary = "user password recovery")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = RegisterRs.class))}),
            @ApiResponse(responseCode = "400", description = "Name of error", content = {@Content(schema = @Schema(implementation = ErrorRs.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {@Content(schema = @Schema())})})
    public void passwordChangeConfirm(@RequestHeader @Parameter(description = "Access Token", example = "JWT Token")
                                      String authorization) {
        emailService.passwordChangeConfirm(authorization);
    }

    @OnlineStatusUpdatable
    @PutMapping("/password/reset")
    @Operation(summary = "user password reset")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = RegisterRs.class))}),
            @ApiResponse(responseCode = "400", description = "Name of error", content = {@Content(schema = @Schema(implementation = ErrorRs.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {@Content(schema = @Schema())})})
    public RegisterRs resetPassword(@RequestHeader @Parameter(description = "Access Token", example = "JWT Token")
                                    String authorization,
                                    @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    description = "passwordSetRq")
                                    PasswordSetRq passwordSetRq) {
        return personService.resetPassword(authorization, passwordSetRq);
    }

    @OnlineStatusUpdatable
    @PutMapping("/password/set")
    @Operation(summary = "set user password")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = RegisterRs.class))}),
            @ApiResponse(responseCode = "400", description = "Name of error", content = {@Content(schema = @Schema(implementation = ErrorRs.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {@Content(schema = @Schema())})})
    public RegisterRs setNewPassword(@RequestHeader @Parameter(description = "Access Token", example = "JWT Token")
                                     String authorization,
                                     @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                     description = "passwordSetRq") PasswordSetRq passwordSetRq) {
        return personService.resetPassword(authorization, passwordSetRq);
    }

    @PostMapping("/register")
    @Operation(summary = "user registration")
    public RegisterRs register(@Valid @RequestBody @Parameter RegisterRq regRequest) {
        return accountService.getRegisterData(regRequest);
    }

    @OnlineStatusUpdatable
    @GetMapping("/notifications")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(ref = "#/components/schemas/CommonRsListPersonSettingsRs"))}),
            @ApiResponse(responseCode = "400", description = "Name of error", content = {@Content(schema = @Schema(implementation = ErrorRs.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {@Content(schema = @Schema())})})
    @Operation(summary = "get user's notifications properties")
    public CommonRs<List<PersonSettingsRs>> notifications(@RequestHeader
                                                          @Parameter(description = "Access Token", example = "JWT Token")
                                                          String authorization) {
        return personService.getPersonSettings(authorization);
    }

    @OnlineStatusUpdatable
    @PutMapping("/notifications")
    @Operation(summary = "edit notifications properties")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(ref = "#/components/schemas/CommonRsComplexRs"))}),
            @ApiResponse(responseCode = "400", description = "Name of error", content = {@Content(schema = @Schema(implementation = ErrorRs.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {@Content(schema = @Schema())})})
    public CommonRs<ComplexRs> saveSettings(@RequestHeader @Parameter(description = "Access Token", example = "JWT Token")
                                            String authorization,
                                            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                    description = "request") PersonSettingsRq personSettingsRq) {
        return personService.setSetting(authorization, personSettingsRq);
    }
}
