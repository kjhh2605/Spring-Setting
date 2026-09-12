package com.example.auth.adapter.in.web;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.adapter.in.web.docs.DevTokenControllerDocs;
import com.example.auth.application.port.in.command.DevTokenUseCase;
import com.example.auth.application.port.in.command.dto.DevTokenCommand;

@RestController
@Validated
@Profile("dev & !prod")
public class DevTokenController implements DevTokenControllerDocs {
    private final DevTokenUseCase useCase;

    public DevTokenController(DevTokenUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @PostMapping("/api/v1/auth/dev/tokens")
    public TokenResponse issue(
            @RequestHeader("X-Dev-Master-Key") String secret,
            @RequestBody DevTokenRequest request,
            HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return TokenResponse.from(useCase.issue(new DevTokenCommand(secret, request.userId())));
    }
}
