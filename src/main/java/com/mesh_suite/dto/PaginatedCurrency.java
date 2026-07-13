package com.mesh_suite.dto;

import com.mesh_suite.domain.form.CurrencySetup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedCurrency implements Serializable {
    private boolean first;
    private boolean last;
    private long totalElements;
    private int totalPages;
    private int size;
    private List<CurrencySetup> content;
}
