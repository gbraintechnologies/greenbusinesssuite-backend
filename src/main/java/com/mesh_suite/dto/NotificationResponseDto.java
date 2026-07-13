package com.mesh_suite.dto;

import com.mesh_suite.domain.notify.NotificationMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private boolean first;
    private boolean last;
    private long totalElements;
    private int totalPages;
    private int size;
    private List<NotificationMessage> content;
}
