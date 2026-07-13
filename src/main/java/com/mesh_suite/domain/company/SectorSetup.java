package com.mesh_suite.domain.company;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "sector_setup")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectorSetup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String countryName;

    @OneToMany(mappedBy = "sectorSetup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Sectors> sectors = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SectorSetup)) return false;
        SectorSetup that = (SectorSetup) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
