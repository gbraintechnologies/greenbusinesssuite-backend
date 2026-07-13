package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailDTO {
    private String emailSubject;
    private String recipientEmail;
    private String fileName;
    private String message;
    private boolean isHtml;
    private byte[] fileData;
}