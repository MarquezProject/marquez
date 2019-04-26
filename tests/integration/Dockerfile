FROM puckel/docker-airflow:1.10.2
USER root

RUN pip install pytest
COPY . /marquez-airflow/

RUN cd /marquez-airflow && \
    python setup.py sdist && \
    pip install .

COPY ./tests/integration/integration_test.py .
CMD python -m pytest ./integration_test.py
