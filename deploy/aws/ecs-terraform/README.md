# Marquez ECS Deployment Guide

This guide provides instructions for deploying Marquez on AWS ECS with RDS PostgreSQL.

## Deployment Order

To ensure ECS services start successfully, follow this deployment order:

1. **Create ECR Repositories First** - Use `terraform apply -target` to create only ECR repositories
2. **Push Docker Images** - Build and push images to the created ECR repositories  
3. **Deploy Full Infrastructure** - Run `terraform apply` to create all remaining resources

This approach ensures Docker images are available when ECS services start, preventing `CannotPullContainerError`.

## Architecture Overview

The deployment consists of:
- **ECS Fargate** for running containerized services
- **RDS PostgreSQL** for data persistence
- **Application Load Balancer** for traffic distribution
- **Amazon OpenSearch** (optional) for advanced search capabilities
- **ECR** for container image storage

## Prerequisites

1. AWS CLI configured with appropriate credentials
2. Docker installed locally
3. Terraform >= 1.0 (for infrastructure provisioning)
4. jq (for JSON processing in scripts)

## Directory Structure

```
deploy/aws/ecs-terraform/
├── ecs/
│   ├── task-definition-api.json    # ECS task definition for API
│   ├── task-definition-web.json    # ECS task definition for Web UI
│   ├── service-api.json           # ECS service definition for API
│   └── service-web.json           # ECS service definition for Web UI
├── terraform/
│   ├── main.tf                     # Core ECS, RDS, and ALB resources
│   ├── vpc.tf                      # VPC and networking configuration
│   ├── vpc-endpoints.tf            # VPC endpoints for AWS services
│   ├── cloudfront.tf               # CloudFront CDN configuration
│   ├── variables.tf                # Variable definitions
│   ├── terraform.tfvars.example    # Example configuration
│   └── environments/               # Environment-specific configs
│       ├── sandbox.tfvars
│       └── production.tfvars
├── scripts/
│   ├── deploy.sh                   # Main deployment script
│   └── build-images.sh            # Docker image build script
└── README.md                       # This file
```

## Quick Start

### 1. Create ECR Repositories and Push Docker Images

First, create only the ECR repositories and push Docker images:

```bash
cd deploy/aws/ecs-terraform/terraform

# Initialize Terraform
terraform init

# Create terraform.tfvars with your configuration
cat > terraform.tfvars <<EOF
aws_region = "us-east-1"
environment = "production"
vpc_id = "vpc-xxxxx"
private_subnet_ids = ["subnet-xxxxx", "subnet-yyyyy"]
public_subnet_ids = ["subnet-zzzzz", "subnet-wwwww"]
db_password = "your-secure-password"
opensearch_enabled = false
EOF

# Apply ONLY ECR resources first
terraform apply -target=aws_ecr_repository.marquez_api -target=aws_ecr_repository.marquez_web

# Get ECR repository URLs
API_REPO=$(terraform output -raw ecr_repository_api)
WEB_REPO=$(terraform output -raw ecr_repository_web)

# Navigate to Marquez root directory
cd ../../../

# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${API_REPO%/*}

# Build and push API image
docker build --platform linux/amd64 -t marquez-api .
docker tag marquez-api:latest ${API_REPO}:latest
docker push ${API_REPO}:latest

# Build and push Web image
cd web
docker build --platform linux/amd64 -t marquez-web .
docker tag marquez-web:latest ${WEB_REPO}:latest
docker push ${WEB_REPO}:latest
```

### 2. Deploy Complete Infrastructure

Now deploy the rest of the infrastructure with images already available in ECR:

```bash
cd deploy/aws/ecs-terraform/terraform

# Apply all remaining infrastructure
terraform apply
```

With this approach, ECS services will start successfully on first deployment since images are already available in ECR.

### 3. Verify Deployment

```bash
# Make scripts executable
chmod +x deploy/scripts/*.sh

# Build and deploy
./deploy/aws/ecs-terraform/scripts/deploy.sh --environment production --region us-east-1
```

## Detailed Setup

### Step 1: Create ECR Repositories

First, create only the ECR repositories using Terraform's `-target` flag:

```bash
cd deploy/aws/ecs-terraform/terraform
terraform init
terraform apply -target=aws_ecr_repository.marquez_api -target=aws_ecr_repository.marquez_web
```

### Step 2: Build and Push Docker Images

With ECR repositories created, build and push Docker images:

```bash
# Get ECR repository URLs
API_REPO=$(terraform output -raw ecr_repository_api)
WEB_REPO=$(terraform output -raw ecr_repository_web)
REGION=$(terraform output -raw region)

# Login to ECR
aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${API_REPO%/*}

# Build and push images (from Marquez root directory)
docker build --platform linux/amd64 -t marquez-api .
docker tag marquez-api:latest ${API_REPO}:latest
docker push ${API_REPO}:latest

cd web
docker build --platform linux/amd64 -t marquez-web .
docker tag marquez-web:latest ${WEB_REPO}:latest
docker push ${WEB_REPO}:latest
```

**Note**: Use `--platform linux/amd64` to ensure compatibility with ECS Fargate.

