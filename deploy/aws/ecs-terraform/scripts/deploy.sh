#!/bin/bash
#
# Copyright 2018-2025 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
set -e

# Configuration
AWS_REGION=${AWS_REGION:-"us-east-1"}
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_API_REPO="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/marquez-api"
ECR_WEB_REPO="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/marquez-web"
ENVIRONMENT=${ENVIRONMENT:-"production"}
IMAGE_TAG=${IMAGE_TAG:-"latest"}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to build and push Docker images
build_and_push() {
    local service=$1
    local dockerfile=$2
    local ecr_repo=$3
    
    echo_info "Building ${service} Docker image..."
    
    if [ "$service" = "api" ]; then
        docker build -f ${dockerfile} -t ${service}:${IMAGE_TAG} .
    else
        cd web && docker build -f ${dockerfile} -t ${service}:${IMAGE_TAG} . && cd ..
    fi
    
    echo_info "Tagging ${service} image for ECR..."
    docker tag ${service}:${IMAGE_TAG} ${ecr_repo}:${IMAGE_TAG}
    
    echo_info "Pushing ${service} image to ECR..."
    docker push ${ecr_repo}:${IMAGE_TAG}
    
    echo_info "${service} image pushed successfully!"
}

# Function to update ECS service
update_ecs_service() {
    local service_name=$1
    local cluster_name="marquez-${ENVIRONMENT}"
    
    echo_info "Updating ECS service ${service_name}..."
    
    aws ecs update-service \
        --cluster ${cluster_name} \
        --service ${service_name} \
        --force-new-deployment \
        --region ${AWS_REGION} \
        > /dev/null
    
    echo_info "Waiting for service ${service_name} to stabilize..."
    aws ecs wait services-stable \
        --cluster ${cluster_name} \
        --services ${service_name} \
        --region ${AWS_REGION}
    
    echo_info "Service ${service_name} updated successfully!"
}

# Main deployment flow
main() {
    echo_info "Starting Marquez ECS deployment..."
    echo_info "Environment: ${ENVIRONMENT}"
    echo_info "AWS Region: ${AWS_REGION}"
    echo_info "Image Tag: ${IMAGE_TAG}"
    
    # Login to ECR
    echo_info "Logging in to ECR..."
    aws ecr get-login-password --region ${AWS_REGION} | \
        docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
    
    # Build and push API image
    build_and_push "api" "Dockerfile" "${ECR_API_REPO}"
    
    # Build and push Web image
    build_and_push "web" "Dockerfile" "${ECR_WEB_REPO}"
    
    # Update ECS services
    update_ecs_service "marquez-api"
    update_ecs_service "marquez-web"
    
    echo_info "Deployment completed successfully!"
    
    # Get ALB DNS name
    ALB_DNS=$(aws elbv2 describe-load-balancers \
        --names "marquez-${ENVIRONMENT}" \
        --query "LoadBalancers[0].DNSName" \
        --output text \
        --region ${AWS_REGION})
    
    echo_info "Application is available at: http://${ALB_DNS}"
    echo_info "API endpoint: http://${ALB_DNS}/api"
    echo_info "Web UI: http://${ALB_DNS}"
}

# Check required tools
check_requirements() {
    local requirements=("docker" "aws" "jq")
    
    for cmd in "${requirements[@]}"; do
        if ! command -v ${cmd} &> /dev/null; then
            echo_error "${cmd} is not installed. Please install it first."
            exit 1
        fi
    done
    
    # Check AWS credentials
    if ! aws sts get-caller-identity &> /dev/null; then
        echo_error "AWS credentials are not configured. Please configure them first."
        exit 1
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --environment|-e)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --region|-r)
            AWS_REGION="$2"
            shift 2
            ;;
        --tag|-t)
            IMAGE_TAG="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  -e, --environment  Environment name (default: production)"
            echo "  -r, --region       AWS region (default: us-east-1)"
            echo "  -t, --tag          Docker image tag (default: latest)"
            echo "  -h, --help         Show this help message"
            exit 0
            ;;
        *)
            echo_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Run deployment
check_requirements
main