# Spring Boot Postcode Distance Calculator Assessment

## Overview

This is a sample Spring Boot application demonstrating the use of Spring Web to host postcode and calculate distance
between postal codes. It is designed using clean architectural patterns and emphasizes testability and maintainability.

---

## 1. Setup

### Requirements

- Java 17+
- Maven 3.6+
- Spring Boot
    - Spring Web
    - Spring Batch
    - Spring Security
    - Spring Data JPA
    - Spring Boot Test
- H2 (file-based database)
- Others: Lombok, MapStruct

- ukpostcodes-short.csv is provided for a short list of postcode for quick startup and test runs
- for complete list of postcodes, download ukpostcodes.csv
  from <https://www.freemaptools.com/download-uk-postcode-lat-lng.htm>

---

### 1a. Steps to Run

#### Option 1: Using JAR (recommended for deployment)

1. Build the JAR file:

   ```bash
   mvn clean package

2. Start the application (Running with the complete list of ukpostcodes will take 1 min to startup, use
   ukpostcodes-short.csv instead for quick test runs):

    ```bash
    cd ./target
    java -jar ./postcode-0.0.1-SNAPSHOT.jar --csvPath=./ukpostcodes.csv

#### Option 2: Using IDE or CLI for Development

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--csvPath=ukpostcodes-short.csv
```

## 2. Database Structure

- Database: File-based H2 database.

- Initialization:
    - On every application startup (via either method), a default batch job is executed to load data
      from `param:csv_path`.
    - The data in the H2 database is cleared on each restart to ensure a consistent initial state.

## 3. Design Patterns & Architecture

### 3a. N-Tier Architecture

#### The project separates concerns into:

1. Controller Layer (HTTP handling)
1. Service Layer (business logic)
1. Repository Layer (data access)
1. Configuration Layer (job and security configurations)

### 3b. Domain-Driven Design (DDD)

#### Core domain logic is encapsulated within the domain layer.

- Data transfer between layers is done via DTOs, with MapStruct for mapping.

## 4. APIs

### 4.1 REST APIs

#### 4.1.1 GET /api/v1/postcodes/uk/{postcode}

- Authentication: Basic Auth (admin:admin)
- API Address: GET /api/v1/postcodes/uk/{postcode}
- Parameters:
    - postcode (path variable): postcode to get

- Response Object:

```json
{
  "id": 3,
  "postcode": "AB10 7JB",
  "latitude": 57.12,
  "longitude": -2.13
}
```

- Error Response:
    - 404 Not Found
  ```json
    {
    "error": "Entity Not Found"
    }
    ```

- Sample curl command

```bash
#GET /api/v1/postcodes/uk/{postcode}, postcode string must be URL-encoded
curl -u admin:admin -X GET "http://localhost:8080/api/v1/postcodes/uk/AB11%208RQ"
```

### 4.1.2 PATCH /api/v1/postcodes/uk/{postcode}/coordinates

- Authentication: Basic Auth (admin:admin)
- API Address: PATCH /api/v1/postcodes/uk/{postcode}/coordinates
- Parameters:
    - postcode (path variable): postcode to update
    - Request Object:

```json
{
  "latitude": 58.0,
  "longitude": 1.0
}
```

- Response Object:

```json 
{
  "id": 3,
  "postcode": "AB11 8RQ",
  "latitude": 58.0,
  "longitude": 1.0
}
```

- Error Responses:
    - 404 Not Found

```json
{
  "error": "Entity Not Found"
}
```

- 400 Bad Request

```json
  {
  "error": "Latitude or longitude is out of valid UK bounds. Expected lat between 49.9 and 60.9, lon between -8.2 and 1.8."
}
```

- Sample curl command

  ```bash
  # PATCH /api/v1/transactions/{id}/description
  curl -u admin:admin -X PATCH "http://localhost:8080/api/v1/postcodes/uk/AB10%207JB/coordinates" \
  -H "Content-Type: application/json" \
  -d '{"latitude": 100.0, "longitude": 100.0}'
  ```

### 4.1.3 GET /api/v1/postcodes/uk/distance

- Authentication: Basic Auth (admin:admin)
- API Address: GET /api/v1/postcodes/uk/distance
- Parameters:
    - postcode1
    - postcode2
- Response Object:

```json
{
  "postcode1": {
    "id": 21,
    "postcode": "AB21 0TF",
    "latitude": 57.22,
    "longitude": -2.27
  },
  "postcode2": {
    "id": 22,
    "postcode": "AB21 7LD",
    "latitude": 57.21,
    "longitude": -2.19
  },
  "distance": {
    "unit": "km",
    "value": 4.94353671586168275098316371440887451171875
  }
}
```

- Error Responses:
    - 404 Not Found

```json
{
  "error": "Entity Not Found"
}
```

- Sample curl command

  ```bash
  # GET /api/v1/postcodes/uk/distance
  curl -u admin:admin -X GET "http://localhost:8080/api/v1/postcodes/uk/distance?postcode_1=AB21%200TF&postcode_2=AB21%207LD"
  ```

## 5. File Reference

- [ukpostcodes-short.csv](./src/main/resources/ukpostcodes-short.csv): The sample short data file used to test the
  initialization the H2 database
  on application
  startup.
- [application.yml](./src/main/resources/application.yml): Configures the file-based H2 database and Spring Batch
  settings.

