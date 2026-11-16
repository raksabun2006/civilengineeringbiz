package com.engineerbiz.client.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.client.enums.ClientStatus;
import com.engineerbiz.client.enums.ClientType;
import com.engineerbiz.common.entity.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Represents client entities (developers, homeowners, corporate entities, or government bodies)
 * commissioning engineering projects.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clients", uniqueConstraints = {
        @UniqueConstraint(name = "uk_clients_code", columnNames = "client_code")
})
@SQLRestriction("deleted_at IS NULL")
public class Client extends AuditableEntity {

    @Column(name = "client_code", nullable = false, length = 30)
    private String clientCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "name_khmer", length = 150)
    private String nameKhmer;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 30)
    private ClientType clientType = ClientType.CORPORATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ClientStatus status = ClientStatus.ACTIVE;

    @Column(name = "vat_tin", length = 50)
    private String vatTin;

    @Column(name = "primary_contact_name", length = 100)
    private String primaryContactName;

    @Column(name = "primary_contact_email", length = 150)
    private String primaryContactEmail;

    @Column(name = "primary_contact_phone", length = 30)
    private String primaryContactPhone;

    @Column(name = "billing_address", length = 255)
    private String billingAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "payment_term_days")
    private Integer paymentTermDays = 30;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
