# NovelHub

NovelHub is an educational full-stack platform for publishing novels and selling individual chapters with a mock payment provider.

## Project overview

### For users

- Browse novels and chapter summaries without signing in.
- Register and sign in with email/password authentication.
- Read free chapters after signing in.
- Buy paid chapters through the mock payment service.
- Read paid chapters after a successful purchase.
- View successfully purchased chapters in **My Library**.

### For administrators

- Create, edit, and delete novels.
- Create, edit, and delete chapters.
- Preview all chapters, including paid chapters.
- View all users and purchase records.

Public registration always creates a `USER`; the `ADMIN` role is created by the backend startup initializer.

## Tech stack

- Backend: Java, Spring Boot 3.5, Spring MVC, Spring Security, JWT, Spring Data JPA/Hibernate, PostgreSQL, Maven Wrapper.
- Frontend: React 19, Vite, Tailwind CSS, Axios, React Router.
- Payments: an application-provided mock payment service that always succeeds.

The Maven project currently targets Java 17 (`java.version` in `backend/pom.xml`); use JDK 21 as the project prerequisite described below.

## Project structure

```text
novelhub/
├── backend/                         Spring Boot REST API and persistence layer
│   ├── src/main/java/com/novelhub/  Controllers, services, entities, repositories, security, and DTOs
│   ├── src/main/resources/          Backend configuration
│   ├── .mvn/                        Maven Wrapper support
│   ├── pom.xml                      Maven dependencies and build configuration
│   └── docker-compose.yml           Optional local PostgreSQL container
├── frontend/                        React/Vite browser application
│   ├── src/                         Routes, pages, components, API client, and auth context
│   ├── package.json                 Frontend dependencies and scripts
│   └── .env.example                 Frontend API URL example
├── .env.example                     Backend and frontend environment variable examples
└── .gitignore                       Local, generated, and credential-file exclusions
```

## Database design

Hibernate creates or updates the schema with `ddl-auto: update`.

- `User` maps to `users`. A user has a `Role` (`USER` or `ADMIN`) and can have many `Purchase` records.
- `Novel` maps to `novels`. A novel has many `Chapter` records; deleting a novel cascades to its chapters.
- `Chapter` maps to `chapters`. Each chapter belongs to one novel and has a title, content, number, and price. A zero price means free.
- `Purchase` maps to `purchases`. It links one user to one chapter and stores the purchase price, status, payment reference, and timestamp.
- `PurchaseStatus` records whether a mock payment is `SUCCESS` or `FAILED`.

The `purchases` table is the join entity between users and chapters, so it can store purchase-specific data.

## API endpoints

All paths include the `/api` prefix. `ADMIN` means an authenticated user with the `ADMIN` role.

| Method | Path | Access |
|---|---|---|
| POST | `/auth/register` | Public |
| POST | `/auth/login` | Public |
| GET | `/auth/me` | Authenticated user |
| GET | `/novels` | Public |
| GET | `/novels/{id}` | Public; unlock status is personalized when authenticated |
| GET | `/chapters/{id}` | Authenticated user |
| POST | `/purchases/{chapterId}` | Authenticated user |
| GET | `/purchases/my` | Authenticated user |
| POST | `/admin/novels` | ADMIN |
| PUT | `/admin/novels/{id}` | ADMIN |
| DELETE | `/admin/novels/{id}` | ADMIN |
| GET | `/admin/novels/{novelId}/chapters` | ADMIN |
| POST | `/admin/novels/{novelId}/chapters` | ADMIN |
| PUT | `/admin/chapters/{id}` | ADMIN |
| DELETE | `/admin/chapters/{id}` | ADMIN |
| GET | `/admin/users` | ADMIN |
| GET | `/admin/purchases` | ADMIN |

## How chapter protection works

The frontend does not protect chapter content. A request to `GET /api/chapters/{id}` is authenticated by the backend JWT filter. The backend then:

1. Allows a free chapter.
2. Allows an administrator to preview any chapter.
3. For a paid chapter, checks for a `Purchase` with `SUCCESS` for the authenticated user and chapter.
4. Returns the chapter content only after that check succeeds; otherwise it returns a locked/forbidden error without putting the content in the response.

## Prerequisites

- JDK 21 (the current Maven configuration targets Java 17)
- PostgreSQL 16 or another compatible PostgreSQL installation
- Node.js and npm

## Setup and running

### 1. Create the database

Create a PostgreSQL database named `novelhub`, or start the optional local container:

```bash
cd backend
docker compose up -d
```

If you use an existing PostgreSQL server, create the database and set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for the backend.

### 2. Configure environment variables

Copy `.env.example` to a private environment file or configure the variables in your shell/IDE. Never commit the private file. The frontend reads `VITE_API_URL` from `frontend/.env`; `frontend/.env.example` contains its placeholder URL.

### 3. Run the backend

From `backend/`:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on `http://localhost:8080` by default.

### 4. Run the frontend

From `frontend/`:

```bash
npm install
npm run dev
```

Open the Vite URL shown in the terminal, normally `http://localhost:5173`.

## How the first ADMIN user is created

On startup, `DataInitializer` checks whether `ADMIN_EMAIL` already exists. If it does not, it creates one `ADMIN` user using `ADMIN_NAME`, `ADMIN_EMAIL`, and `ADMIN_PASSWORD`, storing the password as a BCrypt hash. Set these variables to your own values before the first startup. Later startups do not replace an existing user.

When `SEED_DEMO_DATA=true` and there are no novels, startup also inserts one demo novel with one free and two paid chapters.

## Environment variables

| Name | Purpose | Example value |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/novelhub` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `<your_password>` |
| `JWT_SECRET` | JWT signing secret; use at least 32 bytes | `<at_least_32_character_random_secret>` |
| `ADMIN_NAME` | Initial administrator display name | `Admin` |
| `ADMIN_EMAIL` | Initial administrator email | `admin@novelhub.local` |
| `ADMIN_PASSWORD` | Initial administrator password | `<your_admin_password>` |
| `SEED_DEMO_DATA` | Enable demo novel creation when the novels table is empty | `true` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins allowed by the API | `http://localhost:5173` |
| `VITE_API_URL` | Frontend Axios base URL | `http://localhost:8080/api` |
