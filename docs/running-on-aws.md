---
layout: running-on-aws
---

# Running Marquez on AWS

This guide helps you set up Marquez from scratch, on AWS, without using an automated approach. It details step-by-step how to go from a bare AWS account, to a fully functioning Marquez deployment that members of your company can use.

#### PREREQUISITES

* AWS CLI
* Helm
* kubectl

## AWS EKS Cluster

To create an EKS cluster, please follow the steps outlined in the AWS EKS [documentation](https://docs.aws.amazon.com/eks/latest/userguide/create-cluster.html).

##### CONNECT TO EKS CLUSTER

1. Use your AWS account access keys to run the following command to update your kubectl config and switch to the new EKS cluster context:

   ```bash
   export AWS_ACCESS_KEY_ID=<AWS-ACCESS-KEY-ID>
   export AWS_SECRET_ACCESS_KEY=<AWS-SECRET-ACCESS-KEY>
   export AWS_SESSION_TOKEN=<AWS-SESSION-TOKEN>
   ```
   
2. Switch to your EKS cluster context:

   ```bash
   $ aws eks update-kubeconfig --name <AWS-EKS-CLUSTER> --region <AWS-REGION>
   ```

3. Verify the context is switched:

   ```bash
   $ kubectl config current-context
   arn:aws:eks:<AWS-REGION>:<AWS_ACCOUNT_ID>:cluster/<AWS-EKS-CLUSTER>
   ```

4. Using kubectl, let's verify we can connect to your EKS cluster:

   ```bash
   $ kubectl get pods
   No resources found in default namespace.
   ```
   
## AWS RDS

Next, we'll create an AWS RDS instance as outlined in the AWS RDS [documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_CreateDBInstance.html). This database will be used to store dataset and job metadata collected via the Marquez HTTP API.

##### CREATE RDS DATABASE

1. Navigate to [RDS](https://console.aws.amazon.com/rds/home) and create a PostgreSQL compatible database, leaving the database `Template` as `Production`
3. Use `marquez` as the cluster identifier, and set the master username to `marquez`
5. Choose a master password which you’ll later use in your Helm template (see [password](https://github.com/MarquezProject/marquez/blob/main/chart/values.yaml#L32)  in `values.yaml`)
6. Leave public access to the database off
7. Choose the same VPC that your EKS cluster is in
8. In a separate tab, navigate to the EKS cluster page and make note of the security group attached to your cluster
9. Go back to the RDS page and in the security group section, add the EKS cluster’s security group (feel free to leave the default as well). This will ensure you don’t have to play around with security group rules in order for pods running in the cluster to access the RDS instance
10. Under the top level Additional configuration (there’s a sub menu by the same name) under “Initial database name” enter `marquez` as well

##### CONNECT TO RDS DATABASE

1. Create a `marquez` namespace:
   
   ```bash
   $ kubectl create namespace marquez
   ```
2. Next, run the following command with the username and password you used, and the host returned by AWS:

   ```bash
   kubectl run pgsql-postgresql-client --rm --tty -i --restart='Never' \
     --namespace marquez \
     --image docker.io/bitnami/postgresql:11.7.0-debian-10-r9 \
     --env="PGPASSWORD=<AWS-RDS-PASSWORD>" \
     --command -- psql marquez --host <AWS-RDS-HOST> -U <AWS-RDS-USERNAME> -d marquez -p 5432
   ```

## Deploy Marquez on EKS

##### INSTALLING MARQUEZ

1. Clone the Marquez repo:

   ```bash
   $ git clone git@github.com:MarquezProject/marquez.git && cd chart
   ```
2. Install Marquez:
   
   ```bash
   helm install --namespace marquez --create-namespace marquez .
   ```
   
3. Verify all the pods have come up correctly:

   ```bash
   $ kubectl get pods -n marquez
   ```
 
##### UPGRADING MARQUEZ

```bash
helm upgrade --namespace marquez .
```
  
##### UNINSTALLING MARQUEZ

```bash
helm uninstall --namespace marquez marquez
```------------