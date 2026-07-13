package com.mesh_suite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocIssueDetails {

    private Long companyId;
    private Long userId;
    private String docLink;
	
	}