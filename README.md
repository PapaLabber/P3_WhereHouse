# WhereHouse

WhereHouse is a study project for Novo Nordisk that allocates pre-production ingredients across warehouses based on forecast and capacity data from Excel files.

## Overview

The application lets a user:

1. Select a country and year.
2. Upload an `.xlsx` input file.
3. Send the file to the backend allocation engine.
4. Download a generated Excel result file.
5. Review a warehouse utilisation dashboard based on the latest processed run.

## Tech Stack

- Frontend: React, TypeScript, Vite
- Backend: Java, Spring Boot, Apache POI, OR-Tools
- Testing: Vitest, Playwright
- Containerisation: Docker Compose

## Repository Structure

```text
.
├── backend/     Spring Boot API and allocation logic
├── frontend/    React application
├── e2e/         Playwright end-to-end tests
├── outputFile/  Generated Excel output files
└── compose.yaml Local multi-container setup
```

## Running the Project

### Recommended: Docker Compose

From the repository root:

```bash
docker compose up --build
```

Services:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`

Generated Excel files are written to `./outputFile` on the host machine.

To stop the services:

```bash
docker compose down
```

## Running Tests

### Frontend unit tests

```bash
cd frontend
npm install
npm test
```

### End-to-end tests

```bash
cd e2e
npm install
npm run e2e
```

This starts the Docker services, waits for the frontend to become available, runs the Playwright suite, and shuts everything down again.

## API Endpoints

The backend currently exposes two main endpoints under `/api`:

- `POST /api/export`
  Accepts an `.xlsx` file together with `wantedCountry` and `wantedYear`, runs the allocation flow, and returns a generated Excel file.
- `GET /api/dashboard`
  Returns warehouse dashboard data for the latest successful allocation run.

## Notes

- The main workflow depends on Excel input files in `.xlsx` format.
- The dashboard is populated only after a successful export run.
- Sample generated output can be found in `outputFile/`.
- Additional Docker notes are available in `README.Docker.md`.
