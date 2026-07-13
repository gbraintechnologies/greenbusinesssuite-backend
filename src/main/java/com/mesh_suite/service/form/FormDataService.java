package com.mesh_suite.service.form;

import com.mesh_suite.constant.forms.FormResponseStatus;
import com.mesh_suite.constant.forms.PaymentStatus;
import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.dao.form.*;
import com.mesh_suite.domain.form.FormData;
import com.mesh_suite.domain.form.FormDataField;
import com.mesh_suite.domain.form.Payment;
import com.mesh_suite.dto.CompletedFormsCountResponse;
import com.mesh_suite.dto.FormDataProjection;
import com.mesh_suite.dto.FormDataWithPaymentStatus;
import com.mesh_suite.dto.StatisticResult;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.util.TimelineFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class FormDataService {
    @Autowired
    private FormDataRepository formDataRepository;
    @Autowired
    private FormDataFieldRepository formDataFieldRepository;
    @Autowired
    private FormsRepository formsRepository;
    @Autowired
    private FormFieldRepository formFieldRepository;
    @Autowired
    private FormsService formsService;
    @Autowired
    private PaymentRepository paymentRepository;

    public Long saveFormData(FormData formData) {
        log.info("Saving FormData for userId {} and formId {}", formData.getUserId(), formData.getFormId());

        FormData savedFormData = formDataRepository.save(formData);
        log.info("Saved FormData with id {}", savedFormData.getId());
        return savedFormData.getId();
    }

    public FormData updateFormData(FormData formData) {
        log.info("Updating FormData with id {}", formData.getId());

        if (!formDataRepository.existsById(formData.getId())) {
            String errorMessage = "FormData with id " + formData.getId() + " not found";
            log.error(errorMessage);
            throw new ResourceNotFoundException(errorMessage);
        }
        FormData updatedFormData = formDataRepository.save(formData);
        log.info("Updated FormData with id {}", updatedFormData.getId());
        return updatedFormData;
    }
    public void deleteFormDataByUserIdAndFormId(Long userId, Long formId) {
        log.info("Deleting FormData for userId {} and formId {}", userId, formId);

        formDataRepository.findByUserIdAndFormId(userId, formId)
                .ifPresentOrElse(formData -> {
                    formDataRepository.delete(formData);
            log.info("Deleted FormData with id {}", formData.getId());
        }, () -> {
            String errorMessage = "Form data not found for userId " + userId + " and formId " + formId;
            log.error(errorMessage);
            throw new ResourceNotFoundException(errorMessage);
        });
    }
    public FormData getFormDataById(Long responseId) {
        log.info("Fetching FormData for form response id {}", responseId);
        return formDataRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("Form data not found with response ID: " + responseId));
    }
    public Map<String, Object> getFormDataWithPaymentById(Long responseId) {
        log.info("Fetching FormData and Payment for response id {}", responseId);

        // Get form data
        FormData formData = formDataRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("Form data not found with response ID: " + responseId));

        // Get payment details
        Payment payment = paymentRepository.findByResponseId(responseId)
                .orElse(null); // Return null if payment not found

        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("responseData", formData);
        response.put("paymentDetails", payment != null ? payment : Collections.emptyMap());

        return response;
    }

    public Page<FormData> getAllFormData(int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size);

        // Fetch non-deleted records with or without timeline filtering
        return (timeline == null || timeline == Timeline.ALL)
                ? formDataRepository.findAll(pageable)
                : formDataRepository.findAllWithTimeline(TimelineFilter.calculateStartDate(timeline), pageable);
    }

    public void deleteFormData(Long id) {
        log.info("Attempting to delete FormData with id: {}", id);

        if (!formDataRepository.existsById(id)) {
            log.warn("FormData with id {} not found", id);
            throw new ResourceNotFoundException("FormData with id " + id + " not found");
        }
        formDataRepository.deleteById(id);
        log.info("Successfully deleted Form response data with id : {}", id);
    }

    public Page<FormDataProjection> getFormDataByFormId(Long formId, int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size);
        return (timeline == null || timeline == Timeline.ALL)
                ? formDataRepository.findAllByFormId(formId, pageable)
                : formDataRepository.findAllByFormIdWithTimeline(formId, TimelineFilter.calculateStartDate(timeline), pageable);
    }

    public Page<FormDataWithPaymentStatus> getAllFormDataByFormId(Long formId, int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FormDataProjection> formDataPage = (timeline == null || timeline == Timeline.ALL)
                ? formDataRepository.findAllByFormId(formId, pageable)
                : formDataRepository.findAllByFormIdWithTimeline(formId, TimelineFilter.calculateStartDate(timeline), pageable);

        // Map to include payment status
        List<FormDataWithPaymentStatus> content = formDataPage.getContent().stream()
                .map(formData -> {
                    PaymentStatus status = paymentRepository.findByResponseId(formData.getId())
                            .map(Payment::getStatus)
                            .orElse(null); // return null when no payment exists
                    return new FormDataWithPaymentStatus(formData, status);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, formDataPage.getTotalElements());
    }


    public Map<String, Long> getFormsDataCounts() {
        log.info("Fetching counts of FormData");
        long completedCount = formDataRepository.countByIsCompleted(true);
        long notCompletedCount = formDataRepository.countByIsCompleted(false);

        Map<String, Long> countsMap = new HashMap<>();
        countsMap.put("noCompleted", completedCount);
        countsMap.put("noUnCompleted", notCompletedCount);
        log.info("Retrieved counts of FormData: completed={}, notCompleted={}", completedCount, notCompletedCount);
        return countsMap;
    }
    @Transactional(readOnly = true)
    public Map<String, Long> getCountsByFormId(Long formId) {
        log.info("Fetching counts of FormData");
        long completedCount = formDataRepository.countByFormIdAndIsCompleted(formId, true);
        long notCompletedCount = formDataRepository.countByFormIdAndIsCompleted(formId, false);
       // long totalCount = formDataRepository.countByFormId(formId);

        Map<String, Long> countsMap = new HashMap<>();
        countsMap.put("completedCount", completedCount);
        countsMap.put("unCompletedCount", notCompletedCount);
        countsMap.put("totalCount", completedCount + notCompletedCount);
        log.info("Retrieved counts of FormData: completed={}, notCompleted={}", completedCount, notCompletedCount);
        return countsMap;
    }

    public List<FormData> getAllFormDataByFormDataRequest(Long userId, Long companyId, Long formId) {
        return formDataRepository.findAllByUserIdAndCompanyIdAndFormId(userId, companyId, formId);
    }

    public List<FormData> getAllFormDataByUserIdAndFormId(Long userId, Long formId) {
        log.info("Fetching all FormData for userId: {} and formId: {}", userId, formId);

        List<FormData> formDataList = formDataRepository.findAllByUserIdAndFormId(userId, formId);

        if (formDataList.isEmpty()) {
            log.warn("No FormData found for userId: {} and formId: {}", userId, formId);
        } else {
            log.info("Found {} FormData entries for userId: {} and formId: {}", formDataList.size(), userId, formId);
        }

        return formDataList;
    }

    public List<FormData> getAllFormDataByUserIdAndCompanyId(Long userId, Long companyId) {
        log.info("Fetching all FormData for userId: {} and companyId: {}", userId, companyId);

        List<FormData> formDataList = formDataRepository.findAllByUserIdAndCompanyId(userId, companyId);

        if (formDataList.isEmpty()) {
            log.warn("No FormData found for userId: {} and companyId: {}", userId, companyId);
        } else {
            log.info("Found {} FormData entries for userId: {} and companyId: {}", formDataList.size(), userId, companyId);
        }

        return formDataList;
    }

    public Page<FormData> getAllFormDataByCompanyId(Long companyId, int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size);

        // Fetch non-deleted records with or without timeline filtering
        return (timeline == null || timeline == Timeline.ALL)
                ? formDataRepository.findAllByCompanyId(companyId, pageable)
                : formDataRepository.findAllByCompanyIdWithTimeline(companyId, TimelineFilter.calculateStartDate(timeline), pageable);
    }

    public List<StatisticResult> getStatistics(Long formId, Long companyId) {
        log.info("Fetching statistical fields for formId {} and companyId {}", formId, companyId);

        // Fetch statistical fields
        List<FormDataField> statisticalFields = formDataRepository.findStatsFieldsByFormIdAndCompanyId(formId, companyId);

        log.info("Found {} statistical fields for formId {} and companyId {}", statisticalFields.size(), formId, companyId);

        // For uniqueness of StatisticResult objects
        Set<StatisticResult> statisticResults = new HashSet<>();

        // Process statisticalFields to compute statistics
        statisticalFields.forEach(field -> {
            log.debug("Processing statistical field: {}", field);

            switch (field.getStatisticalFunction()) {
                case "sum":
                case "average":
                    BigDecimal result = calculateSumOrAverage(formId, field.getFormFieldId(), field.getStatisticalFunction());
                    Map<String, Object> singleDataEntry = createSingleDataEntry(field.getFieldName(), result);
                    statisticResults.add(new StatisticResult(field.getFieldName(), field.getStatisticalFunction(), field.getDisplayType(), List.of(singleDataEntry)));
                    break;
                case "count":
                    List<Map<String, Object>> countData = calculateCount(formId, field.getFormFieldId());
                    statisticResults.add(new StatisticResult(field.getFieldName(), "count", field.getDisplayType(), countData));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported aggregation function: " + field.getStatisticalFunction());
            }
        });

        // Convert the set to a list for the final result
        List<StatisticResult> resultList = new ArrayList<>(statisticResults);
        log.info("Generated {} unique StatisticResults for formId {} and companyId {}", resultList.size(), formId, companyId);

        return resultList;
    }

    private BigDecimal calculateSumOrAverage(Long formId, Long formFieldId, String function) {
        log.debug("Calculating {} for formId {} and formFieldId {}", function, formId, formFieldId);

        List<String> fieldValues = formDataRepository.findFieldValuesByFormIdAndFieldId(formId, formFieldId);

        BigDecimal total = fieldValues.stream()
                .filter(Objects::nonNull)
                .map(this::convertToBigDecimal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (function.equals("average") && !fieldValues.isEmpty()) {
            return total.divide(BigDecimal.valueOf(fieldValues.size()), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            return total;
        }
    }

    private List<Map<String, Object>> calculateCount(Long formId, Long formFieldId) {
        log.debug("Calculating count for formId {} and formFieldId {}", formId, formFieldId);

        // Fetch all responses and their counts
        List<Object[]> countedDataList = formDataRepository.countResponsesByFormIdAndFieldId(formId, formFieldId);

        // Collect responses and their counts into a map, excluding null and empty responses
        Map<String, Long> countedData = countedDataList.stream()
                .filter(entry -> entry[0] != null && !((String) entry[0]).trim().isEmpty()) // Exclude null and empty responses
                .collect(Collectors.toMap(
                        entry -> (String) entry[0],
                        entry -> (Long) entry[1]
                ));

        // Map each unique response value to its count, handling single and comma-separated responses
        return countedData.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> dataEntry = new HashMap<>();
                    dataEntry.put("name", entry.getKey());
                    dataEntry.put("value", entry.getValue());
                    return dataEntry;
                })
                .collect(Collectors.toList());
    }
    private Map<String, Object> createSingleDataEntry(String name, BigDecimal value) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("name", name);
        entry.put("value", value);
        return entry;
    }

    private BigDecimal convertToBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.error("Error converting value {} to BigDecimal: {}", value, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public long countIgnoredLinks(Long companyId, List<Long> publishedFormIds) {
        long ignoredCount = publishedFormIds.stream()
                .filter(formId -> {
                    List<FormData> formDataList = formDataRepository.findByCompanyIdsAndFormId(companyId, formId);
                    return formDataList.isEmpty();
                })
                .count();
        return ignoredCount;
    }


    public long countLinksOpened(Long companyId, List<Long> publishedFormIds) {
        long totalEntryCount = publishedFormIds.stream()
                .mapToLong(formId -> {
                    List<FormData> formDataList = formDataRepository.findByCompanyIdsAndFormId(companyId, formId);
                    return formDataList.size();
                })
                .sum();
        return totalEntryCount;
    }

    public long countUniqueUserIds(Long companyId) {
        return formDataRepository.countUniqueUserIdsByCompanyId(companyId);
    }

    public long countTotalEntries(Long companyId) {
        return formDataRepository.countByCompanyId(companyId);
    }
    public Map<String, Long> getTotalFormsSummary(Long companyId) {
        long completedForms = formDataRepository.countByCompanyIdAndIsCompleted(companyId, true);
        long uncompletedForms = formDataRepository.countByCompanyIdAndIsCompleted(companyId, false);

        Map<String, Long> formSummary = new HashMap<>();
        formSummary.put("completedForms", completedForms);
        formSummary.put("uncompletedForms", uncompletedForms);

        return formSummary;
    }

    public Optional<FormData> updateStatus(Long id, FormResponseStatus status) {
        Optional<FormData> optionalFormData = formDataRepository.findById(id);
        if (optionalFormData.isPresent()) {
            FormData formData = optionalFormData.get();
            formData.setStatus(status);
            formDataRepository.save(formData);
        }
        return optionalFormData;
    }

    public CompletedFormsCountResponse getCompletedFormCountsByCompanyId(Long companyId, int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size);

      log.info("Fetching completed form counts for companyId={}, timeline={}, derived timeline={}", companyId, timeline, timeline.toString());

        Page<Object[]> results;
        try {
            results = (timeline == null || timeline == Timeline.ALL)
                    ? formDataRepository.findCompletedFormCountsByCompanyId(companyId, pageable)
                    : formDataRepository.findCompletedFormCountsByCompanyWithTimeline(companyId, TimelineFilter.calculateStartDate(timeline), pageable);

            log.info("Query executed successfully, found {} records", results.getTotalElements());
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            return new CompletedFormsCountResponse(true, true, 0, 0, size, new ArrayList<>());
        }

        if (results == null || results.isEmpty()) {
            log.warn("No completed forms found for companyId={}", companyId);
            return new CompletedFormsCountResponse(true, true, 0, 0, size, new ArrayList<>());
        }

        List<Map<String, Long>> userFormStatList = results.stream()
                .map(result -> {
                    try {
                        return Map.of("userId", (Long) result[0], "submitFormsCount", (Long) result[1]);
                    } catch (Exception e) {
                        log.error("Error processing result row: {}", Arrays.toString(result), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new CompletedFormsCountResponse(
                results.isFirst(),
                results.isLast(),
                results.getTotalElements(),
                results.getTotalPages(),
                results.getSize(),
                userFormStatList
        );
    }

    public List<FormData> getCompletedFormsByUserId(Long userId) {
        return formDataRepository.findByUserIdAndIsCompleted(userId, true);
    }

    public List<FormData> getUncompletedFormsByUserId(Long userId) {
        return formDataRepository.findByUserIdAndIsCompleted(userId, false);
    }

    public Page<FormData> findCompletedFormDataByCompanyAndStatus(Long companyId, FormResponseStatus status, Pageable pageable) {
        return formDataRepository.findByCompanyIdAndStatus(companyId, status, pageable);
    }

    public List<Long> getFormIdsByUserId(Long userId) {
        return formDataRepository.findFormIdsByUserId(userId);
    }

    public Long saveExternalFormData(FormData formData) {
        FormData savedFormData = formDataRepository.save(formData);
        log.info("Saved FormData with id {}", savedFormData.getId());
        return savedFormData.getId();
    }
    public List<FormData> getFormsByUserId(Long userId) {
        return formDataRepository.findByUserId(userId);
    }

}