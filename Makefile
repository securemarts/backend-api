# Securemarts API – common commands for devs
# Run `make` or `make help` to list targets.

.PHONY: help start stop up down build logs run run-local db clean test

# Default target: show help
help:
	@echo "Securemarts API – make targets"
	@echo ""
	@echo "  make start     Start app + Postgres (Docker, detached)"
	@echo "  make up        Same as start"
	@echo "  make stop      Stop all containers"
	@echo "  make down      Same as stop"
	@echo "  make build     Build and start (docker compose up --build -d)"
	@echo "  make logs      Follow app + postgres logs"
	@echo "  make run       Run app locally with Maven (needs Postgres on localhost:5432)"
	@echo "  make run-local Start Postgres in Docker, then run app locally (DB on port 5433)"
	@echo "  make db        Start Postgres only in Docker (published on host port 5433)"
	@echo "  make test      Run tests (Maven)"
	@echo "  make clean     Stop containers and run Maven clean"
	@echo ""

# Start full stack (app + Postgres) in background
start up:
	docker compose up -d

# Build images and start
build:
	docker compose up --build -d

# Stop all services
stop down:
	docker compose down

# Follow logs
logs:
	docker compose logs -f

# Start Postgres only (host port 5433)
db:
	docker compose up -d postgres

# Run app locally with Maven (DB must be on localhost:5432, e.g. system Postgres)
run:
	./mvnw spring-boot:run

# Start Postgres in Docker (port 5433) then run app locally
run-local: db
	@echo "Waiting for Postgres to be ready..."
	@sleep 3
	SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/securemarts ./mvnw spring-boot:run

# Run tests
test:
	./mvnw test

# Stop containers and clean Maven build
clean:
	docker compose down
	./mvnw clean
