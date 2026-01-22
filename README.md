# NeonCare: a jOOQ demo

A demo application demonstrating how to use jOOQ with Java.

## Tech stack

- Spring Boot 4
- Java 25
- Vaadin
- PostgreSQL
- jOOQ OSS
- Docker Compose
- Maven

## How to run the application

Spin up a database instance:

```shell
docker compose up -d
```

Run Flyway migrations to create a schema and seed test data:

```shell
mvn flyway:migrate
```

Run code generation with jOOQ

```shell
mvn jooq-codegen:generate
```

Run the application:

```shell
mvn spring-boot:run
```