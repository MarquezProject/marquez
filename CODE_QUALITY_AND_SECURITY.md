# Code Quality and Security Assurance Statement

The authors of Marquez are committed to providing secure software of the highest quality possible. To this end, we employ a number of tools and methodologies to ensure that our design, build, maintenance and testing practices maximize efficiency and minimize risk.

The specific security and analysis methodologies that we employ include but are not limited to:

## Security

- Participation in the [OpenSSF Best Practices Badge Program](https://bestpractices.coreinfrastructure.org/en/projects/5106) for Free/Libre and FLOSS projects to ensure that we follow current best practices for quality and security
- Use of [HTTPS](https://en.wikipedia.org/wiki/HTTPS) for network communication
- Support for multiple cryptographic algorithms (through the use of HTTPS)
- Separate storage of authentication credentials according to best practices
- Use of secure protocols for network communication (through the use of HTTPS)
- Up-to-date support for TLS/SSL (through the use of [OpenSSL](https://www.openssl.org/))
- Performance of TLS certificate verification by default before sending HTTP headers with private information (through the use of OpenSSL and HTTPS)
- Distribution of the software via cryptographically signed releases (on the [PyPI](https://pypi.org/) and [Maven](https://mvnrepository.com/) package repositories)
- Use of [GitHub](https://github.com/) Issues for vulnerability reporting and tracking

## Analysis

- Use of [PMD](https://pmd.github.io/) and [Spotless](https://github.com/diffplug/spotless) for Java code linting on pull requests and builds
- Use of [Flake8](https://flake8.pycqa.org/en/latest/) and [Pytest](https://docs.pytest.org/en/7.2.x/) for Python code linting on pull requests and builds
- Use of GitHub Issues for bug reporting and tracking

## Contact

For more information about our approach to quality and security, feel free to reach out to the Marquez development team:

- Slack: [Marquezproject.slack.com](https://join.slack.com/t/marquezproject/shared_invite/zt-29w4n8y45-Re3B1KTlZU5wO6X6JRzGmA)
- Twitter: [@MarquezProject](https://twitter.com/MarquezProject)

----
SPDX-License-Identifier: Apache-2.0
Copyright 2018-2023 contributors to the Marquez project.
