resource "aws_vpc" "vpc" {
  cidr_block = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support = true
  tags = {
    Name = "ml-shipper-vpc${title(terraform.workspace)}"
  }
}

resource "aws_subnet" "private_subnet_a" {
  cidr_block = local.private_subnet_a_cidr
  vpc_id = aws_vpc.vpc.id
  availability_zone = "us-west-2a"
  tags = {
    Name = "ml-shipper-subnet-a-${title(terraform.workspace)}"
  }
}

resource "aws_subnet" "private_subnet_b" {
  cidr_block = local.private_subnet_b_cidr
  vpc_id = aws_vpc.vpc.id
  availability_zone = "us-west-2b"
  tags = {
    Name = "ml-shipper-subnet-b-${title(terraform.workspace)}"
  }
}
resource "aws_subnet" "private_subnet_c" {
  cidr_block = local.private_subnet_c_cidr
  vpc_id = aws_vpc.vpc.id
  availability_zone = "us-west-2c"
  tags = {
    Name = "ml-shipper-subnet-c-${title(terraform.workspace)}"
  }
}

resource "aws_subnet" "public_subnet_a" {
  cidr_block = local.public_subnet_a_cidr
  vpc_id = aws_vpc.vpc.id
  availability_zone = "us-west-2a"
  tags = {
    Name = "ml-shipper-public-subnet-a-${title(terraform.workspace)}"
  }
}

resource "aws_subnet" "public_subnet_b" {
  cidr_block = local.public_subnet_b_cidr
  vpc_id = aws_vpc.vpc.id
  availability_zone = "us-west-2b"
  tags = {
    Name = "ml-shipper-public-subnet-b-${title(terraform.workspace)}"
  }
}

resource "aws_subnet" "public_subnet_c" {
  cidr_block = local.public_subnet_c_cidr
  vpc_id = aws_vpc.vpc.id
  availability_zone = "us-west-2c"
  tags = {
    Name = "ml-shipper-public-subnet-c-${title(terraform.workspace)}"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.vpc.id
  tags = {
    Name = "ml-shipper-igw-${title(terraform.workspace)}"
  }
}

resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.nat_ip.id
  subnet_id = aws_subnet.public_subnet_a.id
  tags = {
    Name = "ml-shipper-nat-${title(terraform.workspace)}"
  }
}

resource "aws_eip" "nat_ip" {
  vpc = true
}

resource "aws_route_table" "route_table_public" {
  vpc_id = aws_vpc.vpc.id
  depends_on = [
    aws_vpc.vpc,
    aws_internet_gateway.igw
  ]

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
  tags = {
    Name = "ml-shipper-public-${title(terraform.workspace)}"
  }
}

resource "aws_route_table" "route_table_private" {
  vpc_id = aws_vpc.vpc.id
  depends_on = [
    aws_vpc.vpc,
    aws_nat_gateway.nat
  ]

  route {
    cidr_block = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat.id
  }
  tags = {
    Name = "ml-shipper-private-${title(terraform.workspace)}"
  }
}

resource "aws_route_table_association" "route_assoc_to_sbnet_a" {
  route_table_id = aws_route_table.route_table_public.id
  subnet_id = aws_subnet.public_subnet_a.id
}

resource "aws_route_table_association" "route_assoc_to_sbnet_b" {
  route_table_id = aws_route_table.route_table_public.id
  subnet_id = aws_subnet.public_subnet_b.id
}

resource "aws_route_table_association" "route_assoc_to_sbnet_c" {
  route_table_id = aws_route_table.route_table_public.id
  subnet_id = aws_subnet.public_subnet_c.id
}

resource "aws_route_table_association" "route_assoc_to_private_sbnet_a" {
  route_table_id = aws_route_table.route_table_private.id
  subnet_id = aws_subnet.private_subnet_a.id
}

resource "aws_route_table_association" "route_assoc_to_private_sbnet_b" {
  route_table_id = aws_route_table.route_table_private.id
  subnet_id = aws_subnet.private_subnet_b.id
}

resource "aws_route_table_association" "route_assoc_to_private_sbnet_c" {
  route_table_id = aws_route_table.route_table_private.id
  subnet_id = aws_subnet.private_subnet_c.id
}

