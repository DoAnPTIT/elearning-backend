#!/bin/bash

# ============================================
# Production Deployment Script
# ============================================
# This script helps deploy the application to production environment

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if .env.prod exists
if [ ! -f ".env.prod" ]; then
    print_error ".env.prod file not found!"
    print_info "Please copy .env.prod.example to .env.prod and fill in your values:"
    echo "  cp .env.prod.example .env.prod"
    echo "  nano .env.prod  # or use your preferred editor"
    exit 1
fi

print_info "Starting production deployment..."

# Load environment variables
export $(cat .env.prod | grep -v '^#' | xargs)

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    print_error "Docker Compose is not installed. Please install it and try again."
    exit 1
fi

# Use docker compose (newer) or docker-compose (older)
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

print_info "Using: $DOCKER_COMPOSE"

# Stop existing containers
print_info "Stopping existing containers..."
$DOCKER_COMPOSE -f docker-compose.prod.yml down

# Build and start services
print_info "Building and starting services..."
$DOCKER_COMPOSE -f docker-compose.prod.yml up -d --build

# Wait for services to be healthy
print_info "Waiting for services to be healthy..."
sleep 10

# Check service health
print_info "Checking service health..."
if docker ps | grep -q "elearning-backend-prod"; then
    print_info "Backend service is running"
else
    print_error "Backend service failed to start. Check logs with:"
    echo "  $DOCKER_COMPOSE -f docker-compose.prod.yml logs app"
    exit 1
fi

# Show running containers
print_info "Running containers:"
docker ps --filter "name=elearning" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

print_info ""
print_info "============================================"
print_info "Deployment completed successfully!"
print_info "============================================"
print_info ""
print_info "Useful commands:"
print_info "  View logs:        $DOCKER_COMPOSE -f docker-compose.prod.yml logs -f app"
print_info "  Stop services:    $DOCKER_COMPOSE -f docker-compose.prod.yml down"
print_info "  Restart services: $DOCKER_COMPOSE -f docker-compose.prod.yml restart"
print_info "  View all logs:    $DOCKER_COMPOSE -f docker-compose.prod.yml logs"
print_info ""

