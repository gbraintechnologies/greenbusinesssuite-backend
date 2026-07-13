package com.mesh_suite.domain.coutry;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "child_address_scheme_entries")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Child Address Entries Schema", name = "Child address Schema")
public class ChildAddressSchemeEntries implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "parent_address_scheme_entries_id")
    @JsonBackReference
    private ParentAddressSchemeEntries parentAddressSchemeEntries;
}