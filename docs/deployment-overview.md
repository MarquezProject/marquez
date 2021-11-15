---
layout: deployment-overview
---

# Deployment Overview

## Helm Chart

Marquez uses [Helm](https://helm.sh) to manage deployments onto [Kubernetes](https://kubernetes.io) in a cloud enviroment. The chart and templates are maintained in the Marquez [repository](https://github.com/MarquezProject/marquez) and can be found under [chart/](https://github.com/MarquezProject/marquez/tree/main/chart). The chart has a base `values.yaml` file, with the option to easily override deployment [settings](https://github.com/MarquezProject/marquez/tree/main/chart#configuration).

## Database

The Marquez [HTTP API](https://marquezproject.github.io/marquez/openapi.html) relys only on PostgreSQL to store dataset, job, and run metadata allowing for minimal operational overhead. We recommend a cloud provided databases, like AWS [RDS](https://aws.amazon.com/rds/postgresql), when deploying Marquez onto Kubernetes.

## Authentication

Our [clients](https://github.com/MarquezProject/marquez/tree/main/clients) support authentication by automatically sending an API key on each request via _Bearer Auth_ when configured on client Instantiation. By default, the Marquez HTTP API does not require any form of authentication or authorization.

## Next Steps

The following guides will help you and your team effectively deploy and manage Marquez in a cloud environment:

* [Running Marquez on AWS](running-on-aws.html)
