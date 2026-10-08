package com.hosteldekho.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(indexes = {
    @Index(name = "idx_hostel_manager", columnList = "manager_id"),
    @Index(name = "idx_hostel_locality", columnList = "locality"),
    @Index(name = "idx_hostel_gender", columnList = "gender_type"),
    @Index(name = "idx_hostel_coliving", columnList = "co_living"),
    @Index(name = "idx_hostel_food_type", columnList = "food_type")
})
public class Hostel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(length = 4000) private String description;
    private String address;
    private String locality;
    private String city;
    private Double latitude;
    private Double longitude;
    @Enumerated(EnumType.STRING) private Enums.GenderType genderType;
    private boolean coLiving;
    private String contactName;
    private String contactPhone;
    private String contactWhatsapp;
    private String contactEmail;
    @Column(length = 4000) private String rules;
    @Column(length = 2000) private String amenities;
    private BigDecimal securityDeposit;
    private BigDecimal maintenanceChargePerMonth;
    private BigDecimal messChargePerMonth;
    @Enumerated(EnumType.STRING) private Enums.ElectricityPolicy electricityPolicy;
    private BigDecimal electricityChargePerMonth;
    private Integer lockInMonths;
    private Integer noticePeriodDays;
    private Boolean messIncludedInRent;
    @Enumerated(EnumType.STRING) private Enums.FoodType foodType;
    @Column(length = 1000) private String mealTimings;

    @BatchSize(size = 64)
    @ElementCollection
    @CollectionTable(name = "hostel_images", joinColumns = @JoinColumn(name = "hostel_id"))
    @Column(name = "image_url", length = 1000)
    @Builder.Default private List<String> imageUrls = new ArrayList<>();

    @ManyToOne(optional = false)
    private Manager manager;

    @BatchSize(size = 64)
    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<Room> rooms = new ArrayList<>();

    @BatchSize(size = 64)
    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<HostelCollege> collegeLinks = new ArrayList<>();
}
