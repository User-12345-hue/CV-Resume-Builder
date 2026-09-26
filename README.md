# CV & Resume Builder

A browser-based resume builder inspired by the workflows and layouts in [Project_Report.pdf](./Project_Report.pdf). Create an account, keep multiple resumes, choose a design, add your experience and optional photo, and print a copy or save it as a PDF from your browser.

The original project is a Java Swing application backed by a local MySQL database. This repository now also contains a responsive web application designed for remote access. The new application stores accounts and resumes in MySQL and keeps each resume private to its owner.

## Features

- Responsive sign-up and sign-in with BCrypt-hashed passwords.
- Create, edit, preview, and delete multiple resumes.
- Personal details, profile summary, work experience, education, skills, projects and awards, certifications, and additional activities.
- Modern, classic, and minimal resume templates.
- Optional private profile photo upload (PNG, JPEG, or GIF; maximum 5 MB).
- Print-friendly A4 layout; use **Print / Save PDF** and choose **Save as PDF** in the browser dialog.
- MySQL persistence, database migrations, and a Docker Compose deployment.

## Run with Docker Compose

Requirements: Docker Engine and Docker Compose v2.

1. Copy `.env.example` to `.env`.
2. Replace `DB_PASSWORD` and `DB_ROOT_PASSWORD` with different, randomly generated secrets. Do not commit `.env`.
3. Start the app:

   ```powershell
   docker compose up --build -d
   ```

4. Open [http://localhost:8080](http://localhost:8080) and create an account.
5. Follow logs with `docker compose logs -f app`; stop the services with `docker compose down`. Resume data remains in the named `cvbuilder_data` volume.

To choose another local port, set `APP_PORT` in `.env`. The database is only available on the internal Compose network; it is not published to the internet.

## Run from source

Requirements: JDK 21 or later, Maven 3.9+, and a MySQL 8.x database. Create an empty database named `cv_resume_builder`, then configure:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/cv_resume_builder?serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=cvbuilder
SPRING_DATASOURCE_PASSWORD=<your database password>
```

Build and test:

```powershell
mvn clean verify
```

Run:

```powershell
mvn spring-boot:run
```

Flyway creates the web application's tables on startup. The legacy dump at `MySQL Database/cv_resume_builder (1).sql` is for the original desktop program and is **not** used or imported by the web app.

## Deploy for remote access

The Compose setup can run on a Docker-capable server. For public access, put an HTTPS reverse proxy or managed ingress in front of the app, point a domain to that host, and set `SESSION_COOKIE_SECURE=true` in the production environment. Keep MySQL private, provide unique secrets through the host's secret store, and configure encrypted database backups and monitoring before storing real user data. Do not expose the application over plain HTTP or publish the MySQL port on a public interface.

The included Compose file is a portable starting point, not a claim that a cloud deployment has already been provisioned. A public deployment also needs a cloud/server account, domain/TLS configuration, production database and credentials.

## Project layout

- `src/main/java/app/cvbuilder/` — Spring Boot web application.
- `src/main/resources/templates/` — server-rendered pages.
- `src/main/resources/static/` — responsive UI and print styles.
- `src/main/resources/db/migration/` — versioned MySQL schema.
- Root `Dockerfile` and `docker-compose.yml` — container build and local/self-hosted deployment.
- `Project_Report.pdf` and the original Java Swing forms remain as documentation/reference for the legacy application.
