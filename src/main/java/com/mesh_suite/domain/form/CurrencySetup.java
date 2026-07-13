package com.mesh_suite.domain.form;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "currency_setup")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencySetup implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "The name of the currency")
    private String currency;

    @Schema(description = "The symbol representing the currency")
    private String symbol;

    @Schema(description = "The country associated with the currency setup")
    private String countryName;

    @OneToMany(mappedBy = "currencySetup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    @JsonManagedReference
    @OrderBy("amount ASC")
    private List<Denomination> denominations = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdOn;

    @UpdateTimestamp
    private LocalDateTime updatedOn;
    private LocalDateTime deletedOn;

    @Column(name ="is_Deleted",nullable = false, columnDefinition = "boolean default false")
    @Schema(defaultValue = "false")
    private Boolean isDeleted = false;
}
