# Marquez [Helm Chart](https://helm.sh)

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/marquez-project/community)
[![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-chart/master/LICENSE)

Helm Chart for [Marquez](https://github.com/MarquezProject/marquez).

## TL;DR;

```bash
$ helm install marquez .
```

## Installing the Chart

To install the chart with the release name `my-release`:

```bash
$ helm install marquez .
```

> **Note:** For a list of parameters that can be overridden during installation, see the [configuration](#configuration) section.

## Uninstalling the Chart

To uninstall the `marquez ` deployment:

```bash
$ helm delete marquez
```

## Configuration

### [Marquez](https://github.com/MarquezProject/marquez) **parameters**

| Parameter                  | Description                      | Default                  |
|----------------------------|----------------------------------|--------------------------|
| `marquez.image.repository` | Marquez image                    | `marquezproject/marquez` |
| `marquez.image.tag`        | Marquez image tag                | `0.10.4`                 |
| `marquez.image.pullPolicy` | Image pull policy                | `IfNotPresent`           |
| `marquez.replicaCount`     | Number of desired replicas       | `1`                      |
| `marquez.db.host`          | PostgreSQL host                  | `nil`                    |
| `marquez.db.port`          | PostgreSQL port                  | `5432`                   |
| `marquez.db.name`          | PostgreSQL database              | `marquez`                |
| `marquez.db.user`          | PostgreSQL user                  | `buendia`                |
| `marquez.db.password`      | PostgreSQL password              | `macondo `               |

### [Marquez Web UI](https://github.com/MarquezProject/marquez-web) **parameters**

| Parameter              | Description                   | Default        |
|------------------------|-------------------------------|----------------|
| `web.image.repository` | Marquez Web UI image          | `marquez-web`  |
| `web.image.tag`        | Marquez Web UI image tag      | `0.3.0`.       |
| `web.image.pullPolicy` | Image pull policy             | `IfNotPresent` |
| `web.replicaCount`     | Number of desired replicas    | `1`            |

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-chart/blob/master/CONTRIBUTING.md) for more details about how to contribute.