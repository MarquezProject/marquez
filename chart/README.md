# Marquez [Helm Chart](https://helm.sh)

Helm Chart for [Marquez](https://github.com/MarquezProject/marquez).

## TL;DR;

```bash
cd /path/to/marquez/chart
helm install marquez . --dependency-update
```

## Installing the Chart

To install the chart with the release name `marquez`:

```bash
helm install marquez . --dependency-update
```

> **Note:** For a list of parameters that can be overridden during installation, see the [configuration](#configuration) section.

## Uninstalling the Chart

To uninstall the `marquez ` deployment:

```bash
helm delete marquez
```

## Configuration

### Global parameters

| Parameter              | Description                       | Default |
|------------------------|-----------------------------------|---------|
| `global.imageRegistry` | Global Docker image registry      | `""`   |

### Common parameters

| Parameter              | Description                         | Default |
|------------------------|-------------------------------------|---------|
| `commonLabels`         | Labels common to all resources      | `{}`    |
| `commonAnnotations`    | Annotations common to all resources | `{}`    |

### [Marquez API](https://github.com/MarquezProject/marquez) **parameters**

| Parameter                     | Description                                                           | Default                  |
|-------------------------------|-----------------------------------------------------------------------|--------------------------|
| `marquez.image.registry`      | Marquez image registry                                                | `docker.io`              |
| `marquez.image.repository`    | Marquez image repository                                              | `marquezproject/marquez` |
| `marquez.image.tag`           | Marquez image tag                                                     | `0.20.0`                 |
| `marquez.image.pullPolicy`    | Marquez image pull policy                                             | `IfNotPresent`           |
| `marquez.containerPort`       | The port Marquez is running on                                        | `5000`                   |
| `marquez.containerAdminPort`  | The port the Marquez Admin UI is running on                           | `5001`                   |
| `marquez.servicePort`         | The port the service is running on (redirects to `api.containerPort`) | `5000`                   |
| `marquez.replicaCount`        | Number of desired replicas                                            | `1`                      |
| `marquez.existingSecretName`  | Marquez existing secret                                               | `nil`                    |
| `marquez.db.host`             | PostgreSQL host                                                       | `localhost`              |
| `marquez.db.port`             | PostgreSQL port                                                       | `5432`                   |
| `marquez.db.name`             | PostgreSQL database                                                   | `marquez`                |
| `marquez.db.user`             | PostgreSQL user                                                       | `buendia`                |
| `marquez.db.password`         | PostgreSQL password                                                   | `macondo`                |
| `marquez.migrateOnStartup`    | Execute Flyway migration                                              | `true`                   |
| `marquez.resources.limits`    | K8s resource limit overrides                                          | `nil`                    |
| `marquez.resources.requests`  | K8s resource requests overrides                                       | `nil`                    |
| `marquez.logLevel`            | Marquez log level                                                     | `INFO`                   |
| `marquez.javaOpts`            | JVM options                                                           | `-Xmx400m`               |
| `marquez.ingress.enabled`     | Enables ingress settings                                              | `false`                  |
| `marquez.ingress.annotations` | Annotations applied to ingress                                        | `nil`                    |
| `marquez.ingress.hosts`       | Hostname applied to ingress routes                                    | `nil`                    |
| `marquez.ingress.tls`         | TLS settings for hostname                                             | `nil`                    |

### [Marquez Web UI](https://github.com/MarquezProject/marquez-web) **parameters**

| Parameter                 | Description                     | Default                      |
|---------------------------|---------------------------------|------------------------------|
| `web.enabled`             | Enables creation of Web UI      | `true`                       |
| `web.replicaCount`        | Number of desired replicas      | `1`                          |
| `web.image.registry`      | Marquez Web UI image registry   | `docker.io`                  |
| `web.image.repository`    | Marquez Web UI image repository | `marquezproject/marquez-web` |
| `web.image.tag`           | Marquez Web UI image tag        | `0.15.0`                     |
| `web.image.pullPolicy`    | Image pull policy               | `IfNotPresent`               |
| `web.port`                | Marquez Web host port           | `5000`                       |
| `web.resources.limits`    | K8s resource limit overrides    | `nil`                        |
| `web.resources.requests`  | K8s resource requests overrides | `nil`                        |
| `web.ingress.enabled`     | Enables ingress settings           | `false`                   |
| `web.ingress.annotations` | Annotations applied to ingress     | `nil`                     |
| `web.ingress.hosts`       | Hostname applied to ingress routes | `nil`                     |
| `web.ingress.tls`         | TLS settings for hostname          | `nil`                     |
