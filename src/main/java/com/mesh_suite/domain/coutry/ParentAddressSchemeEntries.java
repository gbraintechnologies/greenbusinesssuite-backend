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
@Table(name = "parent_address_scheme_entries")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "parent address scheme entries table", name = "parent address scheme entries")
public class ParentAddressSchemeEntries implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "parent_address_scheme_id")
    @JsonBackReference
    private ParentAddressScheme parentAddressScheme;
    @OneToMany(mappedBy = "parentAddressSchemeEntries", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChildAddressSchemeEntries> childAddressSchemeEntries = new ArrayList<>();
}