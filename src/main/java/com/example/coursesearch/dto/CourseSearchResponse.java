package com.example.coursesearch.dto;

import com.example.coursesearch.document.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchResponse {
    
    private long total;
    private List<CourseDocument> courses;
    private int page;
    private int size;
    private int totalPages;
}