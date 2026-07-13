package com.mesh_suite.service.notify;

import com.mesh_suite.constant.forms.InputType;
import com.mesh_suite.dao.company.SectorSetupRepository;
import com.mesh_suite.dao.company.SectorsRepository;
import com.mesh_suite.dao.country.AddressingSchemeRepository;
import com.mesh_suite.dao.country.CountryRepository;
import com.mesh_suite.dao.country.ParentLevelRepository;
import com.mesh_suite.domain.company.SectorSetup;
import com.mesh_suite.domain.company.Sectors;
import com.mesh_suite.domain.coutry.AddressingScheme;
import com.mesh_suite.domain.coutry.Country;
import com.mesh_suite.domain.coutry.ParentLevel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class CsvService {
    @Autowired
    private SectorSetupRepository sectorSetupRepository;
    @Autowired
    private SectorsRepository sectorsRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AddressingSchemeRepository addressingSchemeRepository;

    @Autowired
    private ParentLevelRepository parentLevelRepository;
    public void uploadCsv(MultipartFile file) throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            processCsv(csvReader);
        } catch (IOException | CsvValidationException e) {
            log.error("Failed to upload CSV file", e);
            throw e;
        }
    }

    private void processCsv(CSVReader csvReader) throws IOException, CsvValidationException {
        String[] values;
        boolean isFirstLine = true;
        while ((values = csvReader.readNext()) != null) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; // Skip header line
            }
            if (values.length < 2) {
                log.warn("Skipping invalid row: {}", Arrays.toString(values));
                continue; // Skip invalid rows
            }

            String countryName = values[0];
            String parentSector = values[1];
            List<String> subSectorsList = Arrays.asList(values).subList(2, values.length).stream()
                    .filter(subSector -> subSector != null && !subSector.trim().isEmpty())
                    .collect(Collectors.toList());

            if (subSectorsList.isEmpty()) {
                log.warn("Skipping row with empty sub-sectors: {}", Arrays.toString(values));
                continue; // Skip rows with empty sub-sectors
            }

            log.info("Processing country: {}, parent sector: {}, sub-sectors: {}", countryName, parentSector, subSectorsList);
            processSector(countryName, parentSector, subSectorsList);
        }
    }

    private void processSector(String countryName, String parentSector, List<String> subSectorsList) {
        SectorSetup sectorSetup = sectorSetupRepository.findByCountryName(countryName)
                .orElseGet(() -> new SectorSetup(null, countryName, new ArrayList<>()));

        Optional<Sectors> existingSector = sectorSetup.getSectors().stream()
                .filter(s -> s.getParentSector().equals(parentSector))
                .findFirst();

        if (existingSector.isPresent()) {
            updateExistingSector(existingSector.get(), subSectorsList);
        } else {
            createNewSector(sectorSetup, parentSector, subSectorsList);
        }

        sectorSetupRepository.save(sectorSetup);
        log.info("Saved sector setup for country: {}", countryName);
    }

    private void updateExistingSector(Sectors existing, List<String> subSectorsList) {
        existing.setSubSector(new HashSet<>(subSectorsList));
        sectorsRepository.save(existing);
        log.info("Updated existing sector for parent sector: {}", existing.getParentSector());
    }

    private void createNewSector(SectorSetup sectorSetup, String parentSector, List<String> subSectorsList) {
        Sectors sectors = new Sectors();
        sectors.setParentSector(parentSector);
        sectors.setSubSector(new HashSet<>(subSectorsList));
        sectors.setSectorSetup(sectorSetup);
        sectorSetup.getSectors().add(sectors);
        log.info("Created new sector for parent sector: {}", parentSector);
    }
    public void importCountryCsv(MultipartFile file) throws IOException, CsvException {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = csvReader.readAll();

            // Check if the CSV file is empty
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            // Skip the header row
            rows.remove(0);

            for (String[] row : rows) {
                // Ensure row has at least 5 columns (index 0-4)
                if (row.length < 5) {
                    throw new IllegalArgumentException("Invalid CSV format: insufficient columns at row " + rows.indexOf(row));
                }

                // Extract values from the row
                String countryName = row[0];
                InputType inputType;

                try {
                    inputType = InputType.valueOf(row[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid InputType in row " + rows.indexOf(row));
                }

                String parentLevelName = row[2];
                String childLevelName = row[3];
                String parentName = row[4];

                // Process the country entity
                Country country = countryRepository.findByCountryName(countryName).orElseGet(() -> {
                    Country newCountry = new Country();
                    newCountry.setCountryName(countryName);
                    newCountry.setInputType(inputType);
                    return countryRepository.save(newCountry);
                });

                // Process AddressingScheme
                AddressingScheme addressingScheme = country.getAddressingScheme();
                if (addressingScheme == null) {
                    addressingScheme = new AddressingScheme();
                    addressingScheme.setCountry(country);
                    addressingScheme.setParentLevelName(parentLevelName);
                    addressingScheme.setChildLevelName(childLevelName);
                    addressingScheme = addressingSchemeRepository.save(addressingScheme);
                    country.setAddressingScheme(addressingScheme);
                }

                // Process ParentLevel
                ParentLevel parentLevel = new ParentLevel();
                parentLevel.setAddressingScheme(addressingScheme);
                parentLevel.setParentName(parentName);

                // Collect child levels (assuming they are included in the ChildLevels field)
                List<String> childLevels = new ArrayList<>();
                if (row.length > 5) {
                    String[] childLevelArray = row[5].split(",");
                    for (String childLevel : childLevelArray) {
                        if (!childLevel.trim().isEmpty()) {
                            childLevels.add(childLevel.trim());
                        }
                    }
                }

                parentLevel.setChildLevels(childLevels);
                parentLevelRepository.save(parentLevel);
            }
        }
    }

}

