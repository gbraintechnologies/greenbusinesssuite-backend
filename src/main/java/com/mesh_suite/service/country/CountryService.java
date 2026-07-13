package com.mesh_suite.service.country;

import com.mesh_suite.dao.country.CountryRepository;
import com.mesh_suite.dao.country.ParentLevelRepository;
import com.mesh_suite.domain.coutry.AddressingScheme;
import com.mesh_suite.domain.coutry.Country;
import com.mesh_suite.domain.coutry.ParentLevel;
import com.mesh_suite.dto.CountryRequestDTO;
import com.mesh_suite.dto.PaginatedCountryResponse;
import com.mesh_suite.dto.ParentLevelRequestDTO;
import com.mesh_suite.exception.CountryNotFoundException;
import com.mesh_suite.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CountryService {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private ParentLevelRepository parentLevelRepository;
    public Long createCountry(CountryRequestDTO countryRequestDTO) {

        // Check if a country with the same countryName (case-insensitive) already exists
        Optional<Country> existingCountry = countryRepository.findByCountryNameIgnoreCase(countryRequestDTO.getCountryName());

        if (existingCountry.isPresent()) {
            // Throw an exception if the country already exists
            throw new ResourceNotFoundException("Country with the name '" + countryRequestDTO.getCountryName() + "' already exists");
        }
        Country country = new Country();
        country.setCountryName(countryRequestDTO.getCountryName());
        country.setCountryId(countryRequestDTO.getCountryId());
        country.setInputType(countryRequestDTO.getInputType());

        AddressingScheme addressingScheme = new AddressingScheme();
        addressingScheme.setCountry(country);
        addressingScheme.setParentLevelName(countryRequestDTO.getParentLevelName());
        addressingScheme.setChildLevelName(countryRequestDTO.getChildLevelName());

        // Convert list of parent names to ParentLevel entities
        List<ParentLevel> parentLevels = countryRequestDTO.getParentNames().stream()
                .map(parentName -> {
                    ParentLevel parentLevel = new ParentLevel();
                    parentLevel.setParentName(parentName);
                    parentLevel.setAddressingScheme(addressingScheme); // Set the relationship
                    parentLevel.setChildLevels(new ArrayList<>()); // Initialize an empty list for child levels
                    return parentLevel;
                })
                .collect(Collectors.toList());

        addressingScheme.setParentLevels(parentLevels); // Set the parent levels in the addressing scheme
        country.setAddressingScheme(addressingScheme);

        return countryRepository.save(country).getId();
    }
    public Country getCountryById(Long id) {
        return countryRepository.findById(id)
                .orElse(null);
    }

    public Country updateCountry(Country updatedCountry) {
        return countryRepository.findById(updatedCountry.getId())
                .map(existingCountry -> {
                    existingCountry.setCountryName(updatedCountry.getCountryName());
                    existingCountry.setCountryId(updatedCountry.getCountryId());
                    existingCountry.setInputType(updatedCountry.getInputType());

                    AddressingScheme addressingScheme = updatedCountry.getAddressingScheme();
                    if (addressingScheme != null) {
                        addressingScheme.setCountry(existingCountry);
                        existingCountry.setAddressingScheme(addressingScheme);
                    }

                    return countryRepository.save(existingCountry);
                })
                .orElse(null);
    }
    @Transactional
    public void deleteCountryById(Long id) {
        if (countryRepository.existsById(id)) {
            countryRepository.deleteById(id);
        } else {
            throw new CountryNotFoundException("Country with ID " + id + " not found");
        }
    }

    public PaginatedCountryResponse getCountries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "countryName"));
        Page<Country> countryPage = countryRepository.findAll(pageable);

        PaginatedCountryResponse response = new PaginatedCountryResponse();
        response.setFirst(countryPage.isFirst());
        response.setLast(countryPage.isLast());
        response.setTotalElements(countryPage.getTotalElements());
        response.setTotalPages(countryPage.getTotalPages());
        response.setSize(countryPage.getSize());
        response.setCountries(countryPage.getContent());

        return response;
    }
    public Optional<Country> getCountryByName(String countryName) {
        return countryRepository.findByCountryNameIgnoreCase(countryName);
    }

    public List<String> getChildLevelsByParentNameAndCountryId(String parentName, Long countryId) {
        return parentLevelRepository.findChildLevelsByParentNameAndCountryId(parentName, countryId);
    }

    public void addParentLevel(ParentLevelRequestDTO parentRequest) {
        Country country = countryRepository.findById(parentRequest.getCountryId())
                .orElseThrow(() -> new IllegalArgumentException("Country not found"));

        AddressingScheme addressingScheme = country.getAddressingScheme();
        if (addressingScheme == null) {
            throw new IllegalArgumentException("AddressingScheme not found for the country");
        }

        ParentLevel parentLevel = new ParentLevel();
        parentLevel.setAddressingScheme(addressingScheme);
        parentLevel.setParentName(parentRequest.getParentName());
        parentLevel.setChildLevels(parentRequest.getChildLevels());

        parentLevelRepository.save(parentLevel);
    }
    public void deleteParentLevel(Long parentId) {
        ParentLevel parentLevel = parentLevelRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent level not found"));

        parentLevelRepository.delete(parentLevel);
    }

    public List<String> getAllCountryNames() {
        return countryRepository.findAll()
                .stream()
                .map(Country::getCountryName)
                .collect(Collectors.toList());
    }
    public List<String> getChildLevelsByParentName(String parentName) {
        ParentLevel parentLevel = parentLevelRepository.findByParentNameIgnoreCase(parentName)
                .orElseThrow(() -> new IllegalArgumentException("Parent name not found: " + parentName));

        return parentLevel.getChildLevels();
    }
}
