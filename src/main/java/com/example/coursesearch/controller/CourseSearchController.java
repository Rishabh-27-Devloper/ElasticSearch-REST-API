package com.example.coursesearch.controller;

import com.example.coursesearch.document.CourseDocument;
import com.example.coursesearch.dto.CourseSearchRequest;
import com.example.coursesearch.dto.CourseSearchResponse;
import com.example.coursesearch.dto.SuggestionResponse;
import com.example.coursesearch.service.CourseSearchService;
import com.example.coursesearch.service.CourseAutocompleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CourseSearchController {
    
    private final CourseSearchService courseSearchService;
    private final CourseAutocompleteService courseAutocompleteService;
    
    @GetMapping("/search")
    public ResponseEntity<CourseSearchResponse> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) CourseDocument.CourseType type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "upcoming") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.debug("Received search request - q: {}, minAge: {}, maxAge: {}, category: {}, type: {}, minPrice: {}, maxPrice: {}, startDate: {}, sort: {}, page: {}, size: {}",
                q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort, page, size);
        
        CourseSearchRequest request = CourseSearchRequest.builder()
                .q(q)
                .minAge(minAge)
                .maxAge(maxAge)
                .category(category)
                .type(type)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .startDate(startDate)
                .sort(sort)
                .page(page)
                .size(size)
                .build();
        
        CourseSearchResponse response = courseSearchService.searchCourses(request);
        
        log.debug("Returning search response with {} courses out of {} total", 
                response.getCourses().size(), response.getTotal());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Assignment B: Autocomplete endpoint
     * Returns suggested course titles based on partial input
     */
    @GetMapping("/search/suggest")
    public ResponseEntity<SuggestionResponse> getSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.debug("Received autocomplete request - q: '{}', size: {}", q, size);
        
        // Validate size parameter
        if (size < 1 || size > 20) {
            size = 10;
        }
        
        SuggestionResponse response = courseAutocompleteService.getSuggestions(q, size);
        
        log.debug("Returning {} autocomplete suggestions for query: '{}'", 
                response.getSuggestions().size(), q);
        
        return ResponseEntity.ok(response);
    }
}