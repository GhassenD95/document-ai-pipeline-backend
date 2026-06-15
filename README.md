# Document AI Pipeline

Automated invoice processing pipeline: upload a PDF, the system extracts text via **Tesseract OCR**, then uses **Groq (Llama 3.1)** to pull structured fields (invoice number, date, total, vendor).

Built with **Spring Boot 4** + **React 19**, orchestrated with **Docker Compose**, processed asynchronously via **RabbitMQ**.

> Portfolio project demonstrating full-stack development, async messaging, OCR integration, LLM prompting, rate limiting, and Docker deployment.

---

## Architecture

```
[React Frontend] → [Nginx /api proxy] → [Spring Boot API]
                                              ↕ (RabbitMQ)
                                     [Consumer Worker]
                                    ├── Tesseract OCR
                                    └── Groq LLM Extraction
                                              ↕
                                        [PostgreSQL]
```

## Quick Start (Docker)

```bash
git clone https://github.com/GhassenD95/document-ai-pipeline-backend
git clone https://github.com/GhassenD95/document-ai-pipeline-frontend

cd document-ai-pipeline-backend
cp .env.example .env   # add your Groq API key
docker compose up -d
```

| Service   | URL                                    |
|-----------|----------------------------------------|
| Frontend  | http://localhost:5173                   |
| Backend   | http://localhost:8081                   |
| Swagger   | http://localhost:8081/swagger-ui.html   |
| RabbitMQ  | http://localhost:15672 (docai/docai)    |

## Development (without Docker)

### Backend

Requires Java 21, Maven, Tesseract 5 + English traineddata.

```bash
cd document-ai-pipeline-backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd document-ai-pipeline-frontend
npm install
npm run dev
```

## API

| Method | Path                                   | Description              |
|--------|----------------------------------------|--------------------------|
| GET    | /api/documents                         | List all documents       |
| POST   | /api/documents/upload                  | Upload PDF (multipart)   |
| GET    | /api/documents/{id}                    | Get document details     |
| GET    | /api/documents/{id}/download           | Download original PDF    |
| POST   | /api/documents/{id}/retry              | Re-process a failed doc  |
| DELETE | /api/documents/{id}                    | Delete a document        |
| GET    | /api/documents/search?q=               | Full-text search         |
| POST   | /api/documents/generate-sample         | Generate a sample invoice |

## Environment Variables

| Variable           | Default | Description                    |
|--------------------|---------|--------------------------------|
| `GROK_API_KEY`     | —       | Groq API key (get one free at console.groq.com) |
| `POSTGRES_USER`    | `dev`   | Database user                  |
| `POSTGRES_PASSWORD`| `dev`   | Database password              |
| `POSTGRES_DB`      | `docai` | Database name                  |

## Tech Stack

| Layer       | Technologies |
|-------------|-------------|
| Backend     | Spring Boot 4, Java 21, Spring Data JPA, RabbitMQ, Tess4J (OCR), Apache PDFBox, Bucket4j (rate limiting), SpringDoc OpenAPI (Swagger) |
| Frontend    | React 19, TypeScript, Vite, Tailwind CSS v4, React Router v7, TanStack Query |
| Infra       | PostgreSQL 16, RabbitMQ, Docker Compose, Nginx |

## Key Features

- **Asynchronous processing** — uploads return immediately, RabbitMQ queues OCR + LLM work
- **OCR extraction** — Tesseract 5 reads text from PDF invoices
- **LLM field extraction** — Groq (Llama 3.1 8B) parses invoice number, date, total, vendor
- **Full-text search** — PostgreSQL `tsvector` indexes extracted text
- **Rate limiting** — 5 uploads/minute per IP via Bucket4j
- **Sample PDF generator** — Apache PDFBox creates realistic test invoices
- **Retry & delete** — reprocess failed documents or remove them
- **Dockerized** — one command to start the full stack
