#!/bin/bash
#
# Copyright 2018-2025 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
set -e

# Build script for local testing and CI/CD pipelines

# Configuration
IMAGE_TAG=${IMAGE_TAG:-"latest"}
REGISTRY=${REGISTRY:-""}

# Colors for output
GREEN='\033[0;32m'
NC='\033[0m'

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

# Build API image
build_api() {
    echo_info "Building Marquez API image..."
    docker build -f Dockerfile -t marquez-api:${IMAGE_TAG} .
    
    if [ ! -z "${REGISTRY}" ]; then
        docker tag marquez-api:${IMAGE_TAG} ${REGISTRY}/marquez-api:${IMAGE_TAG}
        echo_info "Tagged as ${REGISTRY}/marquez-api:${IMAGE_TAG}"
    fi
}

# Build Web image
build_web() {
    echo_info "Building Marquez Web image..."
    cd web
    docker build -f Dockerfile -t marquez-web:${IMAGE_TAG} .
    cd ..
    
    if [ ! -z "${REGISTRY}" ]; then
        docker tag marquez-web:${IMAGE_TAG} ${REGISTRY}/marquez-web:${IMAGE_TAG}
        echo_info "Tagged as ${REGISTRY}/marquez-web:${IMAGE_TAG}"
    fi
}

# Main
main() {
    echo_info "Starting build process..."
    echo_info "Image tag: ${IMAGE_TAG}"
    
    # Navigate to project root
    cd $(dirname "$0")/../..
    
    # Build images
    build_api
    build_web
    
    echo_info "Build completed successfully!"
    echo_info "Images built:"
    echo "  - marquez-api:${IMAGE_TAG}"
    echo "  - marquez-web:${IMAGE_TAG}"
}

main