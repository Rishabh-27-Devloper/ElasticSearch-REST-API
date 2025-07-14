package com.example.coursesearch.service;

import com.example.coursesearch.document.CourseDocument;
import com.example.coursesearch.repository.CourseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionService implements CommandLineRunner {
    
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data ingestion...");
        
        try {
            // Check if data already exists
            long count = courseRepository.count();
            if (count > 0) {
                log.info("Data already exists in index. Skipping ingestion. Count: {}", count);
                return;
            }
            
            // Load sample data
            List<CourseDocument> courses = loadSampleData();
            
            // Bulk index the data
            courseRepository.saveAll(courses);
            
            log.info("Successfully indexed {} courses", courses.size());
            
        } catch (Exception e) {
            log.error("Error during data ingestion: ", e);
            throw e;
        }
    }
    
    private List<CourseDocument> loadSampleData() throws IOException {
        log.info("Loading sample data from sample-courses.json");
        
        // Configure ObjectMapper for Java 8 time
        objectMapper.registerModule(new JavaTimeModule());
        
        ClassPathResource resource = new ClassPathResource("sample-courses.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<CourseDocument>>() {});
        }
    }
}