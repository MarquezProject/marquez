import setuptools

with open("README.md", "r") as f:
    long_description = f.read()

setuptools.setup(
    name="marquez-airflow",
    version="0.0.2",
    author="Marquez Team",
    author_email="",
    description="Marquez integration with Airflow",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/MarquezProject/marquez-airflow",
    packages=setuptools.find_packages(),
    install_requires = [
        "marquez-python>=1.2.1",
        "apache-airflow>=1.9.0"
    ],
)
