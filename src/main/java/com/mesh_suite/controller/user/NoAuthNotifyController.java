package com.mesh_suite.controller.user;

import com.mesh_suite.dto.request.TempCredentialRequest;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.exception.BadRequestException;
import com.mesh_suite.service.user.UserCompatibilityService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mesh-suite/v1.0/noauth")
@RequiredArgsConstructor
public class NoAuthNotifyController {

    private final UserCompatibilityService userCompatibilityService;

    @Operation(summary = "Email the temporary password created for a new user")
    @PostMapping({"/notify_user_temp_cred", "/notify_user_temp_cred/"})
    public ResponseEntity<MessageResponse> notifyTemporaryCredentials(@RequestBody TempCredentialRequest request) {
        if (request == null) {
            throw new BadRequestException("user_id is required");
        }
        return ResponseEntity.ok(userCompatibilityService.notifyTemporaryCredentials(request.getUserId(), request.getChannel()));
    }
}
