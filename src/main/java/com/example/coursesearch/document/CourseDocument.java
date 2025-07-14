package com.example.coursesearch.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.CompletionField;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "courses")
public class CourseDocument {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Keyword)
    private CourseType type;
    
    @Field(type = FieldType.Keyword)
    private String gradeRange;
    
    @Field(type = FieldType.Integer)
    private Integer minAge;
    
    @Field(type = FieldType.Integer)
    private Integer maxAge;
    
    @Field(type = FieldType.Double)
    private Double price;
    
    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate nextSessionDate;
    
    // Completion field for autocomplete suggestions
   @CompletionField(maxInputLength = 100)
    private String[] titleSuggest;
    
    // Helper method to create completion field from title
    public static String[] createTitleSuggest(String title) {
        if (title == null || title.trim().isEmpty()) {
            return new String[0];
        }
        
        String[] words = title.toLowerCase().split("\\s+");
        if (words.length > 1) {
            String[] suggestions = new String[words.length + 1];
            suggestions[0] = title;
            System.arraycopy(words, 0, suggestions, 1, words.length);
            return suggestions;
        }
        return new String[]{title};
    }
    
    // Builder pattern enhancement to auto-create titleSuggest
    public static class CourseDocumentBuilder {
        public CourseDocumentBuilder title(String title) {
            this.title = title;
            this.titleSuggest = createTitleSuggest(title);
            return this;
        }
    }
    
    public enum CourseType {
        ONE_TIME, COURSE, CLUB
    }
}