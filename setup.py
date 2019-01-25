import io
import setuptools

with io.open('README.md', encoding='utf-8') as f:
    long_description = f.read()

setuptools.setup(
    name="marquez-airflow",
    version="0.0.1",
    author="Author",
    author_email="author@example.com",
    description="Marquez integration with Airflow",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/MarquezProject/marquez-airflow",
    packages=setuptools.find_packages(),
    install_requires = [
        "apache-airflow>=1.10.0"
    ],
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: Apache Software License",
    ],
)
