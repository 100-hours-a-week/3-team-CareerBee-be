package org.choon.careerbee.domain.company.repository.wish;

import java.util.Optional;
import javax.swing.text.html.Option;
import org.choon.careerbee.domain.company.entity.Company;
import org.choon.careerbee.domain.company.entity.wish.WishCompany;
import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishCompanyRepository extends JpaRepository<WishCompany, Long> {

  boolean existsByMemberAndCompany(Member member, Company company);

  Optional<WishCompany> findByMemberAndCompany(Member member, Company company);
}
