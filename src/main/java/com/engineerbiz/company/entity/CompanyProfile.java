package com.engineerbiz.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import com.engineerbiz.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Singleton configuration record holding legal, tax, and banking details
 * of the single civil engineering firm using this system.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "company_profile")
public class CompanyProfile extends BaseEntity {

    @Column(name = "company_name_en", nullable = false, length = 150)
    private String companyNameEn;

    @Column(name = "company_name_km", length = 150)
    private String companyNameKm;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "vat_tin", length = 50)
    private String vatTin;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "website", length = 150)
    private String website;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city = "Phnom Penh";

    @Column(name = "country", nullable = false, length = 60)
    private String country = "Cambodia";

    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency = "USD";

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_name", length = 100)
    private String bankAccountName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_swift_code", length = 20)
    private String bankSwiftCode;

    @Column(name = "bakong_id", length = 100)
    private String bakongId;
}
