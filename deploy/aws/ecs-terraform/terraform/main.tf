terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.region
}

# Additional Variables for sensitive data
variable "db_password" {
  description = "Password for RDS database"
  type        = string
  sensitive   = true
}

variable "opensearch_enabled" {
  description = "Enable OpenSearch integration"
  type        = bool
  default     = false
}

# ECR Repositories
resource "aws_ecr_repository" "marquez_api" {
  name                 = "${local.name_prefix}-api"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = local.common_tags
}

resource "aws_ecr_repository" "marquez_web" {
  name                 = "${local.name_prefix}-web"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = local.common_tags
}

# ECS Cluster
resource "aws_ecs_cluster" "marquez" {
  name = local.name_prefix

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = local.common_tags
}

# CloudWatch Log Groups
resource "aws_cloudwatch_log_group" "marquez_api" {
  name              = "/ecs/${local.name_prefix}-api"
  retention_in_days = 30

  tags = local.common_tags
}

resource "aws_cloudwatch_log_group" "marquez_web" {
  name              = "/ecs/${local.name_prefix}-web"
  retention_in_days = 30

  tags = local.common_tags
}

# IAM Roles
resource "aws_iam_role" "ecs_task_execution" {
  name = "${local.name_prefix}-ecs-task-execution"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_secrets" {
  name = "ecs-secrets-policy"
  role = aws_iam_role.ecs_task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = [
          aws_secretsmanager_secret.db_password.arn
        ]
      }
    ]
  })
}

resource "aws_iam_role" "ecs_task" {
  name = "${local.name_prefix}-ecs-task"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = local.common_tags
}

# Secrets Manager
resource "aws_secretsmanager_secret" "db_password" {
  name = "${local.name_prefix}-db-password"

  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = var.db_password
}

# Security Groups
resource "aws_security_group" "alb" {
  name_prefix = "${local.name_prefix}-alb-"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-alb-sg"
    }
  )
}

resource "aws_security_group" "ecs_tasks" {
  name_prefix = "${local.name_prefix}-ecs-tasks-"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-ecs-tasks-sg"
    }
  )
}

resource "aws_security_group" "rds" {
  name_prefix = "${local.name_prefix}-rds-"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_tasks.id]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-rds-sg"
    }
  )
}

# RDS PostgreSQL
resource "aws_db_instance" "marquez" {
  identifier     = local.name_prefix
  engine         = "postgres"
  engine_version = "14"
  instance_class = var.database_instance_class
  
  allocated_storage     = var.database_allocated_storage
  max_allocated_storage = var.database_allocated_storage * 10
  storage_type          = "gp3"
  storage_encrypted     = true
  
  db_name  = "marquez"
  username = "marquez"
  password = var.db_password
  
  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name
  
  backup_retention_period = var.backup_retention_period
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"
  
  multi_az            = var.database_multi_az
  deletion_protection = var.enable_deletion_protection
  skip_final_snapshot = !var.enable_deletion_protection
  final_snapshot_identifier = var.enable_deletion_protection ? "${local.name_prefix}-final-${formatdate("YYYY-MM-DD-hhmm", timestamp())}" : null
  
  enabled_cloudwatch_logs_exports = ["postgresql"]
  
  tags = merge(
    local.common_tags,
    {
      Name = local.name_prefix
    }
  )
}

# Application Load Balancer
resource "aws_alb" "marquez" {
  name               = local.name_prefix
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id

  enable_deletion_protection = var.enable_deletion_protection
  enable_http2               = true
  enable_cross_zone_load_balancing = true

  tags = local.common_tags
}

# Target Groups
resource "aws_alb_target_group" "api" {
  name        = "${local.name_prefix}-api"
  port        = 5000
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    enabled             = true
    path                = "/healthcheck"
    port                = 5001
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = local.common_tags
}

resource "aws_alb_target_group" "api_admin" {
  name        = "${local.name_prefix}-api-admin"
  port        = 5001
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    enabled             = true
    path                = "/healthcheck"
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = local.common_tags
}

resource "aws_alb_target_group" "web" {
  name        = "${local.name_prefix}-web"
  port        = 3000
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    enabled             = true
    path                = "/"
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = local.common_tags
}

# ALB Listeners
resource "aws_alb_listener" "http" {
  load_balancer_arn = aws_alb.marquez.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.web.arn
  }
}

# Note: HTTPS listener requires ACM certificate
# resource "aws_alb_listener" "https" {
#   load_balancer_arn = aws_alb.marquez.arn
#   port              = "443"
#   protocol          = "HTTPS"
#   ssl_policy        = "ELBSecurityPolicy-TLS-1-2-2017-01"
#   certificate_arn   = var.certificate_arn
#
#   default_action {
#     type             = "forward"
#     target_group_arn = aws_alb_target_group.web.arn
#   }
# }

# Listener Rules (for HTTP during development)
resource "aws_alb_listener_rule" "api" {
  listener_arn = aws_alb_listener.http.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.api.arn
  }

  condition {
    path_pattern {
      values = ["/api/*", "/lineage/*"]
    }
  }
}

resource "aws_alb_listener_rule" "api_admin" {
  listener_arn = aws_alb_listener.http.arn
  priority     = 101

  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.api_admin.arn
  }

  condition {
    path_pattern {
      values = ["/healthcheck", "/metrics"]
    }
  }
}

resource "aws_alb_listener_rule" "web" {
  listener_arn = aws_alb_listener.http.arn
  priority     = 102

  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.web.arn
  }

  condition {
    path_pattern {
      values = ["/*"]
    }
  }
}

