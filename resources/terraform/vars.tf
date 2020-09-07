data "aws_caller_identity" "current" {}


locals  {
  private_subnet_a_cidr = "10.0.0.0/24"
  private_subnet_b_cidr = "10.0.4.0/24"
  private_subnet_c_cidr = "10.0.5.0/24"
  public_subnet_a_cidr = "10.0.1.0/24"
  public_subnet_b_cidr = "10.0.2.0/24"
  public_subnet_c_cidr = "10.0.3.0/24"
  account_id = data.aws_caller_identity.current.account_id
  s3_bucket_name = "ml-shipper-${local.account_id}"
  s3_bucket_prefixes = {
    load_balancer_logs = "logs"
  }
}