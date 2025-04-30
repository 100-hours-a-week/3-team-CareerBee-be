package org.choon.careerbee.domain.company.repository;

import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.repository.custom.CompanyCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long>, CompanyCustomRepository {
}