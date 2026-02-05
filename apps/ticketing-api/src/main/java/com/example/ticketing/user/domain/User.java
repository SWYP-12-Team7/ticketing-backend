package com.example.ticketing.user.domain;

import com.example.jpa.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(length = 100)
    private String nickname;

    @Column(length = 500)
    private String profileImage;

    @OneToMany(mappedBy = "user")
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @Column(length = 50)
    private String name; // 실명

    @Column(length = 255)
    private String address;

    private Double latitude;

    private Double longitude;

    // 온보딩 완료 여부
    @Column(name = "onboarding_completed")
    private boolean onboardingCompleted = false;

    // 온보딩 진행 상태 (건너뛰기 대응)
    @Column(name = "onboarding_step")
    private Integer onboardingStep;  // 1, 2 또는 null(미시작/완료)

    public void completeOnboarding() {
        this.onboardingCompleted = true;
        this.onboardingStep = null;
    }

    public void updateOnboardingStep(Integer step) {
        this.onboardingStep = step;
    }

    public boolean hasInProgressOnboarding() {
        return this.onboardingStep != null && !this.onboardingCompleted;
    }

    @Builder
    public User(String email, String nickname, String profileImage) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public void saveProfile(String name, String address, Double latitude, Double longitude) {
        if (this.name != null) {
            throw new CustomException(ErrorCode.NAME_ALREADY_SET);
        }
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateAddress(String address, Double latitude, Double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void withdraw() {
        this.delete();
    }
}
