package com.mesh_suite.domain.coutry;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parent_address_scheme")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "parent address scheme table", name = "parent address scheme")
public class ParentAddressScheme implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "jurisdiction_id")
    @JsonBackReference
    private Jurisdiction jurisdiction;
    @OneToMany(mappedBy = "parentAddressScheme", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ParentAddressSchemeEntries> parentAddressSchemeEntries = new ArrayList<>();
    private String inputType;
}