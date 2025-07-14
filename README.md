# Course Search Application - Assignment A

A Spring Boot application that provides course search functionality using Elasticsearch with advanced filtering, sorting, and pagination.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Elasticsearch 9.x running on localhost:9200
- Git

## Project Structure

```
src/
├── main/
│   ├── java/com/example/coursesearch/
│   │   ├── config/
│   │   │   └── ElasticsearchConfig.java
│   │   ├── controller/
│   │   │   └── CourseSearchController.java
│   │   ├── document/
│   │   │   └── CourseDocument.java
│   │   ├── dto/
│   │   │   ├── CourseSearchRequest.java
│   │   │   └── CourseSearchResponse.java
│   │   ├── repository/
│   │   │   └── CourseRepository.java
│   │   ├── service/
│   │   │   ├── CourseSearchService.java
│   │   │   └── DataIngestionService.java
│   │   └── CourseSearchApplication.java
│   └── resources/
│       ├── application.properties
│       └── sample-courses.json
└── test/
```

## Setup Instructions

### 1. Verify Elasticsearch is Running

```bash
curl http://localhost:9200
```

Expected response:
```json
{
  "name" : "...",
  "cluster_name" : "elasticsearch",
  "version" : {
    "number" : "9.x.x",
    ...
  }
}
```

### 2. Clone and Build the Project

```bash
git clone https://github.com/Rishabh-27-Devloper/ElasticSearch-REST-API.git
cd ElasticSearch-REST-API
.\mvnw.cmd clean install
```

### 3. Run the Application

