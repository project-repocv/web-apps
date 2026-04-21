#!/bin/bash

echo "Building all microservices..."

# Build Eureka Server
echo "Building Eureka Server..."
cd eureka-server
mvn clean package -DskipTests
cd ..

# Build Customer Service
echo "Building Customer Service..."
cd customer-service
mvn clean package -DskipTests
cd ..

# Build Account Service
echo "Building Account Service..."
cd account-service
mvn clean package -DskipTests
cd ..

# Build Transaction Service
echo "Building Transaction Service..."
cd transaction-service
mvn clean package -DskipTests
cd ..

# Build Auth Service
echo "Building Auth Service..."
cd auth-service
mvn clean package -DskipTests
cd ..

# Build Notification Service
echo "Building Notification Service..."
cd notification-service
mvn clean package -DskipTests
cd ..

# Build API Gateway
echo "Building API Gateway..."
cd api-gateway
mvn clean package -DskipTests
cd ..

echo "All services built successfully!"
echo "Run 'docker-compose up --build' to start the application."
