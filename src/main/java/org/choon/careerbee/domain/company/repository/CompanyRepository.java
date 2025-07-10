package org.choon.careerbee.domain.company.repository;

import java.util.Optional;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.repository.custom.CompanyCustomRepository;
import org.choon.careerbee.domain.company.repository.jdbc.CompanyJdbcRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends
    JpaRepository<Company, Long>,
    CompanyCustomRepository,
    CompanyJdbcRepository {

    Optional<Company> findBySaraminName(String saraminName);
}
