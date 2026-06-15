# Document AI Pipeline

Automated document processing pipeline with OCR (Tesseract) and LLM-based data extraction (Groq). Upload PDF invoices — the system extracts text via OCR, then uses an LLM to pull structured fields (invoice number, date, total, vendor, etc.).

## Architecture

[PostgreSQL] <-> [Spring Boot Backend] <-> [RabbitMQ] <-> [Consumer (OCR + LLM)]
                                                                       ^
[React Frontend] ---> [Nginx (proxy /api)] -----------------------------+

## Quick Start

```bash
# Prerequisites: Docker, Docker Compose
# 1. Clone both repos side by side
git clone <backend-repo> document-ai-pipeline-backend
git clone <frontend-repo> document-ai-pipeline-frontend

# 2. Set environment variables (backend/.env)
cd document-ai-pipeline-backend
cp .env.example .env   # edit GROK_API_KEY, postgres credentials

# 3. Start everything
docker compose up -d
```

| Service  | URL                        |
|----------|----------------------------|
| Frontend | http://localhost:5173       |
| Backend  | http://localhost:8081       |
| Swagger  | http://localhost:8081/swagger-ui.html |
| RabbitMQ | http://localhost:15672 (docai/docai) |

## Development (without Docker)

### Backend

```bash
cd document-ai-pipeline-backend
# Requires: Java 21, Maven, Tesseract 5 + English traineddata
./mvnw spring-boot:run
```

### Frontend

```bash
cd document-ai-pipeline-frontend
npm install
npm run dev
```

## API Endpoints

| Method | Path                                   | Description              |
|--------|----------------------------------------|--------------------------|
| GET    | /api/documents                         | List all documents       |
| POST   | /api/documents/upload                  | Upload PDF (multipart)   |
| POST   | /api/documents/generate-sample         | Generate a sample invoice|
| GET    | /api/documents/{id}                    | Get document details     |
| GET    | /api/documents/{id}/download           | Download original PDF    |
| POST   | /api/documents/{id}/retry              | Re-process a failed doc  |
| DELETE | /api/documents/{id}                    | Delete a document        |
| GET    | /api/documents/search?q=               | Full-text search         |

## Environment Variables

| Variable           | Default                    | Description            |
|--------------------|----------------------------|------------------------|
| `GROK_API_KEY`     | —                          | Groq API key (required)|
| `POSTGRES_USER`    | `dev`                      | Database user          |
| `POSTGRES_PASSWORD`| `dev`                      | Database password      |
| `POSTGRES_DB`      | `docai`                    | Database name          |

## Tech Stack

- **Backend:** Spring Boot 4, Java 21, Spring Data JPA, RabbitMQ, Tess4J, Apache PDFBox, Bucket4j, SpringDoc OpenAPI
- **Frontend:** React 19, TypeScript, Vite, Tailwind CSS v4, React Router v7, TanStack Query
- **Infra:** PostgreSQL 16, RabbitMQ, Docker Compose
