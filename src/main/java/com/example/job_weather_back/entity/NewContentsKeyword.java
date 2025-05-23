//뉴스나 채용 공고가 가지고 있는 키워드 정보를 따로 분리해서 저장하는 테이블

package com.example.job_weather_back.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "new_contents_keywords")
public class NewContentsKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
<<<<<<< HEAD
    @JoinColumn(name = "new_contents_id", nullable = false)
    private NewContents newContents;

=======
    @JoinColumn(name = "new_contents_id")
    private NewContents newContents;


>>>>>>> b4f1a8eaec03cc7fbaccc36024fc85994059880d
    @Column(length = 100, nullable = false)
    private String keyword;

}