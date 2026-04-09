# Sample Terraform configuration for testing the Cost Intelligence Analyzer

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
  region = "us-east-1"
}

# EC2 Instance
resource "aws_instance" "web_server" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t3.large"
  count         = 2

  tags = {
    Name = "WebServer"
  }
}

# RDS Database
resource "aws_db_instance" "main" {
  allocated_storage    = 20
  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = "db.m5.large"
  username             = "admin"
  password             = "password123"
  db_name              = "mydb"
  skip_final_snapshot  = true

  tags = {
    Name = "MainDatabase"
  }
}

# S3 Bucket
resource "aws_s3_bucket" "data_bucket" {
  bucket = "my-cost-intelligent-bucket"

  tags = {
    Name = "DataStorage"
  }
}

# Load Balancer
resource "aws_lb" "main" {
  name               = "cost-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = ["subnet-12345", "subnet-67890"]

  tags = {
    Name = "CostALB"
  }
}

resource "aws_security_group" "alb" {
  name        = "alb-sg"
  description = "Security group for ALB"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Lambda Function
resource "aws_lambda_function" "processor" {
  filename      = "lambda.zip"
  function_name = "data-processor"
  role          = aws_iam_role.lambda_role.arn
  handler       = "index.handler"
  memory_size   = 256

  depends_on = [
    aws_iam_role_policy_attachment.lambda_basic
  ]
}

resource "aws_iam_role" "lambda_role" {
  name = "lambda-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_basic" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.lambda_role.name
}

# Outputs
output "instance_ids" {
  value       = aws_instance.web_server[*].id
  description = "IDs of the EC2 instances"
}

output "db_endpoint" {
  value       = aws_db_instance.main.endpoint
  description = "RDS endpoint"
}

output "load_balancer_dns" {
  value       = aws_lb.main.dns_name
  description = "DNS name of the load balancer"
}

