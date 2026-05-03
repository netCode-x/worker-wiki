package com.codebit.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "about_id", nullable = false)
    private AboutPage about;

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(length = 50)
    private String displayName;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 100)
    private String iconClass;

    @Column(length = 20)
    private String color;

    private Integer sortOrder = 0;

    private Integer enabled = 1;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}