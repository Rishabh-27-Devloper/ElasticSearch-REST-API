# Course Search Spring Boot + Elasticsearch

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot) [![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.x-orange.svg)](https://www.elastic.co/) [![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/) [![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/) [![Git](https://img.shields.io/badge/Git-latest-gray.svg)](https://git-scm.com/)

> A Spring Boot application powered by Elasticsearch for real-time course search, featuring full-text search, advanced filtering, sorting, **autocomplete suggestions**, **fuzzy search**, pagination, and more.

---

## 🚀 Features

* Full-text search on course title and description
* Advanced filtering (age, price, category, type, date)
* Autocomplete suggestions for course titles
* Fuzzy search (typo-tolerant)
* Sorting (by upcoming session or price)
* Pagination
* Bulk indexing from JSON at startup
* REST API endpoints

---

## 📚 Technologies Used

| Tool/Technology           | Purpose                        |
| ------------------------- | ------------------------------ |
| Java 17                   | Programming language           |
| Spring Boot               | Application framework          |
| Spring Data Elasticsearch | Integration with Elasticsearch |
| Elasticsearch 8.11.0      | Search engine backend          |
| Docker & Docker Compose   | Environment setup              |
| Maven                     | Build tool                     |
| Lombok                    | Boilerplate code reduction     |

---

## 📂 Project Directory Structure

```
src/main/java/com/example/coursesearch/
├── config/                     # Configuration classes
│   └── ElasticsearchConfig.java
├── controller/                # REST API endpoints
│   └── CourseSearchController.java
├── document/                  # Elasticsearch document classes
│   └── CourseDocument.java
├── dto/                       # DTOs for API requests/responses
│   ├── CourseSearchRequest.java
│   ├── CourseSearchResponse.java
│   └── SuggestionResponse.java
├── repository/                # Spring Data Elasticsearch Repos
│   └── CourseRepository.java
├── service/                   # Business logic layer
│   ├── CourseSearchService.java
│   ├── CourseAutocompleteService.java
│   └── DataIngestionService.java
└── CourseSearchApplication.java
```

---

## 📦 Sample Data

File: `src/main/resources/sample-courses.json`

Each course object includes:

* `id`, `title`, `description`, `category`, `type`, `gradeRange`
* `minAge`, `maxAge`, `price`, `nextSessionDate`

Automatically bulk indexed on app startup.

---

## 🔧 Setup Instructions

### Prerequisites

* Java 17+
* Maven 3.6+
* Docker & Docker Compose

### Step-by-step

```bash
git clone https://github.com/Rishabh-27-Devloper/ElasticSearch-REST-API.git
cd ElasticSearch-REST-API
mvn clean package
docker-compose up -d
mvn spring-boot:run
```

Verify Elasticsearch:

```bash
curl http://localhost:9200
```

---

## 🔍 API Endpoints

### 1. `GET /api/search`

#### Query Parameters:

* `q` (search keyword)
* `minAge`, `maxAge`
* `category`, `type`
* `minPrice`, `maxPrice`
* `startDate` (ISO format)
* `sort` = `upcoming`, `priceAsc`, `priceDesc`
* `page`, `size`

#### Fuzzy Matching Enabled

* Query like `q=dinors` will still return `Dinosaurs 101`

#### Example:

```bash
curl "http://localhost:8080/api/search?q=math&minAge=5"
```

### 2. `GET /api/search/suggest`

#### Query Parameters:

* `q` (partial title, required)
* `size` (optional, default = 10)

#### Example:

```bash
curl "http://localhost:8080/api/search/suggest?q=prog"
```

#### Response:

```json
{
  "suggestions": [
    "Programming Fundamentals",
    "Advanced Programming",
    "Programming for Kids"
  ]
}
```

---

## ⚙️ Configuration

`application.properties`

```properties
spring.elasticsearch.uris=http://localhost:9200
app.elasticsearch.index.courses=courses
```

---

## 🔹 Example Search Calls

```bash
# Basic search
curl "http://localhost:8080/api/search?q=math"

# Filter by category
curl "http://localhost:8080/api/search?category=Science"

# Fuzzy typo search
curl "http://localhost:8080/api/search?q=dinors"

# Autocomplete suggest
curl "http://localhost:8080/api/search/suggest?q=adv"
```

---

## 🔬 Search Logic

* **Multi-match full-text** on `title`, `description`
* **Filters**: min/max age, price, category, type, startDate
* **Sorting**: default `nextSessionDate`, optional `priceAsc` / `priceDesc`
* **Fuzziness**: enabled for query length > 2, fuzzy typo-tolerant search
* **Autocomplete**: Completion Suggester on title

---

## 📊 Health & Monitoring

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:9200/_cluster/health
```

---

## ✅ Testing

```bash
mvn test
```

* Integration tests for basic search and suggestions recommended
* Optionally use Testcontainers for ephemeral Elasticsearch instance

---

## 📦 Docker Compose

File: `docker-compose.yml`

```yaml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
```

Start:

```bash
docker-compose up -d
```

---

## 📡 API Endpoints & Usage

### 1. Search Courses

```
GET /api/search
```

**Query Parameters** (all optional):

* `q` - Keyword (full-text on title & description)
* `category` - Exact match (e.g., Math, Science)
* `type` - ONE\_TIME | COURSE | CLUB
* `minAge`, `maxAge` - Numeric filters
* `minPrice`, `maxPrice` - Price filters
* `startDate` - ISO-8601 date (e.g., 2025-08-01)
* `sort` - upcoming (default), priceAsc, priceDesc
* `page`, `size` - Pagination (0-based, default size=10)

**Sample Response**:

```json
{
  "total":52,
  "page":0,
  "size":10,
  "totalPages":6,
  "courses":[
    { "id":"1","title":"Intro to Math","category":"Math","price":150.0,"nextSessionDate":"2025-08-15" }
    // ...
  ]
}
```

### 2. Example Requests

* **All courses**:
  
  ```bash
  curl "http://localhost:8080/api/search"
  ```
  *Sample Response*:

  ```json
  {
    "total":52,
    "page":0,
    "size":10,
    "totalPages":6,
    "courses":[
      { "id":"1","title":"Intro to Math","category":"Math","price":150.0,"nextSessionDate":"2025-08-15" }
      // ...
    ]
  }
  ```
  
* **By keyword**:
  
  ```bash
  curl "http://localhost:8080/api/search?q=physics"
  ```
  *Sample Response*:

  ```json
  {
    "total": 2,
    "courses": [
      {
        "id": "2",
        "title": "Physics for Beginners",
        "description": "Explore the fundamental principles of physics through hands-on experiments and interactive demonstrations. Learn about motion, forces, and energy.",
        "category": "Science",
        "type": "COURSE",
        "gradeRange": "9th-12th",
        "minAge": 14,
        "maxAge": 18,
        "price": 200,
        "nextSessionDate": "2025-08-20"
      },
      {
        "id": "49",
        "title": "Advanced Physics",
        "description": "Explore advanced physics concepts including quantum mechanics and relativity. For highly motivated students.",
        "category": "Science",
        "type": "COURSE",
        "gradeRange": "11th-12th",
        "minAge": 16,
        "maxAge": 18,
        "price": 280,
        "nextSessionDate": "2025-09-03"
      }
    ],
    "page": 0,
    "size": 10,
    "totalPages": 1
  }
  ```
* **Filters**:

  ```bash
  curl "http://localhost:8080/api/search?category=Science&minAge=12&maxAge=16&sort=priceAsc"
  ```
  *Sample Response*:

  ```json
  {
    "total": 9,
    "courses": [
      {
        "id": "28",
        "title": "Astronomy Club",
        "description": "Explore the universe through telescope observations and astronomy discussions. Weather permitting outdoor sessions.",
        "category": "Science",
        "type": "CLUB",
        "gradeRange": "4th-9th",
        "minAge": 9,
        "maxAge": 15,
        "price": 50,
        "nextSessionDate": "2025-08-16"
      },
      {
        "id": "18",
        "title": "Science Fair Preparation",
        "description": "Prepare for science fairs with project guidance and presentation skills. One-on-one mentoring included.",
        "category": "Science",
        "type": "ONE_TIME",
        "gradeRange": "5th-8th",
        "minAge": 10,
        "maxAge": 14,
        "price": 110,
        "nextSessionDate": "2025-08-11"
      },
      {
        "id": "6",
        "title": "Biology Lab Experiments",
        "description": "Hands-on biology experiments covering cell structure, genetics, and ecology. Laboratory equipment provided.",
        "category": "Science",
        "type": "COURSE",
        "gradeRange": "9th-11th",
        "minAge": 14,
        "maxAge": 17,
        "price": 180,
        "nextSessionDate": "2025-08-18"
      }
    ],
    "page": 0,
    "size": 3,
    "totalPages": 3
  }
  ```
* **Pagination**:

  ```bash
  curl "http://localhost:8080/api/search?page=1&size=2"
  ```
  *Sample Response*:

  ```json
  {
    "total": 52,
    "courses": [
      {
        "id": "4",
        "title": "Chess Club",
        "description": "Join our weekly chess club and improve your strategic thinking skills. All skill levels welcome!",
        "category": "Games",
        "type": "CLUB",
        "gradeRange": "3rd-8th",
        "minAge": 8,
        "maxAge": 14,
        "price": 30,
        "nextSessionDate": "2025-08-12"
      },
      {
        "id": "22",
        "title": "Public Speaking",
        "description": "Build confidence and improve communication skills through public speaking exercises and presentations.",
        "category": "Life Skills",
        "type": "ONE_TIME",
        "gradeRange": "6th-12th",
        "minAge": 11,
        "maxAge": 18,
        "price": 90,
        "nextSessionDate": "2025-08-12"
      }
    ],
    "page": 1,
    "size": 2,
    "totalPages": 26
  }
  ```

---

## 📅 Submission Checklist

* [x] All source code committed
* [x] Sample data included
* [x] Elasticsearch starts via Docker
* [x] Endpoints tested
* [x] README includes curl examples
* [x] Bonus: Autocomplete and Fuzzy search implemented

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See the [LICENSE](LICENSE) file for more details.

---

## 🙌 Acknowledgements

Thanks to the evaluation team for the opportunity to showcase this project. Please reach out via the form for any clarifications.


## 🙋 Author

*Prakhar Shukla* – B.Tech in Electronics & Communication Engineering (2023–27)

> Feel free to reach out for questions or improvements!
