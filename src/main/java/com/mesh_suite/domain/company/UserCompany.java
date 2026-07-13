package com.mesh_suite.domain.company;

import com.mesh_suite.constant.company.BuildStatus;
import com.mesh_suite.constant.company.CompanyStatus;
import com.mesh_suite.constant.forms.CompanyCurrency;
import com.mesh_suite.domain.user.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_company")
public class UserCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false, unique = true)
    private String companyName;

    @Column(name = "description", unique = true)
    private String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CompanyStatus status;

    @Column(name = "primary_contact_name", nullable = false)
    private String primaryContactName;

    @Column(name = "primary_contact_email", nullable = false)
    private String primaryContactEmail;

    @Column(name = "primary_contact_phone_number")
    private String primaryContactPhoneNumber;

    @Column(name = "company_logo")
    private String companyLogo;

    @Column(name = "company_address")
    private String companyAddress;

    @Column(name = "company_digital_address")
    private String companyDigitalAddress;

    @Column(name = "industry")
    private String industry;

    @Column(name = "company_merchant_momo_number")
    private String companyMerchantMomoNumber;

    @Column(name = "company_bank_name")
    private String companyBankName;

    @Column(name = "form_set")
    @ElementCollection
    private List<Long> assignedFormIds;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "start_of_day_time")
    private Time startOfDayTime;

    @Column(name = "end_of_day_time")
    private Time endOfDayTime;

    @Column(name = "primary_currency", nullable = false)
    private CompanyCurrency primaryCurrency;

    @Column(name = "secondary_currency")
    @ElementCollection
    private List<CompanyCurrency> secondaryCurrency;

    @ManyToOne
    @JoinColumn(name = "company_admin_id", referencedColumnName = "id")
    private Users companyAdmin;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_on")
    private ZonedDateTime createdOn;

    @Column(name = "updated_on")
    private ZonedDateTime updatedOn;

    @Column(name = "deleted_on")
    private ZonedDateTime deletedOn;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "build_status")
    @Enumerated(EnumType.STRING)
    private BuildStatus buildStatus;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "db_url")
    private String dbUrl;

    @Column(name = "company_identifier")
    private String companyIdentifier;

    public void generateAndSetCompanyIdentifier() {
        if (this.companyName != null && !this.companyName.isEmpty()) {
            this.companyIdentifier = generateCompanyIdentifier(this.companyName);
        }
    }

    private static String generateCompanyIdentifier(String companyName) {
        String cleanedName = companyName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String namePart = cleanedName.substring(0, Math.min(cleanedName.length(), 4));
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        return namePart + randomNumber;
    }

    public void setupDatabaseConfig(String host, int port, String username, String password) {
        if (this.companyIdentifier == null || this.companyIdentifier.isEmpty()) {
            throw new IllegalStateException("Company identifier must be generated before setting DB config");
        }

        this.driverName = "org.postgresql.Driver";
        this.dbUrl = String.format(
                "jdbc:postgresql://%s:%d/%s",
                host,
                port,
                this.companyIdentifier // database name == companyIdentifier
        );
    }

}
