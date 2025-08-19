DC = docker-compose -f docker-compose.yml

up:
	@echo "============= starting db local docker ============="
	$(DC) up -d 


docker/database/down:
	$(DC) down

logs:
	$(DC) logs -f db

ps:
	$(DC) ps

migrate:
	docker exec -i apartment_db psql -U admin -d apartment < migrations/init.sql

db-reset:
	$(DC) down
	rm -rf ./db
	$(DC) up -d
	# wait until Postgres is ready
	docker exec apartment_db sh -c "until pg_isready -U admin; do sleep 1; done"
	# run init.sql manually in case you want to re-run migrations
	docker exec -i apartment_db psql -U admin -d apartment < migrations/init.sql
