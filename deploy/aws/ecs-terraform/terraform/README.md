# Marquez ECS Deployment with Terraform

This Terraform configuration deploys Marquez on AWS ECS with RDS PostgreSQL in a dedicated VPC.

## Architecture

- **VPC**: Isolated network with public, private, and database subnets
- **ECS Fargate**: Serverless container orchestration
- **RDS PostgreSQL**: Managed database with configurable HA options
- **ALB**: Application Load Balancer for traffic distribution
- **VPC Endpoints**: Private connectivity to AWS services
- **NAT Gateway**: Outbound internet access for private subnets

## Prerequisites

1. AWS CLI configured with appropriate credentials
2. Terraform >= 1.0
3. Docker for building container images

## Directory Structure

```
terraform/
├── main.tf                 # Main configuration (ECS, RDS, ALB)
├── vpc.tf                  # VPC and networking resources
├── vpc-endpoints.tf        # VPC endpoints for AWS services
├── variables.tf            # Variable definitions
├── terraform.tfvars.example # Example configuration
└── environments/           # Environment-specific configurations
    ├── sandbox.tfvars
    └── production.tfvars
```

## Deployment Steps

### 1. Initialize Terraform

```bash
cd deploy/terraform
terraform init
```

### 2. Configure Environment Variables

Copy the example configuration and update with your values:

```bash
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your configuration
```

Or use environment-specific configuration:

```bash
terraform plan -var-file=environments/sandbox.tfvars
```

### 3. Set Database Password

Export the database password as an environment variable:

```bash
export TF_VAR_db_password="your-secure-password"
```

### 4. Plan Deployment

Review the resources that will be created:

```bash
# For sandbox environment
terraform plan -var-file=environments/sandbox.tfvars

# For production environment
terraform plan -var-file=environments/production.tfvars
```

### 5. Apply Configuration

Deploy the infrastructure:

```bash
# For sandbox environment
terraform apply -var-file=environments/sandbox.tfvars

# For production environment
terraform apply -var-file=environments/production.tfvars
```

### 6. Build and Push Docker Images

After infrastructure is created, build and push Docker images:

```bash
# Get ECR repository URLs from Terraform output
API_REPO=$(terraform output -raw ecr_repository_api)
WEB_REPO=$(terraform output -raw ecr_repository_web)

# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $API_REPO

# Build and push API image
cd ../../
docker build -f docker/Dockerfile.api -t $API_REPO:latest .
docker push $API_REPO:latest

# Build and push Web image
docker build -f docker/Dockerfile.web -t $WEB_REPO:latest ./web
docker push $WEB_REPO:latest
```

### 7. Update ECS Services

Force new deployment to use the pushed images:

```bash
aws ecs update-service --cluster marquez-sandbox --service marquez-sandbox-api --force-new-deployment
aws ecs update-service --cluster marquez-sandbox --service marquez-sandbox-web --force-new-deployment
```

## Environment Configurations

### Sandbox
- Cost-optimized configuration
- Single NAT Gateway
- Minimal resources (db.t3.micro, 1 ECS task)
- No deletion protection

### Production
- High availability configuration
- Multi-AZ deployment
- Multiple NAT Gateways
- Larger resources (db.t3.medium+, 3+ ECS tasks)
- Deletion protection enabled

## Key Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `environment` | Environment name (sandbox, dev, staging, production) | sandbox |
| `vpc_cidr` | CIDR block for VPC | 10.1.0.0/16 |
| `nat_gateway_count` | Number of NAT Gateways | 1 |
| `database_instance_class` | RDS instance class | db.t3.micro |
| `ecs_service_desired_count` | Number of ECS tasks | 1 |
| `enable_deletion_protection` | Enable deletion protection | false |

## Outputs

After deployment, Terraform will output:
- `alb_dns_name`: URL to access Marquez
- `ecr_repository_api`: ECR repository for API image
- `ecr_repository_web`: ECR repository for Web image
- `rds_endpoint`: RDS database endpoint
- `vpc_id`: Created VPC ID

## Cost Optimization Tips

1. **Sandbox/Dev**: Use single NAT Gateway (`nat_gateway_count = 1`)
2. **Off-hours**: Use AWS Instance Scheduler to stop RDS instances
3. **ECS Tasks**: Scale down during non-business hours
4. **VPC Endpoints**: Only create required endpoints

## Cleanup

To destroy all resources:

```bash
terraform destroy -var-file=environments/sandbox.tfvars
```

**Warning**: This will delete all resources including the database. Ensure you have backups if needed.

## Troubleshooting

### ECS Tasks Failing to Start
- Check CloudWatch Logs for container errors
- Verify ECR images are pushed correctly
- Ensure VPC endpoints are configured for private subnets

### Database Connection Issues
- Verify security group rules
- Check RDS subnet group configuration
- Ensure database password is set correctly

### ALB Health Check Failures
- Verify target group health check settings
- Check application startup time and adjust `health_check_grace_period_seconds`
- Review ECS task logs for application errors