# ECS Task Definitions
resource "aws_ecs_task_definition" "marquez_api" {
  family                   = "${local.name_prefix}-api"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = "marquez-api"
      image     = "${aws_ecr_repository.marquez_api.repository_url}:latest"
      essential = true
      
      portMappings = [
        {
          containerPort = 5000
          protocol      = "tcp"
        },
        {
          containerPort = 5001
          protocol      = "tcp"
        }
      ]
      
      environment = [
        {
          name  = "MARQUEZ_PORT"
          value = "5000"
        },
        {
          name  = "MARQUEZ_ADMIN_PORT"
          value = "5001"
        },
        {
          name  = "POSTGRES_HOST"
          value = aws_db_instance.marquez.address
        },
        {
          name  = "POSTGRES_PORT"
          value = "5432"
        },
        {
          name  = "POSTGRES_DB"
          value = "marquez"
        },
        {
          name  = "POSTGRES_USER"
          value = "marquez"
        },
        {
          name  = "MIGRATE_ON_STARTUP"
          value = "true"
        },
        {
          name  = "SEARCH_ENABLED"
          value = tostring(var.opensearch_enabled)
        }
      ]
      
      secrets = [
        {
          name      = "POSTGRES_PASSWORD"
          valueFrom = aws_secretsmanager_secret.db_password.arn
        }
      ]
      
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.marquez_api.name
          awslogs-region        = var.region
          awslogs-stream-prefix = "ecs"
        }
      }
      
      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:5001/healthcheck || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = local.common_tags
}

resource "aws_ecs_task_definition" "marquez_web" {
  family                   = "${local.name_prefix}-web"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = "marquez-web"
      image     = "${aws_ecr_repository.marquez_web.repository_url}:latest"
      essential = true
      
      portMappings = [
        {
          containerPort = 3000
          protocol      = "tcp"
        }
      ]
      
      environment = [
        {
          name  = "MARQUEZ_HOST"
          value = aws_alb.marquez.dns_name
        },
        {
          name  = "MARQUEZ_PORT"
          value = "80"
        },
        {
          name  = "WEB_PORT"
          value = "3000"
        },
        {
          name  = "REACT_APP_ADVANCED_SEARCH"
          value = "true"
        }
      ]
      
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.marquez_web.name
          awslogs-region        = var.region
          awslogs-stream-prefix = "ecs"
        }
      }
      
      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:3000 || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = local.common_tags
}

# ECS Services
resource "aws_ecs_service" "marquez_api" {
  name            = "${local.name_prefix}-api"
  cluster         = aws_ecs_cluster.marquez.id
  task_definition = aws_ecs_task_definition.marquez_api.arn
  desired_count   = var.ecs_service_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.api.arn
    container_name   = "marquez-api"
    container_port   = 5000
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.api_admin.arn
    container_name   = "marquez-api"
    container_port   = 5001
  }

  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  health_check_grace_period_seconds  = 60

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  depends_on = [
    aws_alb_listener.http,
    aws_db_instance.marquez
  ]

  tags = local.common_tags
}

resource "aws_ecs_service" "marquez_web" {
  name            = "${local.name_prefix}-web"
  cluster         = aws_ecs_cluster.marquez.id
  task_definition = aws_ecs_task_definition.marquez_web.arn
  desired_count   = var.ecs_service_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.web.arn
    container_name   = "marquez-web"
    container_port   = 3000
  }

  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  health_check_grace_period_seconds  = 60

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  depends_on = [
    aws_alb_listener.http,
    aws_ecs_service.marquez_api
  ]

  tags = local.common_tags
}

# Auto Scaling
resource "aws_appautoscaling_target" "api" {
  max_capacity       = var.ecs_service_desired_count * 5
  min_capacity       = var.ecs_service_desired_count
  resource_id        = "service/${aws_ecs_cluster.marquez.name}/${aws_ecs_service.marquez_api.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "api_cpu" {
  name               = "${local.name_prefix}-api-cpu"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.api.resource_id
  scalable_dimension = aws_appautoscaling_target.api.scalable_dimension
  service_namespace  = aws_appautoscaling_target.api.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value = 70
  }
}

resource "aws_appautoscaling_target" "web" {
  max_capacity       = var.ecs_service_desired_count * 5
  min_capacity       = var.ecs_service_desired_count
  resource_id        = "service/${aws_ecs_cluster.marquez.name}/${aws_ecs_service.marquez_web.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "web_cpu" {
  name               = "${local.name_prefix}-web-cpu"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.web.resource_id
  scalable_dimension = aws_appautoscaling_target.web.scalable_dimension
  service_namespace  = aws_appautoscaling_target.web.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value = 70
  }
}

# Outputs
output "alb_dns_name" {
  value       = aws_alb.marquez.dns_name
  description = "DNS name of the load balancer"
}

output "ecr_repository_api" {
  value       = aws_ecr_repository.marquez_api.repository_url
  description = "ECR repository URL for API"
}

output "ecr_repository_web" {
  value       = aws_ecr_repository.marquez_web.repository_url
  description = "ECR repository URL for Web"
}

output "rds_endpoint" {
  value       = aws_db_instance.marquez.address
  description = "RDS instance endpoint"
}

output "vpc_id" {
  value       = aws_vpc.main.id
  description = "VPC ID"
}

output "environment" {
  value       = var.environment
  description = "Environment name"
}