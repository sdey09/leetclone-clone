COMPOSE          ?= docker compose
EXECUTORS_COMPOSE = $(COMPOSE) -f code-running-languages/docker-compose.yaml

.DEFAULT_GOAL := help
.PHONY: help setup db-up db-down db-reset db-psql executors-up executors-down executors-logs \
        build test run-publisher run-executor frontend-install frontend-dev ps

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-18s %s\n", $$1, $$2}'

setup: db-up executors-up frontend-install ## One-time setup: Postgres, executor containers, frontend deps
	@echo "Setup done. Start the API with: make run-publisher"

# --- Postgres -------------------------------------------------------------
db-up: ## Start Postgres and wait until it is healthy
	$(COMPOSE) up -d --wait postgres

db-down: ## Stop Postgres (data is kept)
	$(COMPOSE) stop postgres

db-reset: ## Delete all Postgres data and start a fresh database
	$(COMPOSE) down -v
	$(MAKE) db-up

db-psql: ## Open a psql shell in the database
	$(COMPOSE) exec postgres psql -U myuser -d leetcode

# --- Language sandbox containers ------------------------------------------
executors-up: ## Build and start the python/java/cpp/javascript executor containers
	$(EXECUTORS_COMPOSE) up -d --build

executors-down: ## Stop and remove the executor containers
	$(EXECUTORS_COMPOSE) down

executors-logs: ## Tail executor container logs
	$(EXECUTORS_COMPOSE) logs -f

# --- Apps -----------------------------------------------------------------
build: ## Build all Gradle modules
	./gradlew build -x test

test: db-up ## Run all tests (needs Postgres)
	./gradlew test

run-publisher: db-up ## Run code-publisher on :8080
	./gradlew :code-publisher:bootRun

run-executor: ## Run code-executor on :8082
	./gradlew :code-executor:bootRun

frontend-install: ## Install frontend dependencies
	cd frontend && npm install

frontend-dev: ## Run the Vite dev server
	cd frontend && npm run dev

ps: ## Show running containers for this repo
	$(COMPOSE) ps
	$(EXECUTORS_COMPOSE) ps
