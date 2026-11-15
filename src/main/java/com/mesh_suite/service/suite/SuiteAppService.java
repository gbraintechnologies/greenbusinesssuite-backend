package com.mesh_suite.service.suite;

import com.mesh_suite.dao.suite.SuiteAppRepository;
import com.mesh_suite.domain.suite.SuiteApp;
import com.mesh_suite.dto.request.SuiteAppRequest;
import com.mesh_suite.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuiteAppService {

    private final SuiteAppRepository suiteAppRepository;

    public List<SuiteApp> listPublished() {
        return suiteAppRepository.findByPublishedTrueOrderBySortOrderAscIdAsc();
    }

    public List<SuiteApp> listAll() {
        return suiteAppRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    public SuiteApp getById(Long id) {
        return suiteAppRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suite app not found: " + id));
    }

    @Transactional
    public SuiteApp create(SuiteAppRequest request) {
        SuiteApp app = apply(new SuiteApp(), request);
        if (app.getSortOrder() == null) {
            app.setSortOrder(0);
        }
        return suiteAppRepository.save(app);
    }

    @Transactional
    public SuiteApp update(Long id, SuiteAppRequest request) {
        SuiteApp existing = getById(id);
        return suiteAppRepository.save(apply(existing, request));
    }

    @Transactional
    public void delete(Long id) {
        if (!suiteAppRepository.existsById(id)) {
            throw new ResourceNotFoundException("Suite app not found: " + id);
        }
        suiteAppRepository.deleteById(id);
    }

    @Transactional
    public void seedDefaultsIfEmpty() {
        if (suiteAppRepository.count() > 0) {
            return;
        }
        suiteAppRepository.save(SuiteApp.builder()
                .name("Autobus")
                .tagline("AI-powered business operations")
                .description("Customers, sales, marketing, and your team in one operations workspace.")
                .href("https://useautobus.com/")
                .category("Operations")
                .published(true)
                .mark("A")
                .accent("bg-[#14532d]")
                .wash("bg-[#f0fdf4]")
                .sortOrder(1)
                .build());
        suiteAppRepository.save(SuiteApp.builder()
                .name("GREENPOS")
                .tagline("Point of sale for growing shops")
                .description("Take sales, track stock, and close the day from a POS built for African businesses.")
                .href("https://www.usegreenpos.com/")
                .category("Point of sale")
                .published(true)
                .mark("G")
                .accent("bg-[#0b57d0]")
                .wash("bg-[#e8f0fe]")
                .sortOrder(2)
                .build());
    }

    private SuiteApp apply(SuiteApp app, SuiteAppRequest request) {
        String name = request.getName() != null ? request.getName().trim() : "";
        app.setName(name);
        app.setTagline(trimToNull(request.getTagline()));
        app.setDescription(trimToNull(request.getDescription()));
        app.setHref(request.getHref() != null ? request.getHref().trim() : "");
        app.setCategory(trimToNull(request.getCategory()) != null
                ? request.getCategory().trim()
                : "App");
        if (request.getPublished() != null) {
            app.setPublished(request.getPublished());
        }
        app.setLogoUrl(trimToNull(request.getLogoUrl()));
        String mark = trimToNull(request.getMark());
        if (mark == null && !name.isEmpty()) {
            mark = name.substring(0, 1).toUpperCase();
        }
        app.setMark(mark != null ? mark : "A");
        app.setAccent(trimToNull(request.getAccent()) != null
                ? request.getAccent().trim()
                : "bg-[#14532d]");
        app.setWash(trimToNull(request.getWash()) != null
                ? request.getWash().trim()
                : "bg-[#f0fdf4]");
        if (request.getSortOrder() != null) {
            app.setSortOrder(request.getSortOrder());
        }
        return app;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
