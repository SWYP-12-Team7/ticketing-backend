package com.example.ticketing.user.domain;

import com.example.jpa.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "user_regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RegionTag tag;

    @Builder
    public UserRegion(Long userId, String address, BigDecimal latitude, 
                      BigDecimal longitude, RegionTag tag) {
        this.userId = userId;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tag = tag;
    }
}
