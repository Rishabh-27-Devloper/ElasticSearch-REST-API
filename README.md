# Course Search Application (Assignment A)

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot) [![Elasticsearch](https://img.shields.io/badge/Elasticsearch-9.x-orange.svg)](https://www.elastic.co/) [![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/) [![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/) [![Git](https://img.shields.io/badge/Git-latest-gray.svg)](https://git-scm.com/)

A Spring Boot application providing powerful course-search functionality on top of Elasticsearch, including full-text queries, advanced filters, sorting, and pagination.

---

## 📋 Table of Contents

1. [Prerequisites](#-prerequisites)
2. [Project Setup](#-project-setup)
3. [Configuration](#-configuration)
4. [Running the Application](#-running-the-application)
5. [Running with Docker](#-running-with-docker)
6. [API Endpoints & Usage](#-api-endpoints--usage)
7. [Debugging & Troubleshooting](#-debugging--troubleshooting)
8. [Assignment A Checklist](#-assignment-a-checklist)
9. [Next Steps (Bonus)](#-next-steps-bonus)
10. [Author](#-author)

---

## 🛠️ Prerequisites

Before you begin, ensure you have the following installed:

| Tool              | Required Version | Installation Guide                                                                                             |
| ----------------- | ---------------- | -------------------------------------------------------------------------------------------------------------- |
| **Java**          | 17 or higher     | Download from [Oracle JDK](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)               |
| **Maven**         | 3.6 or higher    | Download from [Apache Maven](https://maven.apache.org/download.cgi), then unpack and add `bin/` to your `PATH` |
| **Elasticsearch** | 7.x or 8.x       | Download from [Elastic.co](https://www.elastic.co/downloads/elasticsearch)                                     |
| **Docker**        | Latest           | Download from [Docker Desktop](https://www.docker.com/products/docker-desktop/)                                |
| **Git**           | Latest           | Download from [Git SCM](https://git-scm.com/downloads)                                                         |

> **Tip:** Confirm Java & Maven installation:
>
> ```bash
> java -version
> mvn -version
> ```

---

## 🚀 Project Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/Rishabh-27-Devloper/ElasticSearch-REST-API.git
   cd ElasticSearch-REST-API
   ```

2. **Build the project**

   ```bash
   mvn clean package -DskipTests
   ```

---

## ⚙️ Configuration

Ensure `application.properties` is configured for Docker:

```properties
server.port=8080
spring.elasticsearch.uris=http://elasticsearch:9200
spring.elasticsearch.connection-timeout=5s
spring.elasticsearch.socket-timeout=60s
app.elasticsearch.index.courses=courses
logging.level.com.example.coursesearch=DEBUG
```

---

## 🐳 Running with Docker

### Step 1: Build the JAR

```bash
mvn clean package -DskipTests
```

### Step 2: Run with Docker Compose

```bash
docker-compose up --build
```

This will:

* Start Elasticsearch (on port 9200)
* Build and start your Spring Boot app (on port 8080)
* Wait for Elasticsearch to be healthy before the app starts

### Verify:

```bash
curl http://localhost:8080/api/search
```

---

## 🏃 Running the Application (Locally without Docker)

1. **Ensure Elasticsearch is running** at `localhost:9200`

2. **Disable Elasticsearch security** if needed by editing `elasticsearch.yml`:

   ```yaml
   xpack.security.enabled: false
   xpack.security.transport.ssl.enabled: false
   ```

3. **Run the app**

   ```bash
   mvn spring-boot:run
   ```

The app starts at `http://localhost:8080` and automatically indexes data.

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

## 🐞 Debugging & Troubleshooting

| Issue                               | Solution                                                                            |
| ----------------------------------- | ----------------------------------------------------------------------------------- |
| **ES connection refused**           | Check that `spring.elasticsearch.uris` uses `http://elasticsearch:9200` in Docker   |
| **Data not ingesting**              | Confirm `sample-courses.json` is in `src/main/resources/`                           |
| **App restarts or fails in Docker** | Use `depends_on` and `healthcheck` in `docker-compose.yml`                          |
| **Maven build fails**               | Check JDK and Maven versions; use `mvn clean install -DskipTests`                   |
| **Index not found**                 | Check logs for ingestion issues and run `GET /api/search` to trigger index creation |

---

## 🙋 Author

*Prakhar Shukla* – B.Tech in Electronics & Communication Engineering (2023–27)

> Feel free to reach out for questions or improvements!