resource "aws_security_group" "public_sec_group" {
  vpc_id = aws_vpc.vpc.id
  name = "ml-shipper-public-secgrp-${terraform.workspace}"

  ingress {
    from_port = 22
    protocol = "tcp"
    to_port = 22
    cidr_blocks     = ["0.0.0.0/0"]
  }

  ingress {
    from_port = 443
    protocol = "tcp"
    to_port = 443
    cidr_blocks     = ["0.0.0.0/0"]
  }

  egress {
    from_port = 0
    protocol = "-1"
    to_port = 0
    cidr_blocks     = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "private_sec_group" {
  vpc_id = aws_vpc.vpc.id
  name = "ml-shipper-private-secgrp-${terraform.workspace}"

  ingress {
    // this rule seems too broad, but its actually required to get the network load balancer to work correctly.
    // this means that the private subnet is isolated from the internet based not on security group rules, but on
    // not having a IGW. In effect, it will inherit the ingress rules from the public subnet, so be careful about your
    // public ingress.
    from_port = 0
    protocol = "-1"
    to_port = 0
    cidr_blocks     = ["0.0.0.0/0"]
  }

  egress {
    from_port = 0
    protocol = "-1"
    to_port = 0
    cidr_blocks     = ["0.0.0.0/0"]
  }
}

resource "aws_vpc_endpoint" "s3" {
  vpc_id       = aws_vpc.vpc.id
  service_name = "com.amazonaws.us-west-2.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids = [aws_route_table.route_table_public.id, aws_route_table.route_table_private.id]
}

//resource "aws_vpc_endpoint" "ssm" {
//  service_name = "com.amazonaws.us-west-2.ssm"
//  vpc_id = aws_vpc.vpc.id
//  vpc_endpoint_type = "Interface"
//  subnet_ids = [aws_subnet.private_subnet_c.id, aws_subnet.private_subnet_b.id, aws_subnet.private_subnet_a.id]
//  security_group_ids = [aws_security_group.private_sec_group.id]
//}
//
//resource "aws_vpc_endpoint" "ssm_ec2messages" {
//  service_name = "com.amazonaws.us-west-2.ec2messages"
//  vpc_id = aws_vpc.vpc.id
//  vpc_endpoint_type = "Interface"
//  subnet_ids = [aws_subnet.private_subnet_c.id, aws_subnet.private_subnet_b.id, aws_subnet.private_subnet_a.id]
//  security_group_ids = [aws_security_group.private_sec_group.id]
//}
//
//resource "aws_vpc_endpoint" "ssm_ec2" {
//  service_name = "com.amazonaws.us-west-2.ec2"
//  vpc_id = aws_vpc.vpc.id
//  vpc_endpoint_type = "Interface"
//  subnet_ids = [aws_subnet.private_subnet_c.id, aws_subnet.private_subnet_b.id, aws_subnet.private_subnet_a.id]
//  security_group_ids = [aws_security_group.private_sec_group.id]
//}
//
//
//resource "aws_vpc_endpoint" "ssmmessages" {
//  service_name = "com.amazonaws.us-west-2.ssmmessages"
//  vpc_id = aws_vpc.vpc.id
//  vpc_endpoint_type = "Interface"
//  subnet_ids = [aws_subnet.private_subnet_c.id, aws_subnet.private_subnet_b.id, aws_subnet.private_subnet_a.id]
//  security_group_ids = [aws_security_group.private_sec_group.id]
//}
//resource "aws_vpc_endpoint" "logs" {
//  vpc_id       = aws_vpc.vpc.id
//  service_name = "com.amazonaws.us-west-2.logs"
//  vpc_endpoint_type = "Interface"
//  subnet_ids = [aws_subnet.public_subnet_a.id,aws_subnet.public_subnet_b.id,aws_subnet.public_subnet_c.id]
//  security_group_ids = [aws_security_group.public_sec_group.id, aws_security_group.private_sec_group.id]
//}
//
//resource "aws_vpc_endpoint" "codebuild" {
//  vpc_id       = aws_vpc.vpc.id
//  service_name = "com.amazonaws.us-west-2.codebuild"
//  vpc_endpoint_type = "Interface"
//  subnet_ids = [aws_subnet.public_subnet_a.id,aws_subnet.public_subnet_b.id,aws_subnet.public_subnet_c.id]
//  security_group_ids = [aws_security_group.public_sec_group.id, aws_security_group.private_sec_group.id]
//}
