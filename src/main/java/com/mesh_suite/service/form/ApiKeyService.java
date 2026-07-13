package com.mesh_suite.service.form;

import com.mesh_suite.dao.form.ApiKeyRepository;
import com.mesh_suite.domain.form.ApiKey;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ApiKeyService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private JwtTokenProvider jwtUtil;

    public Long registerClient(ApiKey apiKey) {

        if (apiKeyRepository.findByUsername(apiKey.getUsername()).isPresent()) {
            throw new RuntimeException("Client already exists");
        }
        apiKey.setPassword(passwordEncoder.encode(apiKey.getPassword()));
        apiKey.setEnabled(true);

        return apiKeyRepository.save(apiKey).getId();
    }

    public ApiKey getApiKeyByUsername(String username) {
        return apiKeyRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }


   public String generateToken(String username, String password) {

        ApiKey apiKey = apiKeyRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!apiKey.getEnabled()) {
            throw new RuntimeException("API client disabled");
        }

        if (!passwordEncoder.matches(password, apiKey.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return jwtUtil.generateApiToken(apiKey);
    }

}
