package com.example.job_weather_back.repository;

import com.example.job_weather_back.entity.NewContents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewContentsRepository extends JpaRepository<NewContents, Long> {
    List<NewContents> findByType(String type);
 //강제로 해봄
    @Query("SELECT n FROM NewContents n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<NewContents> searchByKeyword(@Param("keyword") String keyword);
}