### Step 3: Deploy Complete Infrastructure

Now deploy all remaining AWS resources:

```bash
cd deploy/aws/ecs-terraform/terraform
terraform apply
```

This creates:
- ECS Cluster and Services
- RDS PostgreSQL instance
- Application Load Balancer
- Security Groups
- IAM Roles and Policies
- CloudWatch Log Groups
- Auto Scaling configuration

The ECS services will start successfully since Docker images are already available in ECR.

### Step 4: Verify Deployment

```bash
# Deploy with default settings
./deploy/aws/ecs-terraform/scripts/deploy.sh

# Deploy with custom settings
./deploy/aws/ecs-terraform/scripts/deploy.sh \
  --environment staging \
  --region us-west-2 \
  --tag v1.0.0
```

## Configuration

### Environment Variables

#### API Service
- `MARQUEZ_PORT`: API port (default: 5000)
- `MARQUEZ_ADMIN_PORT`: Admin port (default: 5001)
- `POSTGRES_HOST`: RDS endpoint
- `POSTGRES_PORT`: Database port (default: 5432)
- `POSTGRES_DB`: Database name (default: marquez)
- `POSTGRES_USER`: Database user (default: marquez)
- `POSTGRES_PASSWORD`: Database password (stored in AWS Secrets Manager)
- `MIGRATE_ON_STARTUP`: Run database migrations (default: true)
- `SEARCH_ENABLED`: Enable OpenSearch integration (default: false)

#### Web Service
- `MARQUEZ_HOST`: API endpoint URL
- `MARQUEZ_PORT`: API port
- `REACT_APP_ADVANCED_SEARCH`: Enable advanced search features

### Database Configuration

RDS PostgreSQL is configured with:
- Engine: PostgreSQL 14
- Instance class: db.t3.medium (adjustable)
- Storage: 100GB with auto-scaling up to 1TB
- Automated backups: 30 days retention
- Multi-AZ: Configurable for production

### Auto Scaling

Both API and Web services are configured with:
- Minimum instances: 2
- Maximum instances: 10
- Target CPU utilization: 70%

## Security Considerations

1. **Secrets Management**: Database passwords and sensitive data are stored in AWS Secrets Manager
2. **Network Isolation**: ECS tasks run in private subnets
3. **Security Groups**: Restrictive security group rules for each component
4. **Encryption**: RDS storage encryption enabled by default
5. **HTTPS**: Configure ACM certificate for HTTPS support (see Terraform comments)

## Monitoring

### CloudWatch Logs
- API logs: `/ecs/marquez-api`
- Web logs: `/ecs/marquez-web`

### Health Checks
- API: `http://<api-host>:5001/healthcheck`
- Web: `http://<web-host>:3000/`

## Troubleshooting

### Common Issues

1. **ECS Tasks Fail with "CannotPullContainerError"**
   - ECR repositories are created by Terraform but images must be pushed manually
   - Follow Step 2 to build and push Docker images to ECR
   - Ensure you're using `--platform linux/amd64` when building images
   - Verify ECR login is successful before pushing

2. **Database Connection Failed**
   - Check RDS security group allows traffic from ECS tasks
   - Verify database credentials in Secrets Manager
   - Ensure RDS instance is running

3. **ECS Service Won't Start**
   - Check CloudWatch logs for error messages
   - Verify ECR images exist and are accessible
   - Check task definition memory/CPU allocation
   - Ensure images are built for the correct platform (linux/amd64)

4. **ALB Health Check Failing**
   - Verify security group rules
   - Check application startup time
   - Review health check configuration

### Useful Commands

```bash
# View ECS service status
aws ecs describe-services \
  --cluster marquez-production \
  --services marquez-api marquez-web

# View recent logs
aws logs tail /ecs/marquez-api --follow

# Force new deployment
aws ecs update-service \
  --cluster marquez-production \
  --service marquez-api \
  --force-new-deployment

# Check RDS status
aws rds describe-db-instances \
  --db-instance-identifier marquez-production
```

## Cost Optimization

1. Use Fargate Spot for non-production environments
2. Implement proper auto-scaling policies
3. Use RDS reserved instances for production
4. Enable S3 lifecycle policies for CloudWatch logs
5. Consider using AWS OpenSearch Serverless for search functionality

## Backup and Recovery

1. **RDS Automated Backups**: 30-day retention configured
2. **Manual Snapshots**: Create before major changes
3. **Point-in-Time Recovery**: Available within backup retention period

```bash
# Create manual snapshot
aws rds create-db-snapshot \
  --db-instance-identifier marquez-production \
  --db-snapshot-identifier marquez-manual-$(date +%Y%m%d)
```

## Updating Marquez

1. Build new Docker images with updated code
2. Push to ECR with new tag
3. Update ECS task definitions with new image tag
4. Deploy using the deployment script

```bash
# Update to new version
IMAGE_TAG=v1.1.0 ./deploy/aws/ecs-terraform/scripts/deploy.sh
```

## Clean Up

To remove all resources:

```bash
cd deploy/aws/ecs-terraform/terraform
terraform destroy
```

**Warning**: This will delete all resources including the RDS database. Ensure you have backups if needed.