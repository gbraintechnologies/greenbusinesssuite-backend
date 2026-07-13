package com.mesh_suite.dto;

import com.mesh_suite.constant.notify.MediaType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaCenterDto {
    private Long id;
    @NotNull(message = "Media type is required")
    private MediaType mediaType;
    @NotNull(message = "Thumbnail is required for upload")
    private MultipartFile thumbnail;

    private String altText;

    private String heading;

    private String url;
    private Boolean isActive = false;
}
