package com.mesh_suite.domain.coutry;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Entity
@Table(name = "jurisdiction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jurisdiction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "country_id")
    private Long countryId;
    @OneToOne(mappedBy = "jurisdiction", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private ParentAddressScheme parentAddressScheme;
}