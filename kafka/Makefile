build:
	mvn clean install -B -U
start-kafka:
	docker-compose up -d
	echo "wait min. 30 sec for kafka-ui to start, than you can open it on https://localhost:18080"
check-kafka:
	nc -z localhost 2181
	nc -z localhost 9092
	nc -z localhost 18080
	docker-compose logs kafka | grep -i 'started'
stop-kafka:
	docker-compose down --remove-orphans
restart-kafka:
	make stop-kafka
	make start-kafka