# Marquez [Helm Chart](https://helm.sh)

Helm Chart for [Marquez](https://github.com/MarquezProject/marquez).

## TL;DR;

```bash
helm install marquez .
```

## Prerequisites

- Kubernetes 1.12+
- Helm 3.1.0

## Installing the Chart

To install the chart with the release name `marquez`:

```bash
helm install marquez .
```

> **Note:** For a list of parameters that can be overridden during installation, see the [configuration](#configuration) section.

## Uninstalling the Chart

To uninstall the `marquez ` deployment:

```bash
helm delete marquez
```

## Configuration

### [Marquez](https://github.com/MarquezProject/marquez) **parameters**

| Parameter                    | Description                      | Default                  |
|------------------------------|----------------------------------|--------------------------|
| `marquez.replicaCount`       | Number of desired replicas       | `1`                      |
| `marquez.image.registry`     | Marquez image registry           | `docker.io`              |
| `marquez.image.repository`   | Marquez image repository         | `marquezproject/marquez` |
| `marquez.image.tag`          | Marquez image tag                | `0.12.0`                 |
| `marquez.image.pullPolicy`   | Image pull policy                | `IfNotPresent`           |
| `marquez.db.host`            | PostgreSQL host                  | `localhost`              |
| `marquez.db.port`            | PostgreSQL port                  | `5432`                   |
| `marquez.db.name`            | PostgreSQL database              | `marquez`                |
| `marquez.db.user`            | PostgreSQL user                  | `buendia`                |
| `marquez.db.password`        | PostgreSQL password              | `macondo`                |
| `marquez.migrateOnStartup`   | Execute Flyway migration         | `true`                   |
| `marquez.hostname`           | Marquez hostname                 | `localhost`              |
| `marquez.port`               | API host port                    | `5000`                   |
| `marquez.adminPort`          | Heath/Liveness host port         | `5001`                   |
| `marquez.resources.limits`   | K8s resource limit overrides     | `nil`                    |
| `marquez.resources.requests` | K8s resource requests overrides  | `nil`                    |

### [Marquez Web UI](https://github.com/MarquezProject/marquez-web) **parameters**

| Parameter                | Description                     | Default        |
|--------------------------|---------------------------------|----------------|
| `web.enabled`            | Enables creation of Web UI      | `true`         |
| `web.replicaCount`       | Number of desired replicas      | `1`            |
| `web.image.registry`     | Marquez Web UI image registry   | `docker.io`    |
| `web.image.repository`   | Marquez Web UI image repository | `marquez-web`  |
| `web.image.tag`          | Marquez Web UI image tag        | `0.12.0`       |
| `web.image.pullPolicy`   | Image pull policy               | `IfNotPresent` |
| `web.port`               | Marquez Web host port           | `5000`         |
| `web.resources.limits`   | K8s resource limit overrides    | `nil`          |
| `web.resources.requests` | K8s resource requests overrides | `nil`          |

### Common **parameters**

| Parameter              | Description                         | Default |
|------------------------|-------------------------------------|---------|
| `global.imageRegistry` | Globally overrides image registry   | `nil`   |
| `commonLabels`         | Labels common to all resources      | `nil`   |
| `commonAnnotations`    | Annotations common to all resources | `nil`   |
| `affinity`             | Affinity for pod assignment         | `nil`   |
| `tolerations`          | Tolerations for pod assignment      | `nil`   |
| `nodeSelector`         | Node labels for pod assignment      | `nil`   |

### Service **parameters**

| Parameter             | Description                         | Default     |
|-----------------------|-------------------------------------|-------------|
| `service.type`        | Networking type of all services     | `ClusterIP` |
| `service.port`        | Port to expose services             | `80`        |
| `service.annotations` | Annotations applied to all services | `nil`       |

### Ingress **parameters**

| Parameter             | Description                        | Default |
|-----------------------|------------------------------------|---------|
| `ingress.enabled`     | Enables ingress settings           | `false` |
| `ingress.annotations` | Annotations applied to ingress     | `nil`   |
| `ingress.hosts`       | Hostname applied to ingress routes | `nil`   |
| `ingress.tls`         | TLS settings for hostname          | `nil`   |

## Local Installation Guide
If you do not have an existing Postgres database, you can run the following command to create
one using Docker. Contents of the ```./docker-compose-postgres..yml``` file can be customized
to better represent your target environment.

```bash
docker-compose -f ./docker-compose.postgres.yml -p marquez-postgres up
```

Once the Postgres instance has been created, run the following command to locate the IP
address of the database. Note you will need to un-escape the markdown.

```bash
marquez_db_ip=$(docker inspect marquez-postgres_db_1 -f '{{range.NetworkSettings.Networks}}{{.Gateway}}{{end}}')
```

Deploy via Helm and update database values as needed, either via
the `values.yaml` file or within the Helm CLI command. Again, remove the
pesky markdown escape character before running this command.

```bash
helm install marquez chart --set marquez.db.host=$marquez_db_ip
```

Once the Kubernetes pods and services have been installed (usually within 5-10 seconds), connectivity
tests can be executed by running the following Helm command. You should see a status message
of `Succeeded` for each test if the HTTP endpoints were reachable.

```bash
helm test marquez
```

If you haven't configured ingress within the Helm chart values, then you can use the
following port forwarding rules to support local development.

```bash
kubectl port-forward svc/marquez 5000:80
```

```bash
kubectl port-forward svc/marquez-web 3000:80
```

Once these rules are in place, you can view both the APIs and UI using the
links below.
* http://localhost:5000/api/v1/namespaces
* http://localhost:3000

### Troubleshooting
If things aren't working as expected, you can find out more by viewing the `kubectl` logs.
First, get the name of the pod that was installed by the Helm chart.

```bash
kubectl get pods
```

Plug this pod name into the following command, and it will display logs related
to the database migrations. This makes it simple to see errors dealing with
networking issues, credentials, etc.

```bash
kubectl logs -p <podName>
```

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-chart/blob/master/CONTRIBUTING.md) for more details about how to contribute.
