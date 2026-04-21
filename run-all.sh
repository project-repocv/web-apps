#!/bin/bash

echo "Starting Banking Platform..."

# Start all services with Docker Compose
docker-compose up --build -d

echo "Waiting for services to start..."
sleep 30

echo "Services started successfully!"
echo ""
echo "Access points:"
echo "- API Gateway: http://localhost:8080"
echo "- Eureka Dashboard: http://localhost:8761"
echo "- Grafana: http://localhost:3000 (admin/admin)"
echo "- RabbitMQ: http://localhost:15672 (guest/guest)"
echo "- Prometheus: http://localhost:9090"
echo ""
echo "Check service status with: docker-compose ps"
