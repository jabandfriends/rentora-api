DC = docker-compose -f docker-compose.yml

api/build/up:
	@echo "============= starting and building image (api + db) ============="
	$(DC) up -d --build

api/up:
	@echo "============= starting backend (api + db) ============="
	$(DC) up -d 

db/up:
	@echo "============= starting db local docker ============="
	$(DC) up database -d

docker/down:
	$(DC) down

ps:
	$(DC) ps
