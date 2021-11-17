---
layout: deployment-overview
---

# Deployment Overview

## Helm Chart

Marquez uses [Helm](https://helm.sh) to manage deployments onto [Kubernetes](https://kubernetes.io) in a cloud enviroment. The chart and templates for the [HTTP API](https://github.com/MarquezProject/marquez/tree/main/api) server and [Web UI](https://github.com/MarquezProject/marquez/tree/main/web) are maintained in the Marquez [repository](https://github.com/MarquezProject/marquez) and can be found under [chart/](https://github.com/MarquezProject/marquez/tree/main/chart). The chart has a base `values.yaml` file, with the option to easily override deployment [settings](https://github.com/MarquezProject/marquez/tree/main/chart#configuration).

> **Note**: The Marquez HTTP API server and Web UI images are publshed to [DockerHub](https://hub.docker.com/r/marquezproject/marquez).

## Database

The Marquez [HTTP API](https://marquezproject.github.io/marquez/openapi.html) relys only on PostgreSQL to store dataset, job, and run metadata allowing for minimal operational overhead. We recommend a cloud provided databases, like AWS [RDS](https://aws.amazon.com/rds/postgresql), when deploying Marquez onto Kubernetes.

## Authentication

Our [clients](https://github.com/MarquezProject/marquez/tree/main/clients) support authentication by automatically sending an API key on each request via [_Bearer Auth_](https://datatracker.ietf.org/doc/html/rfc6750) when configured on client instantiation. By default, the Marquez HTTP API does not require any form of authentication or authorization.

## Next Steps

The following guides will help you and your team effectively deploy and manage Marquez in a cloud environment:

* [Running Marquez on AWS](running-on-aws.html)
