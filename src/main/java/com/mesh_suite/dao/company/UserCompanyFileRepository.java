package com.mesh_suite.dao.company;

import com.mesh_suite.domain.company.UserCompanyFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCompanyFileRepository extends JpaRepository<UserCompanyFile, Long> {
 List<UserCompanyFile> findByUserId(Long userId);
 List<UserCompanyFile> findByUserIdAndCompanyId(Long userId, Long companyId);
 List<UserCompanyFile> findByUserIdAndFormId(Long userId, Long formId);

 List<UserCompanyFile> findByCompanyId(Long companyId);

 List<UserCompanyFile> findByCompanyIdAndFormIdAndUserId(Long companyId, Long formId,Long userId);
}
