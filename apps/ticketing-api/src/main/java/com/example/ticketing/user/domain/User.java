package com.example.ticketing.user.domain;

import com.example.jpa.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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

    // 온보딩 완료 여부
    @Column(name = "onboarding_completed")
    private boolean onboardingCompleted = false;

    // 최대 이동 시간
    @Column(name = "max_travel_time")
    private Integer maxTravelTime;  // 30, 60, null (제한없음)

    // 온보딩 진행 상태 (중간 이탈 대응)
    @Column(name = "onboarding_step")
    private Integer onboardingStep;  // 1, 2, 3 또는 null(미시작/완료)

    // Step3에서 마지막으로 응답한 콘텐츠 인덱스 (0-based)
    @Column(name = "last_content_index")
    private Integer lastContentIndex;


    public void completeOnboarding() {
        this.onboardingCompleted = true;
        this.onboardingStep = null;  // 완료 시 초기화
        this.lastContentIndex = null;
    }

    public void updateMaxTravelTime(Integer minutes) {
        this.maxTravelTime = minutes;
    }

    public void updateOnboardingStep(Integer step) {
        this.onboardingStep = step;
    }

    public void updateLastContentIndex(Integer index) {
        this.lastContentIndex = index;
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

    public void withdraw() {
        this.delete();
    }
}
