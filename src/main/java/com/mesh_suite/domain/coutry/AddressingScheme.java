package com.mesh_suite.domain.coutry;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "addressing_schemes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressingScheme implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_level_name", nullable = false)
    private String parentLevelName;

    @Column(name = "child_level_name", nullable = false)
    private String childLevelName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    @JsonBackReference
    private Country country;

    @OneToMany(mappedBy = "addressingScheme", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<ParentLevel> parentLevels;
}
