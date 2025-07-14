package com.example.coursesearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.coursesearch.repository")
public class ElasticsearchConfig {
    // No need to extend AbstractElasticsearchConfiguration in newer versions
    // Spring Boot auto-configuration handles the connection based on application.properties
}