```bash
.\mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Verify Data Ingestion

Check the logs for successful data ingestion:
```
INFO  - Starting data ingestion...
INFO  - Successfully indexed 52 courses
```

You can also verify in Elasticsearch:
```bash
curl -X GET "localhost:9200/courses/_count"
```

## API Endpoints

### Search Courses

**Endpoint:** `GET /api/search`

**Query Parameters:**
- `q` (optional): Search keyword for title and description
- `minAge` (optional): Minimum age filter
- `maxAge` (optional): Maximum age filter
- `category` (optional): Course category filter
- `type` (optional): Course type filter (ONE_TIME, COURSE, CLUB)
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter
- `startDate` (optional): Start date filter (ISO-8601 format)
- `sort` (optional): Sort option (upcoming, priceAsc, priceDesc) - default: upcoming
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

**Response Format:**
```json
{
  "total": 52,
  "courses": [
    {
      "id": "1",
      "title": "Introduction to Mathematics",
      "description": "A comprehensive introduction to basic mathematical concepts...",
      "category": "Math",
      "type": "COURSE",
      "gradeRange": "6th-8th",
      "minAge": 11,
      "maxAge": 14,
      "price": 150.00,
      "nextSessionDate": "2025-08-15T10:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalPages": 6
}
```

## API Usage Examples

### 1. Basic Search - All Courses

```bash
curl -X GET "http://localhost:8080/api/search"
```

### 2. Search by Keyword

```bash
curl -X GET "http://localhost:8080/api/search?q=mathematics"
```

### 3. Filter by Category

```bash
curl -X GET "http://localhost:8080/api/search?category=Science"
```

### 4. Filter by Age Range

```bash
curl -X GET "http://localhost:8080/api/search?minAge=12&maxAge=16"
```

### 5. Filter by Course Type

```bash
curl -X GET "http://localhost:8080/api/search?type=COURSE"
```

### 6. Filter by Price Range

```bash
curl -X GET "http://localhost:8080/api/search?minPrice=100&maxPrice=200"
```

### 7. Filter by Start Date

```bash
curl -X GET "http://localhost:8080/api/search?startDate=2025-08-20T00:00:00"
```

### 8. Sort by Price (Ascending)

```bash
curl -X GET "http://localhost:8080/api/search?sort=priceAsc"
```

### 9. Sort by Price (Descending)

```bash
curl -X GET "http://localhost:8080/api/search?sort=priceDesc"
```

### 10. Pagination

```bash
curl -X GET "http://localhost:8080/api/search?page=1&size=5"
```

### 11. Complex Search with Multiple Filters

```bash
curl -X GET "http://localhost:8080/api/search?q=programming&category=Technology&minAge=13&maxAge=18&minPrice=200&maxPrice=300&sort=priceAsc&page=0&size=5"
```

### 12. Search Science Courses for Teenagers

```bash
curl -X GET "http://localhost:8080/api/search?category=Science&minAge=13&maxAge=18&sort=upcoming"
```

### 13. Find Affordable Courses Under $100

```bash
curl -X GET "http://localhost:8080/api/search?maxPrice=100&sort=priceAsc"
```

### 14. Search Art Courses

```bash
curl -X GET "http://localhost:8080/api/search?category=Art&type=COURSE"
```

### 15. Find Clubs for Elementary Students

```bash
curl -X GET "http://localhost:8080/api/search?type=CLUB&maxAge=12"
```

## Expected Behavior

### Search Functionality
- **Full-text search**: Searches in both title and description fields
- **Title boost**: Title matches are given higher relevance than description matches
- **Case-insensitive**: Search terms are case-insensitive

### Filtering
- **Age filters**: `minAge` and `maxAge` work with course's age range
- **Category filter**: Exact match on category field
- **Type filter**: Exact match on course type
- **Price range**: Filter courses within specified price range
- **Date filter**: Show only courses on or after the specified date

### Sorting
- **Default (upcoming)**: Sort by next session date (ascending)
- **Price ascending**: Sort by price from low to high
- **Price descending**: Sort by price from high to low

### Pagination
- **Zero-based indexing**: Page numbers start from 0
- **Default page size**: 10 courses per page
- **Total pages**: Calculated based on total results and page size

## Testing the Application

### Test Data Verification
Verify that all 52 courses are indexed:
```bash
curl -X GET "http://localhost:8080/api/search?size=100" | jq '.total'
```

### Test Search Functionality
```bash
# Should return courses containing "math"
curl -X GET "http://localhost:8080/api/search?q=math"

# Should return only Science courses
curl -X GET "http://localhost:8080/api/search?category=Science"

# Should return courses suitable for 10-year-olds
curl -X GET "http://localhost:8080/api/search?minAge=10&maxAge=10"
```

### Test Sorting
```bash
# Verify price sorting (ascending)
curl -X GET "http://localhost:8080/api/search?sort=priceAsc&size=5"

# Verify price sorting (descending)
curl -X GET "http://localhost:8080/api/search?sort=priceDesc&size=5"
```

## Troubleshooting

### Common Issues

1. **Elasticsearch Connection Error**
   - Ensure Elasticsearch is running on localhost:9200
   - Check firewall settings
   - Verify Elasticsearch cluster health

2. **Data Not Loading**
   - Check application logs for ingestion errors
   - Verify sample-courses.json is in src/main/resources
   - Check Elasticsearch index exists: `curl -X GET "localhost:9200/courses"`

3. **Search Results Empty**
   - Verify data was indexed successfully
   - Check query parameters are correct
   - Review application logs for errors

### Logs
Enable debug logging by adding to application.properties:
```properties
logging.level.com.example.coursesearch=DEBUG
```

## Performance Considerations

- **Efficient queries**: Uses Elasticsearch filters instead of queries where possible
- **Pagination**: Implements proper pagination to handle large result sets
- **Index optimization**: Proper field mappings for optimal search performance

## Course Categories Available
- Math
- Science
- Art
- Language
- Technology
- Life Skills
- Music
- Sports
- Games
- History

## Course Types Available
- `ONE_TIME`: Single session courses
- `COURSE`: Multi-session courses
- `CLUB`: Regular club meetings

## Next Steps for Assignment B (Bonus)

To implement Assignment B features:
1. Add completion suggester mapping to the index
2. Create autocomplete endpoint `/api/search/suggest`
3. Implement fuzzy matching in the main search
4. Update documentation with new features

## Author
Created for Spring Boot Elasticsearch Assignment A
