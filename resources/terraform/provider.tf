terraform {
  backend "s3" {
    bucket = BUCKET
    key = "terraform"
    region = "us-west-2"
  }
}

provider "aws" {
  region = "us-west-2"
  version = "3.5.0"
}
