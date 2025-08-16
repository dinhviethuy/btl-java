Build & Run

Prerequisites

- JDK 17+
- Gradle or use the Gradle Wrapper (recommended)
- MySQL running (update credentials in src/main/resources/application.yml)

Commands

```bash
./gradlew bootRun
```

Database initialization (MySQL)

Use the SQL scripts in `sql/` to initialize a clean schema matching the current JPA mappings (UUID string primary keys with column name `id`).

1. Create database:

```bash
mysql -u root -p < sql/00-create-database.sql
```

2. Create schema (tables, indexes, FKs):

```bash
mysql -u root -p job_finder < sql/01-schema.sql
```

3. Seed initial data:

```bash
mysql -u root -p job_finder < sql/02-data.sql
```

4. Seed super admin + full permissions:

```bash
mysql -u root -p job_finder < sql/03-fake-data-from-json.sql
```

Ensure your `application.yml` points to the same database:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/job_finder?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update
```

Note: If you already have an old schema with numeric ids or mismatched foreign keys, drop it and re-create using the scripts above.

Swagger UI: /swagger-ui/index.html
