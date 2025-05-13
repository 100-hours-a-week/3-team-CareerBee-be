package org.choon.careerbee.domain.company.service;

public interface CompanyCommandService {

    void registWishCompany(Long accessMemberId, Long companyId);

    void deleteWishCompany(Long accessMemberId, Long companyId);
}
