variable "environment" {
  description = "Environment name (sandbox, dev, staging, production)"
  type        = string
  default     = "sandbox"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "marquez"
}

variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.1.0.0/16"
}

variable "availability_zones_count" {
  description = "Number of availability zones to use"
  type        = number
  default     = 2
}

variable "enable_nat_gateway" {
  description = "Enable NAT Gateway for private subnets"
  type        = bool
  default     = true
}

variable "nat_gateway_count" {
  description = "Number of NAT Gateways (1 for single NAT, 2 for HA)"
  type        = number
  default     = 1  # Use 1 for sandbox/dev, 2 for production
}

variable "database_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "database_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 20
}

variable "database_multi_az" {
  description = "Enable Multi-AZ for RDS"
  type        = bool
  default     = false  # false for sandbox/dev, true for production
}

variable "ecs_task_cpu" {
  description = "CPU units for ECS task"
  type        = string
  default     = "512"
}

variable "ecs_task_memory" {
  description = "Memory for ECS task in MB"
  type        = string
  default     = "1024"
}

variable "ecs_service_desired_count" {
  description = "Desired number of ECS service tasks"
  type        = number
  default     = 1  # 1 for sandbox, 2+ for production
}

variable "enable_deletion_protection" {
  description = "Enable deletion protection for RDS and ALB"
  type        = bool
  default     = false  # false for sandbox/dev, true for production
}

variable "backup_retention_period" {
  description = "RDS backup retention period in days"
  type        = number
  default     = 1  # 1 for sandbox, 7+ for production
}

variable "tags" {
  description = "Common tags to apply to all resources"
  type        = map(string)
  default     = {}
}

locals {
  common_tags = merge(
    {
      Environment = var.environment
      Project     = var.project_name
      ManagedBy   = "terraform"
    },
    var.tags
  )
  
  name_prefix = "${var.project_name}-${var.environment}"
}