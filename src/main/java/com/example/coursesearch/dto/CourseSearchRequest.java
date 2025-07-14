package com.example.coursesearch.dto;

import com.example.coursesearch.document.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchRequest {
    
    private String q; // search keyword
    private Integer minAge;
    private Integer maxAge;
    private String category;
    private CourseDocument.CourseType type;
    private Double minPrice;
    private Double maxPrice;
    private LocalDate startDate;
    private String sort = "upcoming"; // upcoming, priceAsc, priceDesc
    private Integer page = 0;
    private Integer size = 10;
    private Boolean fuzzy = false; // Enable fuzzy search
}