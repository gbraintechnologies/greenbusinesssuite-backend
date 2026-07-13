package com.mesh_suite.service.company;

import com.mesh_suite.dao.company.SectorSetupRepository;
import com.mesh_suite.dao.company.SectorsRepository;
import com.mesh_suite.domain.company.SectorSetup;
import com.mesh_suite.domain.company.Sectors;
import com.mesh_suite.dto.SectorSetupDTO;
import com.mesh_suite.dto.SectorSetupRequestDto;
import com.mesh_suite.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SectorSetupService {
    @Autowired
    private SectorSetupRepository sectorSetupRepository;

    @Autowired
    private SectorsRepository sectorsRepository;
    public Page<SectorSetup> getAllSectorSetups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return sectorSetupRepository.findAll(pageable);
    }

    public SectorSetup getSectorSetupById(Long id) {
        Optional<SectorSetup> sectorSetupOptional = sectorSetupRepository.findById(id);
        return sectorSetupOptional.orElseThrow(() -> new ResourceNotFoundException("SectorSetup not found with id: " + id));
    }

    public Optional<SectorSetup> updateSectorSetup(SectorSetup sectorSetup) {
        return Optional.ofNullable(sectorSetupRepository.findById(sectorSetup.getId())
                .map(existingSectorSetup -> {
                    sectorSetup.setCountryName(sectorSetup.getCountryName());
                    return sectorSetupRepository.save(sectorSetup);
                })
                .orElseThrow(() -> new IllegalArgumentException("Sector setup with ID " + sectorSetup.getId() + " not found.")));
    }

    public void deleteSectorSetup(Long id) {
        sectorSetupRepository.findById(id)
                .ifPresentOrElse(
                        sectorSetup -> sectorSetupRepository.deleteById(id),
                        () -> {
                            throw new IllegalArgumentException("Sector setup with ID " + id + " not found.");
                        }
                );
    }

    public List<Map<String, Object>> getSectorStatisticsByCountryName(String countryName) {
        List<Object[]> results = sectorSetupRepository.findSectorStatisticsByCountryName(countryName);

        return results.stream().map(result -> {
            Map<String, Object> sectorSetupMap = new HashMap<>();
            sectorSetupMap.put("id", result[0]);
            sectorSetupMap.put("countryName", result[1]);
            sectorSetupMap.put("parentSectorCount", ((Number) result[2]).intValue());
            sectorSetupMap.put("subSectorCount", ((Number) result[3]).intValue());
            return sectorSetupMap;
        }).collect(Collectors.toList());
    }
    public Map<String, Object> getSectorDetailsByIds(Long sectorSetupId, Long sectorId) {
        log.info("Fetching SectorSetup with ID: {}", sectorSetupId);

        SectorSetup sectorSetup = sectorSetupRepository.findById(sectorSetupId)
                .orElseThrow(() -> {
                    log.error("SectorSetup not found with id {}", sectorSetupId);
                    return new ResourceNotFoundException("SectorSetup not found with id " + sectorSetupId);
                });

        log.info("Fetching Sector with ID: {} in SectorSetup ID: {}", sectorId, sectorSetupId);
        Sectors sector = sectorSetup.getSectors().stream()
                .filter(s -> s.getId().equals(sectorId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Sector not found with id {}", sectorId);
                    return new ResourceNotFoundException("Sector not found with id " + sectorId);
                });

        log.info("Sector found with ID: {} in SectorSetup ID: {}", sectorId, sectorSetupId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", sectorSetup.getId());
        response.put("countryName", sectorSetup.getCountryName());

        Map<String, Object> sectorMap = new HashMap<>();
        sectorMap.put("sectorId", sector.getId());
        sectorMap.put("parentSector", sector.getParentSector());
        sectorMap.put("subSectors", sector.getSubSector());

        response.put("sector", sectorMap);

        log.info("Returning sector details for Sector ID: {} in SectorSetup ID: {}", sectorId, sectorSetupId);

        return response;
    }
    public Sectors editSubSector(Long sectorId, Set<String> newSubSectors) {
        Sectors sector = sectorsRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found with id " + sectorId));

        log.info("Clearing and updating subsectors for sector with ID: {}", sectorId);
        sector.getSubSector().clear();
        sector.getSubSector().addAll(newSubSectors);

        return sectorsRepository.save(sector);
    }
    public Sectors getSector(Long sectorId) {
        log.info("Retrieving sector with ID: {}", sectorId);
        return sectorsRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found with id " + sectorId));
    }

    public void deleteSectorById(Long sectorId) {
        Optional<Sectors> sectorOptional = sectorsRepository.findById(sectorId);

        sectorOptional.ifPresent(sector -> {
            SectorSetup sectorSetup = sector.getSectorSetup();
            if (sectorSetup == null) {
                throw new ResourceNotFoundException("Sector setup not found for sector with id " + sectorId);
            }

            sectorSetup.getSectors().remove(sector);
            sectorsRepository.delete(sector);
            sectorSetupRepository.save(sectorSetup);
        });
    }


    @Transactional
    public Long createSectorSetup(SectorSetupRequestDto requestDto) {
        SectorSetup sectorSetup;
        Optional<SectorSetup> existingSectorSetup = sectorSetupRepository.findSectorByCountryName(requestDto.getCountryName());

        if (existingSectorSetup.isPresent()) {
            throw new ResourceNotFoundException("Sector setup for the country '" + requestDto.getCountryName() + "' already exists");
        } else {
            // Create a new SectorSetup
            sectorSetup = new SectorSetup();
            sectorSetup.setCountryName(requestDto.getCountryName());

            List<Sectors> newSectors = requestDto.getParentSector().stream()
                    .map(parentSectorName -> {
                        Sectors sector = new Sectors();
                        sector.setParentSector(parentSectorName);
                        sector.setSectorSetup(sectorSetup);
                        return sector;
                    })
                    .collect(Collectors.toList());
            sectorSetup.setSectors(newSectors);
        }

        return sectorSetupRepository.save(sectorSetup).getId();
    }

    @Transactional
    public void deleteSectorsBySectorSetupId(Long sectorSetupId) {
        // Find the SectorSetup by ID
        SectorSetup sectorSetup = sectorSetupRepository.findById(sectorSetupId)
                .orElseThrow(() -> new IllegalArgumentException("SectorSetup not found with ID: " + sectorSetupId));

        // Delete all sectors associated with this SectorSetup
        sectorsRepository.deleteAllBySectorSetup(sectorSetup);
    }
    public List<SectorSetup> getSectorSetupByCountryName(String countryName) {
        return sectorSetupRepository.findByCountryNameIgnoreCase(countryName);
    }

    public Page<SectorSetupDTO> getAllSectorData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("countryName"));
        Page<Object[]> resultsPage = sectorSetupRepository.findAllSectorSetupAggregates(pageable);

        // Map results to DTOs
        List<SectorSetupDTO> sectorSetupDTOs = resultsPage.getContent().stream()
                .map(result -> SectorSetupDTO.builder()
                        .id((Long) result[0])
                        .countryName((String) result[1])
                        .parentSectorCount(((Long) result[2]).intValue())
                        .subSectorCount(((Long) result[3]).intValue())
                        .build())
                .collect(Collectors.toList());

        // Return PageImpl with pagination metadata
        return new PageImpl<>(sectorSetupDTOs, pageable, resultsPage.getTotalElements());
    }
}

