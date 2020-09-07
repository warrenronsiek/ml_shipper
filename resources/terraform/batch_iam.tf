
resource aws_iam_role batch_service_role {
  name = "batch-service-role-${terraform.workspace}"
  assume_role_policy = data.aws_iam_policy_document.batch_assume_role.json
}

data aws_iam_policy_document batch_assume_role {
  statement {
    actions = ["sts:AssumeRole"]
    effect = "Allow"
    principals {
      identifiers = ["batch.amazonaws.com"]
      type = "Service"
    }
  }
}

resource aws_iam_role_policy_attachment aws_batch_service_role {
  role       = aws_iam_role.batch_service_role.id
  policy_arn = aws_iam_policy.batch_service_policy.arn
}

resource aws_iam_policy batch_service_policy {
  name = "batch-service-policy-${terraform.workspace}"
  policy = data.aws_iam_policy_document.batch_service_policy.json
}

data aws_iam_policy_document batch_service_policy {
  statement {
    effect = "Allow"
    resources = ["*"]
    actions = [
      "ec2:*",
      "autoscaling:*",
      "ecs:*",
      "logs:*",
      "iam:GetInstanceProfile",
      "iam:GetRole",
      "iam:PassRole",
      "iam:CreateServiceLinkedRole"
    ]
  }
}

resource "aws_iam_instance_profile" "ftpuffin_batch_instance_profile" {
  name = "worker-instance-profile-${terraform.workspace}"
  role = aws_iam_role.batch_instance_role.name
}

resource "aws_iam_role" "batch_instance_role" {
  name = "batch-instance-role-${terraform.workspace}"
  assume_role_policy = data.aws_iam_policy_document.batch_instance_assume_role.json
}

data aws_iam_policy_document batch_instance_assume_role {
  statement {
    actions = ["sts:AssumeRole"]
    effect = "Allow"
    principals {
      identifiers = ["ec2.amazonaws.com"]
      type = "Service"
    }
  }
}

resource "aws_iam_role_policy_attachment" "ftpuffin_worker_instance_role_policy_attach" {
  policy_arn = aws_iam_policy.batch_instance_policy.arn
  role = aws_iam_role.batch_instance_role.id
}

resource "aws_iam_policy" "batch_instance_policy" {
  name = "worker-instance-policy"
  policy = data.aws_iam_policy_document.batch_instance_policy.json
}

data aws_iam_policy_document batch_instance_policy {
  // these are the default permissions required to operate batch instances using ECR/ECS (batch uses ECS low key if
  // you want it to run docker containers.
  statement {
    actions = [
      "ec2:DescribeTags",
      "ecs:CreateCluster",
      "ecs:DeregisterContainerInstance",
      "ecs:DiscoverPollEndpoint",
      "ecs:Poll",
      "ecs:RegisterContainerInstance",
      "ecs:StartTelemetrySession",
      "ecs:UpdateContainerInstancesState",
      "ecs:Submit*",
      "ecr:GetAuthorizationToken",
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    effect = "Allow"
    resources = ["*"]
  }

  // for aws ssm access
  statement {
    effect = "Allow"
    actions = [
      "s3:GetEncryptionConfiguration",
      "ssmmessages:CreateControlChannel",
      "ssmmessages:CreateDataChannel",
      "ssmmessages:OpenControlChannel",
      "ssmmessages:OpenDataChannel",
      "ssm:UpdateInstanceInformation"
    ]
    resources = ["*"]
  }

  // this is so that ssm can write out logs and stuff to s3
  statement {
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:PutObjectAcl",
      "s3:GetEncryptionConfiguration"
    ]
    resources = [
      "arn:aws:s3:::${local.s3_bucket_name}",
      "arn:aws:s3:::${local.s3_bucket_name}/*"
    ]
  }
}