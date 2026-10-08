package com.hosteldekho.entity;
import jakarta.persistence.*; import lombok.*;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder public class College { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,unique=true) private String name; @Column(unique=true) private String slug; private String locality; private String city; private Double latitude; private Double longitude; }
