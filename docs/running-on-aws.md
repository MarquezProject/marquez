---
layout: running-on-aws
---

# Running Marquez on AWS

This guide helps you deploy and manage Marquez on AWS [EKS](https://aws.amazon.com/eks).

#### PREREQUISITES

* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html)
* [helm](https://helm.sh/docs/helm/helm_install/)
* [kubectl](https://kubernetes.io/docs/tasks/tools/)

## AWS EKS Cluster

To create an AWS EKS cluster, please follow the steps outlined in the AWS EKS [documentation](https://docs.aws.amazon.com/eks/latest/userguide/create-cluster.html).

##### CONNECT TO AWS EKS CLUSTER

1. [Make sure you have configured your AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html), then create or update the **kubeconfig** file for your cluster:

   ```bash
   $ aws eks --region <AWS-REGION> update-kubeconfig --name <AWS-EKS-CLUSTER>
   ```

2. Verify the context has been switched:

   ```bash
   $ kubectl config current-context
   arn:aws:eks:<AWS-REGION>:<AWS-ACCOUNT-ID>:cluster/<AWS-EKS-CLUSTER>
   ```

3. Using `kubectl`, verify you can connect to your cluster:

   ```bash
   $ kubectl get svc
   NAME             TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
   svc/kubernetes   ClusterIP   10.100.0.1   <none>        443/TCP   1m
   ```

   > **Note**: If you're having issues connecting to your cluster, please see [Why can't I connect to my AWS EKS cluster?](https://aws.amazon.com/premiumsupport/knowledge-center/eks-cluster-connection)

## AWS RDS

Next, we'll create an AWS RDS instance as outlined in the AWS RDS [documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_CreateDBInstance.html). This database will be used to store dataset, job, and run metadata collected as [OpenLineage](https://openlineage.io) events via the Marquez [HTTP API](https://marquezproject.github.io/marquez/openapi.html).

##### CREATE AWS RDS DATABASE

1. Navigate to the AWS [RDS](https://console.aws.amazon.com/rds/home) page and create a PostgreSQL database, leaving the database template as **Production**
3. Use `marquez` as the database identifier, and set the master username to `marquez`
5. Choose a master password which you’ll later use in your Helm deployment (see [password](https://github.com/MarquezProject/marquez/blob/main/chart/values.yaml#L32)  in `values.yaml`)
6. Leave public access to the database **off**
7. Choose the same VPC that your AWS EKS cluster is in
8. In a separate tab, navigate to the AWS EKS cluster page and make note of the security group attached to your cluster
9. Navigate back to the AWS RDS page and, in the security group section, add the AWS EKS cluster’s security group from **step 8**
10. Next, under the **Additional Configuration** tab, enter `marquez` as the initial database name
11. Finally, select **Create Database**

##### CONNECT TO AWS RDS DATABASE

1. Create a `marquez` namespace:

   ```bash
   $ kubectl create namespace marquez
   ```

2. Next, run the following command with the your AWS RDS `host`, `username`, and `password`:

   ```bash
   kubectl run pgsql-postgresql-client --rm --tty -i --restart='Never' \
     --namespace marquez \
     --image docker.io/bitnami/postgresql:12-debian-10 \
     --env="PGPASSWORD=<AWS-RDS-PASSWORD>" \
     --command -- psql marquez --host <AWS-RDS-HOST> -U <AWS-RDS-USERNAME> -d marquez -p 5432
   ```

## Deploy Marquez on AWS EKS

##### INSTALLING MARQUEZ

1. Get Marquez:

   ```bash
   $ git clone git@github.com:MarquezProject/marquez.git && cd chart
   ```

2. Install Marquez:

   ```bash
   helm upgrade --install marquez .
     --set marquez.db.host <AWS-RDS-HOST>
     --set marquez.db.user <AWS-RDS-USERNAME>
     --set marquez.db.password <AWS-RDS-PASSWORD>
     --namespace marquez
     --atomic
     --wait
   ```

   > **Note:** To avoid overriding deployment settings via the command line, update the [marquez.db](https://github.com/MarquezProject/marquez/blob/main/chart/values.yaml#L27) section of the Marquez Helm chart's `values.yaml` to include the AWS RDS `host`, `username`, and `password` in your deployment.

3. Verify all the pods have come up correctly:

   ```bash
   $ kubectl get pods --namespace marquez
   ```

##### UNINSTALLING MARQUEZ

```bash
helm uninstall marquez --namespace marquez
```
