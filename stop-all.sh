#!/bin/bash

echo "Stopping Banking Platform..."

# Stop all services
docker-compose down

echo "All services stopped successfully!"
