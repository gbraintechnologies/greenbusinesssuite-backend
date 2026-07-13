package com.mesh_suite.service.form;

import com.mesh_suite.dao.country.CurrencySetupRepository;
import com.mesh_suite.dao.country.DenominationRepository;
import com.mesh_suite.domain.form.CurrencySetup;
import com.mesh_suite.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CurrencySetupService {
    @Autowired
    private CurrencySetupRepository currencySetupRepository;

    @Autowired
    private DenominationRepository denominationRepository;

    public CurrencySetup createCurrencySetup(CurrencySetup currencySetup) {
        // Check if the currency already exists (case-insensitive)
        Optional<CurrencySetup> existingCurrency = currencySetupRepository.findCurrencyByNameIgnoreCase(currencySetup.getCurrency());

        if (existingCurrency.isPresent()) {
            throw new ResourceNotFoundException("Currency with the name '" + currencySetup.getCurrency() + "' already exists");
        }
        return currencySetupRepository.save(currencySetup);
    }

    public Page<CurrencySetup> getCurrencySetups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending().and(Sort.by("id")));
        return currencySetupRepository.findAll(pageable);
    }

    public Page<CurrencySetup> getAllExistingCurrencySetups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending().and(Sort.by("id")));
        return currencySetupRepository.findByIsDeletedFalse(pageable);
    }

    public CurrencySetup getCurrencySetupById(Long id) {
        return currencySetupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency Not Found"));
    }

    public CurrencySetup updateCurrencySetup(CurrencySetup updatedCurrencySetup) {
        return currencySetupRepository.save(updatedCurrencySetup);
    }

    public void softDeleteCurrencySetup(Long id) {
        CurrencySetup currencySetup = getCurrencySetupById(id);
        currencySetup.setIsDeleted(true);
        currencySetup.setDeletedOn(LocalDateTime.now());
        currencySetupRepository.save(currencySetup);
    }

    public void deleteCurrencySetup(Long id) {
        currencySetupRepository.deleteById(id);
    }

    public List<CurrencySetup> findCurrencyByCountryName(String countryName) {
        List<CurrencySetup> currencySetups = currencySetupRepository.findByCountryNameContainingIgnoreCase(countryName);

        if (currencySetups.isEmpty()) {
            throw new ResourceNotFoundException("No Currency fouund for this Country "+ countryName);
        }

        return currencySetups;
    }
    @Transactional
    public List<CurrencySetup> searchByCurrency(String currency) {
        return currencySetupRepository.findByCurrencyIgnoreCase(currency);
    }
}
