package com.mesh_suite.dao.analytics;

import com.mesh_suite.domain.analytics.GeaProgramRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface GeaProgramRecordRepository extends JpaRepository<GeaProgramRecord, Long> {

    long countByRecordType(String recordType);

    /**
     * Distinct businesses: unique phones + one count per row without a phone.
     */
    @Query(value = """
            SELECT COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type IN (:recordTypes)
            """, nativeQuery = true)
    long countDistinctBusinesses(@Param("recordTypes") Collection<String> recordTypes);

    @Query(value = """
            SELECT COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type = :recordType
              AND (:programId = 'all' OR program_id = :programId)
            """, nativeQuery = true)
    long countDistinctForProgram(@Param("recordType") String recordType, @Param("programId") String programId);

    /**
     * Sum facility amounts for "Total disbursed".
     * Excludes spreadsheet total rows, absurd outliers (&gt;= GHS 1M), Ernest v2
     * (duplicate of v1), application pipelines that have a disbursed/beneficiary
     * sibling dataset, and ICE regional finalists (not disbursed).
     */
    @Query(value = """
            SELECT COALESCE(SUM(approved_amount), 0)
            FROM gea_program_record
            WHERE record_type = :recordType
              AND approved_amount IS NOT NULL
              AND approved_amount > 0
              AND approved_amount < 1000000
              AND COALESCE(BTRIM(external_key), '') !~* '^(total|grand total|sum)$'
              AND (
                    :programId <> 'all' AND program_id = :programId
                 OR :programId = 'all' AND program_id NOT IN (
                        'ernest_v2', 'nkosuo', 'capbuss', 'ice_regional'
                    )
              )
            """, nativeQuery = true)
    BigDecimal sumApproved(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query(value = """
            SELECT COALESCE(gender, 'Other / undisclosed'),
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type = :recordType
              AND (:programId = 'all' OR program_id = :programId)
            GROUP BY COALESCE(gender, 'Other / undisclosed')
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctByGender(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query(value = """
            SELECT COALESCE(gender, 'Other / undisclosed'),
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type IN (:recordTypes)
            GROUP BY COALESCE(gender, 'Other / undisclosed')
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctByGenderTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query(value = """
            SELECT region,
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type = :recordType
              AND (:programId = 'all' OR program_id = :programId)
              AND region IS NOT NULL
            GROUP BY region
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctByRegion(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query(value = """
            SELECT region,
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type IN (:recordTypes) AND region IS NOT NULL
            GROUP BY region
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctByRegionTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query(value = """
            SELECT sector,
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type = :recordType
              AND (:programId = 'all' OR program_id = :programId)
              AND sector IS NOT NULL
            GROUP BY sector
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctBySector(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query(value = """
            SELECT sector,
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type IN (:recordTypes) AND sector IS NOT NULL
            GROUP BY sector
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctBySectorTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query(value = """
            SELECT CASE WHEN registered = true THEN 'Registered'
                        WHEN registered = false THEN 'Non-registered'
                        ELSE 'Other / undisclosed' END,
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type = :recordType
              AND (:programId = 'all' OR program_id = :programId)
            GROUP BY 1
            """, nativeQuery = true)
    List<Object[]> countDistinctByRegistered(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query(value = """
            SELECT CASE WHEN registered = true THEN 'Registered'
                        WHEN registered = false THEN 'Non-registered'
                        ELSE 'Other / undisclosed' END,
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type IN (:recordTypes)
            GROUP BY 1
            """, nativeQuery = true)
    List<Object[]> countDistinctByRegisteredTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query(value = """
            SELECT CASE WHEN disability = true THEN 'With disability'
                        WHEN disability = false THEN 'Without disability'
                        ELSE 'Other / undisclosed' END,
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type = :recordType
              AND (:programId = 'all' OR program_id = :programId)
            GROUP BY 1
            """, nativeQuery = true)
    List<Object[]> countDistinctByDisability(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query(value = """
            SELECT CASE WHEN disability = true THEN 'With disability'
                        WHEN disability = false THEN 'Without disability'
                        ELSE 'Other / undisclosed' END,
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type IN (:recordTypes)
            GROUP BY 1
            """, nativeQuery = true)
    List<Object[]> countDistinctByDisabilityTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query(value = """
            SELECT COALESCE(age_band, 'Other / undisclosed'),
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type = :recordType
              AND (:programId = 'all' OR program_id = :programId)
            GROUP BY 1
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctByAgeBand(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query(value = """
            SELECT COALESCE(age_band, 'Other / undisclosed'),
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type IN (:recordTypes)
            GROUP BY 1
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctByAgeBandTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query(value = """
            SELECT COALESCE(literacy, 'Other / undisclosed'),
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type = :recordType
              AND (:programId = 'all' OR program_id = :programId)
            GROUP BY 1
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctByLiteracy(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query(value = """
            SELECT COALESCE(literacy, 'Other / undisclosed'),
                   COUNT(DISTINCT phone) FILTER (WHERE phone IS NOT NULL)
                 + COUNT(*) FILTER (WHERE phone IS NULL)
            FROM gea_program_record
            WHERE record_type IN (:recordTypes)
            GROUP BY 1
            ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Object[]> countDistinctByLiteracyTypes(@Param("recordTypes") Collection<String> recordTypes);
}
