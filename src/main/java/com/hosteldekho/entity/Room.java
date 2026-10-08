package com.hosteldekho.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"hostel_id", "roomNumber"}), indexes = {
    @Index(name = "idx_room_hostel", columnList = "hostel_id"),
    @Index(name = "idx_room_ac_type", columnList = "ac_type"),
    @Index(name = "idx_room_monthly_price", columnList = "price_per_month")
})
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Hostel hostel;
    @Column(nullable = false) private String roomNumber;
    @Enumerated(EnumType.STRING) private Enums.AcType acType;
    private int capacity;
    private int occupied;
    private BigDecimal pricePerMonth;
    private BigDecimal pricePerDay;
    private java.time.Instant availabilityUpdatedAt;
    @Column(length = 2000) private String description;
    @Column(length = 1000) private String amenities;

    @BatchSize(size = 64)
    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url", length = 300000)
    @Builder.Default private List<String> imageUrls = new ArrayList<>();

    public int getVacantBeds() { return capacity - occupied; }
}
