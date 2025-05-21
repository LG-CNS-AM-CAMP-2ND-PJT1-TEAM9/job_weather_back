package com.example.job_weather_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikedEmploymentDTO {
    
    // 프론트엔드에서 즐겨찾기 요청 시 보낼 사람인 채용 공고 ID
    private String saraminJobId;

}
