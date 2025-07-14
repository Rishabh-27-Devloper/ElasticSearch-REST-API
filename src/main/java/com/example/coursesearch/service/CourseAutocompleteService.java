package com.example.coursesearch.service;

import com.example.coursesearch.document.CourseDocument;
import com.example.coursesearch.dto.SuggestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseAutocompleteService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SuggestionResponse getSuggestions(String query, int size) {
        log.debug("Getting autocomplete suggestions for query: '{}', size: {}", query, size);

        if (query == null || query.trim().isEmpty()) {
            return SuggestionResponse.builder()
                    .suggestions(new ArrayList<>())
                    .build();
        }

        try {
            // Build efficient search query using Elasticsearch capabilities
            Query searchQuery = buildSuggestionQuery(query.trim().toLowerCase(), size);
            SearchHits<CourseDocument> searchHits = elasticsearchOperations.search(searchQuery, CourseDocument.class);

            // Extract unique suggestions from search results
            Set<String> uniqueSuggestions = new LinkedHashSet<>();
            for (SearchHit<CourseDocument> hit : searchHits) {
                CourseDocument course = hit.getContent();
                if (course.getTitle() != null && !course.getTitle().trim().isEmpty()) {
                    uniqueSuggestions.add(course.getTitle());
                    if (uniqueSuggestions.size() >= size) {
                        break;
                    }
                }
            }

            List<String> suggestions = new ArrayList<>(uniqueSuggestions);

            log.debug("Found {} autocomplete suggestions for query: '{}'", suggestions.size(), query);

            return SuggestionResponse.builder()
                    .suggestions(suggestions)
                    .build();

        } catch (Exception e) {
            log.error("Error getting autocomplete suggestions for query: '{}', error: {}", query, e.getMessage(), e);
            return SuggestionResponse.builder()
                    .suggestions(new ArrayList<>())
                    .build();
        }
    }

    private Query buildSuggestionQuery(String query, int size) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // Prefix query for exact prefix matching (highest priority)
        PrefixQuery prefixQuery = PrefixQuery.of(p -> p
                .field("title")
                .value(query)
        );
        boolQueryBuilder.should(prefixQuery._toQuery());

        // Wildcard query for contains matching (lower priority)
        WildcardQuery wildcardQuery = WildcardQuery.of(w -> w
                .field("title")
                .value("*" + query + "*")
        );
        boolQueryBuilder.should(wildcardQuery._toQuery());

        // Set minimum should match to ensure at least one condition is met
        boolQueryBuilder.minimumShouldMatch("1");

        co.elastic.clients.elasticsearch._types.query_dsl.Query elasticQuery = boolQueryBuilder.build()._toQuery();

        return NativeQuery.builder()
                .withQuery(elasticQuery)
                .withMaxResults(size * 2) // Get more results to account for duplicates
                .build();
    }
}