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

    long countByRecordTypeAndProgramId(String recordType, String programId);

    @Query("""
            select count(r) from GeaProgramRecord r
            where r.recordType in :recordTypes
            """)
    long countByRecordTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query("""
            select count(r) from GeaProgramRecord r
            where r.recordType = :recordType
              and (:programId = 'all' or r.programId = :programId)
            """)
    long countForProgram(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query("""
            select coalesce(sum(r.approvedAmount), 0) from GeaProgramRecord r
            where r.recordType = :recordType
              and (:programId = 'all' or r.programId = :programId)
            """)
    BigDecimal sumApproved(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query("""
            select r.gender, count(r) from GeaProgramRecord r
            where r.recordType = :recordType
              and (:programId = 'all' or r.programId = :programId)
            group by r.gender order by count(r) desc
            """)
    List<Object[]> countByGender(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query("""
            select r.gender, count(r) from GeaProgramRecord r
            where r.recordType in :recordTypes
            group by r.gender order by count(r) desc
            """)
    List<Object[]> countByGenderTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query("""
            select r.region, count(r) from GeaProgramRecord r
            where r.recordType = :recordType
              and (:programId = 'all' or r.programId = :programId)
              and r.region is not null
            group by r.region order by count(r) desc
            """)
    List<Object[]> countByRegion(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query("""
            select r.region, count(r) from GeaProgramRecord r
            where r.recordType in :recordTypes and r.region is not null
            group by r.region order by count(r) desc
            """)
    List<Object[]> countByRegionTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query("""
            select r.sector, count(r) from GeaProgramRecord r
            where r.recordType = :recordType
              and (:programId = 'all' or r.programId = :programId)
              and r.sector is not null
            group by r.sector order by count(r) desc
            """)
    List<Object[]> countBySector(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query("""
            select r.sector, count(r) from GeaProgramRecord r
            where r.recordType in :recordTypes and r.sector is not null
            group by r.sector order by count(r) desc
            """)
    List<Object[]> countBySectorTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query("""
            select case when r.registered = true then 'Registered' when r.registered = false then 'Non-registered' else 'Other / undisclosed' end,
                   count(r)
            from GeaProgramRecord r
            where r.recordType = :recordType
              and (:programId = 'all' or r.programId = :programId)
            group by case when r.registered = true then 'Registered' when r.registered = false then 'Non-registered' else 'Other / undisclosed' end
            """)
    List<Object[]> countByRegistered(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query("""
            select case when r.registered = true then 'Registered' when r.registered = false then 'Non-registered' else 'Other / undisclosed' end,
                   count(r)
            from GeaProgramRecord r
            where r.recordType in :recordTypes
            group by case when r.registered = true then 'Registered' when r.registered = false then 'Non-registered' else 'Other / undisclosed' end
            """)
    List<Object[]> countByRegisteredTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query("""
            select case when r.disability = true then 'With disability' when r.disability = false then 'Without disability' else 'Other / undisclosed' end,
                   count(r)
            from GeaProgramRecord r
            where r.recordType = :recordType
              and (:programId = 'all' or r.programId = :programId)
            group by case when r.disability = true then 'With disability' when r.disability = false then 'Without disability' else 'Other / undisclosed' end
            """)
    List<Object[]> countByDisability(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query("""
            select case when r.disability = true then 'With disability' when r.disability = false then 'Without disability' else 'Other / undisclosed' end,
                   count(r)
            from GeaProgramRecord r
            where r.recordType in :recordTypes
            group by case when r.disability = true then 'With disability' when r.disability = false then 'Without disability' else 'Other / undisclosed' end
            """)
    List<Object[]> countByDisabilityTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query("""
            select coalesce(r.ageBand, 'Other / undisclosed'), count(r)
            from GeaProgramRecord r
            where r.recordType = :recordType
              and (:programId = 'all' or r.programId = :programId)
            group by coalesce(r.ageBand, 'Other / undisclosed')
            order by count(r) desc
            """)
    List<Object[]> countByAgeBand(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query("""
            select coalesce(r.ageBand, 'Other / undisclosed'), count(r)
            from GeaProgramRecord r
            where r.recordType in :recordTypes
            group by coalesce(r.ageBand, 'Other / undisclosed')
            order by count(r) desc
            """)
    List<Object[]> countByAgeBandTypes(@Param("recordTypes") Collection<String> recordTypes);

    @Query("""
            select coalesce(r.literacy, 'Other / undisclosed'), count(r)
            from GeaProgramRecord r
            where r.recordType = :recordType
              and (:programId = 'all' or r.programId = :programId)
            group by coalesce(r.literacy, 'Other / undisclosed')
            order by count(r) desc
            """)
    List<Object[]> countByLiteracy(@Param("recordType") String recordType, @Param("programId") String programId);

    @Query("""
            select coalesce(r.literacy, 'Other / undisclosed'), count(r)
            from GeaProgramRecord r
            where r.recordType in :recordTypes
            group by coalesce(r.literacy, 'Other / undisclosed')
            order by count(r) desc
            """)
    List<Object[]> countByLiteracyTypes(@Param("recordTypes") Collection<String> recordTypes);
}
