# Marquez Deployment Options

This directory contains deployment configurations for Marquez across different platforms and cloud providers.

## Available Deployments

### AWS

#### ECS with Terraform (`aws/ecs-terraform/`)
Complete infrastructure-as-code deployment using:
- **Amazon ECS Fargate** for serverless container orchestration
- **RDS PostgreSQL** for managed database
- **Application Load Balancer** for traffic distribution
- **CloudFront** for HTTPS and global content delivery
- **Terraform** for infrastructure provisioning

See [aws/ecs-terraform/README.md](aws/ecs-terraform/README.md) for detailed instructions.