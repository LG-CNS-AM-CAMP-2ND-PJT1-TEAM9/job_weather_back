package com.example.job_weather_back.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_match_settings")
@Getter
@Setter
public class UserMatchSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version  // 버전 관리 추가
    private Long version = 0L;  // 기본값을 설정하여 null 오류를 방지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_sn", referencedColumnName = "user_sn", nullable = false)
    private User user;

    // 뉴스용 관심 키워드
    @ElementCollection
    @CollectionTable(name = "user_news_keywords", joinColumns = @JoinColumn(name = "setting_id"))
    @Column(name = "keyword_id")
    private Set<Integer> newsKeywordIds;

    // 채용용 관심 키워드
    @ElementCollection
    @CollectionTable(name = "user_job_keywords", joinColumns = @JoinColumn(name = "setting_id"))
    @Column(name = "keyword_id")
    private Set<Integer> jobKeywordIds;

    // 기업 유형 ID
    @ElementCollection
    @CollectionTable(name = "user_type_ids", joinColumns = @JoinColumn(name = "setting_id"))
    @Column(name = "type_id")
    private Set<Integer> typeIds;

    // 포지션 ID
    @ElementCollection
    @CollectionTable(name = "user_position_ids", joinColumns = @JoinColumn(name = "setting_id"))
    @Column(name = "position_id")
    private Set<Integer> positionIds;

    // 근무지역 ID
    @ElementCollection
    @CollectionTable(name = "user_location_ids", joinColumns = @JoinColumn(name = "setting_id"))
    @Column(name = "location_id")
    private Set<Integer> locationIds;

    // 기업 유형 ID (예: 대기업, 스타트업 등)
    @ElementCollection
    @CollectionTable(name = "user_company_type_ids", joinColumns = @JoinColumn(name = "setting_id"))
    @Column(name = "company_type_id")
    private Set<Integer> companyTypeIds;

    // 키워드 매칭 로직
    public boolean matchesAnyKeyword(List<Integer> extractedKeywords, String type) {
        Set<Integer> targetIds = type.equals("news") ? newsKeywordIds : jobKeywordIds;

        return extractedKeywords.stream()
                .anyMatch(targetIds::contains);
    }

    private Integer safeParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}