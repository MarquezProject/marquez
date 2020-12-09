FROM bitnami/airflow-scheduler:1.10.12 AS scheduler
USER root
RUN apt-get update && \
    apt-get install -y git
USER 1001

FROM bitnami/airflow-worker:1.10.12 AS worker
USER root
RUN apt-get update && \
    apt-get install -y git
USER 1001

FROM bitnami/airflow:1.10.12 AS airflow
USER root
RUN apt-get update && \
    apt-get install -y git
USER 1001
