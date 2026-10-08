package com.hosteldekho.entity;
import jakarta.persistence.*; import lombok.*; import org.hibernate.annotations.BatchSize;
@Entity @BatchSize(size=64) @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder public class Manager { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private String name; @Column(nullable=false,unique=true) private String email; @Column(nullable=false) private String passwordHash; @Column(nullable=false) private String phone; }
