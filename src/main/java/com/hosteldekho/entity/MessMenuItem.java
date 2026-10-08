package com.hosteldekho.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"hostel_id","dayOfWeek"}))
public class MessMenuItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private Hostel hostel;
    @Enumerated(EnumType.STRING) private DayOfWeek dayOfWeek;
    @Column(length=500) private String breakfast;
    @Column(length=500) private String lunch;
    @Column(length=500) private String snacks;
    @Column(length=500) private String dinner;
}
