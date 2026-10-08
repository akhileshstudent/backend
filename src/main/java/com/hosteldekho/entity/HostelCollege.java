package com.hosteldekho.entity;
import jakarta.persistence.*; import lombok.*;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"hostel_id", "college_id"}), indexes = {
    @Index(name = "idx_hostel_college_hostel", columnList = "hostel_id"),
    @Index(name = "idx_hostel_college_college", columnList = "college_id")
})
public class HostelCollege {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Hostel hostel;
    @ManyToOne(optional = false) private College college;
    @Column(nullable = false) private Double distanceKm;
}
