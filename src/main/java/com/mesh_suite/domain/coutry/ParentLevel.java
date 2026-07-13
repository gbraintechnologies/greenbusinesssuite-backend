package com.mesh_suite.domain.coutry;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "parent_levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentLevel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressing_scheme_id", nullable = false)
    @JsonBackReference
    private AddressingScheme addressingScheme;

    @Column(name = "parent_name", nullable = false)
    private String parentName;

    @ElementCollection
    @CollectionTable(name = "child_entries", joinColumns = @JoinColumn(name = "parent_level_id"))
    @Column(name = "child_entry", columnDefinition = "TEXT")
    private List<String> childLevels;

}
