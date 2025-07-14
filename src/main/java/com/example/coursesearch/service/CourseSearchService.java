package com.example.coursesearch.service;

import com.example.coursesearch.document.CourseDocument;
import com.example.coursesearch.dto.CourseSearchRequest;
import com.example.coursesearch.dto.CourseSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.json.JsonData;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public CourseSearchResponse searchCourses(CourseSearchRequest request) {
        log.debug("Searching courses with request: {}", request);

        try {
            Query searchQuery = buildSearchQuery(request);
            SearchHits<CourseDocument> searchHits = elasticsearchOperations.search(searchQuery, CourseDocument.class);

            List<CourseDocument> courses = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            long total = searchHits.getTotalHits();
            int totalPages = (int) Math.ceil((double) total / request.getSize());

            log.debug("Found {} courses out of {} total", courses.size(), total);

            return CourseSearchResponse.builder()
                    .total(total)
                    .courses(courses)
                    .page(request.getPage())
                    .size(request.getSize())
                    .totalPages(totalPages)
                    .build();
        } catch (Exception e) {
            log.error("Error searching courses: {}", e.getMessage(), e);
            // Return empty response on error
            return CourseSearchResponse.builder()
                    .total(0)
                    .courses(List.of())
                    .page(request.getPage())
                    .size(request.getSize())
                    .totalPages(0)
                    .build();
        }
    }

    private Query buildSearchQuery(CourseSearchRequest request) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // Enhanced full-text search
        if (request.getQ() != null && !request.getQ().trim().isEmpty()) {
            BoolQuery.Builder textSearchBuilder = new BoolQuery.Builder();

            // Primary search - exact/standard matching with boost
            MultiMatchQuery exactMatchQuery = MultiMatchQuery.of(m -> m
                    .query(request.getQ())
                    .fields("title^3.0", "description^1.0") // Higher boost for title
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
            );
            textSearchBuilder.should(exactMatchQuery._toQuery());

            // Optional: Fuzzy search for typo tolerance (use sparingly for performance)
            if (request.getFuzzy() != null && request.getFuzzy()) {
                String[] searchTerms = request.getQ().trim().split("\\s+");
                for (String term : searchTerms) {
                    if (term.length() > 2) { // Only apply fuzzy for terms longer than 2 characters
                        // Fuzzy search on title
                        FuzzyQuery fuzzyTitleQuery = FuzzyQuery.of(f -> f
                                .field("title")
                                .value(term)
                                .fuzziness("AUTO")
                                .maxExpansions(50)
                                .prefixLength(1)
                        );
                        textSearchBuilder.should(fuzzyTitleQuery._toQuery());

                        // Fuzzy search on description with lower boost
                        FuzzyQuery fuzzyDescQuery = FuzzyQuery.of(f -> f
                                .field("description")
                                .value(term)
                                .fuzziness("AUTO")
                                .maxExpansions(30)
                                .prefixLength(1)
                        );
                        textSearchBuilder.should(fuzzyDescQuery._toQuery());
                    }
                }
            }

            boolQueryBuilder.must(textSearchBuilder.build()._toQuery());
        }

        // Age range filters
        if (request.getMinAge() != null) {
            RangeQuery rangeQuery = RangeQuery.of(r -> r
                    .field("maxAge")
                    .gte(JsonData.of(request.getMinAge()))
            );
            boolQueryBuilder.filter(rangeQuery._toQuery());
        }
        if (request.getMaxAge() != null) {
            RangeQuery rangeQuery = RangeQuery.of(r -> r
                    .field("minAge")
                    .lte(JsonData.of(request.getMaxAge()))
            );
            boolQueryBuilder.filter(rangeQuery._toQuery());
        }

        // Category filter
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            TermQuery termQuery = TermQuery.of(t -> t
                    .field("category")
                    .value(request.getCategory())
            );
            boolQueryBuilder.filter(termQuery._toQuery());
        }

        // Type filter
        if (request.getType() != null) {
            TermQuery termQuery = TermQuery.of(t -> t
                    .field("type")
                    .value(request.getType().toString())
            );
            boolQueryBuilder.filter(termQuery._toQuery());
        }

        // Price range filters
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            RangeQuery.Builder priceRangeBuilder = new RangeQuery.Builder().field("price");
            if (request.getMinPrice() != null) {
                priceRangeBuilder.gte(JsonData.of(request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                priceRangeBuilder.lte(JsonData.of(request.getMaxPrice()));
            }
            boolQueryBuilder.filter(priceRangeBuilder.build()._toQuery());
        }

        // Date filter - show only courses on or after the given date
        if (request.getStartDate() != null) {
            RangeQuery rangeQuery = RangeQuery.of(r -> r
                    .field("nextSessionDate")
                    .gte(JsonData.of(request.getStartDate().toString()))
            );
            boolQueryBuilder.filter(rangeQuery._toQuery());
        }

        // Sorting
        Sort sort = buildSort(request.getSort());

        // Pagination
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        co.elastic.clients.elasticsearch._types.query_dsl.Query elasticQuery = boolQueryBuilder.build()._toQuery();

        return NativeQuery.builder()
                .withQuery(elasticQuery)
                .withPageable(pageable)
                .build();
    }

    private Sort buildSort(String sortParam) {
        if (sortParam == null) {
            sortParam = "upcoming";
        }

        return switch (sortParam.toLowerCase()) {
            case "priceasc" -> Sort.by(Sort.Direction.ASC, "price");
            case "pricedesc" -> Sort.by(Sort.Direction.DESC, "price");
            case "upcoming", "default" -> Sort.by(Sort.Direction.ASC, "nextSessionDate");
            default -> Sort.by(Sort.Direction.ASC, "nextSessionDate");
        };
    }
}