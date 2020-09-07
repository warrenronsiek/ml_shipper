resource "aws_ecr_repository" "ftpuffin-worker" {
  name = "ftpuffin-worker-${terraform.workspace}"
}

resource "aws_batch_compute_environment" "ftpuffin_compute" {
  // ignore the missing "compute environment name missing" error. The requirement is satisfied by the name prefix
  // argument. Basically, you cant delete the last compute environment for a queue. So when changing theis resource,
  // you need to create a new one before you delete the old one. The name prefix means that you don't get a namespace
  // error.
  lifecycle {
    create_before_destroy = true
    ignore_changes = [compute_resources]
  }
  compute_environment_name_prefix = "ftpuffin-worker-${terraform.workspace}-"
  service_role = aws_iam_role.batch_service_role.arn
  type = "MANAGED"
  depends_on = [
    aws_iam_role.batch_instance_role,
    aws_iam_role.batch_service_role,
    aws_iam_policy.batch_instance_policy,
    aws_iam_policy.batch_service_policy,
    aws_iam_role_policy_attachment.aws_batch_service_role,
    aws_iam_role_policy_attachment.ftpuffin_worker_instance_role_policy_attach,
    aws_iam_instance_profile.ftpuffin_batch_instance_profile
  ]
  compute_resources {
    instance_role = aws_iam_instance_profile.ftpuffin_batch_instance_profile.arn
    instance_type = ["m5.xlarge"]
    max_vcpus = 10
    min_vcpus = 0
    bid_percentage = 50
    security_group_ids = [aws_security_group.private_sec_group.id]
    subnets = [aws_subnet.private_subnet_a.id, aws_subnet.private_subnet_b.id, aws_subnet.private_subnet_c.id]
    type = "SPOT"
    allocation_strategy = "SPOT_CAPACITY_OPTIMIZED"
  }
}

resource "aws_batch_job_definition" "ftpuffin_worker" {
  name = "ftpuffin-work-job-def-${terraform.workspace}"
  type = "container"
  container_properties = data.template_file.batch_container_props.rendered
}

data template_file batch_container_props {
  template = file("${path.module}/batch_container_properties.json")
  vars = {
    env = terraform.workspace
    image = aws_ecr_repository.ftpuffin-worker.repository_url
    log_group_name = "ftpuffin-worker-${terraform.workspace}"
    log_group_region = "us-west-2"
  }
}

resource "aws_batch_job_queue" "ftpuff_job_queue" {
  compute_environments = [aws_batch_compute_environment.ftpuffin_compute.arn]
  name = "ftpuffin-worker-queue-${terraform.workspace}"
  priority = 1
  state = "ENABLED"
}