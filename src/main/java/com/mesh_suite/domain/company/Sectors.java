package com.mesh_suite.domain.company;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "sectors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "sectorSetup")
@JsonIgnoreProperties({"sectorSetup"})
public class Sectors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_setup_id")
    @JsonBackReference
    private SectorSetup sectorSetup;

    private String parentSector;

    @ElementCollection
    @CollectionTable(name = "sub_sectors", joinColumns = @JoinColumn(name = "sector_id"))
    @Column(name = "sub_sector")
    private Set<String> subSector = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sectors)) return false;
        Sectors sectors = (Sectors) o;
        return Objects.equals(id, sectors.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sectors{id=" + id + ", parentSector='" + parentSector + "', subSector=" + subSector + "}";
    }
}
