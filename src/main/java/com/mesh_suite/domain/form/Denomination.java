package com.mesh_suite.domain.form;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mesh_suite.util.FormUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "denomination")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Denomination implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "The unique identifier of the denomination")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "currency_setup_id")
    @JsonBackReference
    @Schema(hidden = true)
    private CurrencySetup currencySetup;

    @Column(name = "amount")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
    private BigDecimal amount;

    @Column(name = "name")
    @Schema(description = "The name of the denomination")
    private String name;

    @Enumerated(EnumType.STRING)
    @Schema(description = "The type of the denomination", allowableValues = {"Coin", "Note"})
    private FormUtils.DenominationType denominationType;
}
