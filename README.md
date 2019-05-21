[![CircleCI](https://circleci.com/gh/MarquezProject/marquez-python/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez-python/tree/master) [![codecov](https://codecov.io/gh/MarquezProject/marquez-python/branch/master/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez-python/branch/master) [![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status) [![version](https://img.shields.io/pypi/v/marquez-python.svg)](https://pypi.python.org/pypi/marquez-python) [![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-python/master/LICENSE)



# marquez-python

Marquez-Python is an open source client library package for python that users can use to help build applications that integrate with Marquez.

Marquez is an open source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata.
The marquez-python package is a library that interacts with a running instance of the Marquez server.


# Status
This project is under active development at WeWork. The latest published version can be found [here](https://pypi.org/project/marquez-python/). 

## Requirements.

Python 3.5.0+

## Installation & Usage
### pip install

Marquez-Python is hosted on PyPi and can be installed from that repository.

```sh
pip install marquez-python
```
(you may need to run `pip` with root permission: `sudo pip install marquez-python`)

Then import the package:
```python
from marquez_client import MarquezClient
```

### Setuptools

Install via [Setuptools](http://pypi.python.org/pypi/setuptools).

```sh
python setup.py install --user
```
(or `sudo python setup.py install` to install the package for all users)

Then import the package:
```python

from marquez_client import MarquezClient
```

## Getting Started

Please follow the [installation procedure](#installation--usage) and then run the following:

Please be sure to set the environmental variables to connect with Marquez:
```
export MARQUEZ_HOST='localhost'
export MARQUEZ_PORT='8080'
export MARQUEZ_NAMESPACE='accounting_team'
export MARQUEZ_TIMEOUT_MS=5000
```

### Common Calls and Examples
There are two main ways to use the client:
1. Set the namespace
2. Supply the namespace for calls that require it

For more information about API calls, please see the Marquez API documentation.

#### Instantiating the client
```
mc = MarquezClient(host="localhost", port="8080")
```

#### Instantiating the client to use a default existing namespace
```
mc = MarquezClient(
    host="localhost",
    port="8080",
    namespace_name="accounting_team")
```

Alternatively, a user can use the environmental variable.
```
$> export MARQUEZ_NAMESPACE='accounting_team'
```
The constructor-specified value will take precedence over
the environmental variable. If neither is provided, the default
namespace `default` is used. 
```
mc = MarquezClient(
    host="localhost",
    port="8080")
```
When making function calls that can optionally specify
a namespace, omitting it will use the client's namespace. Specifying the namespace name
will use that name for the request, regardless of what is set for the client.
See [Create a Job](#Create-a-Job). 

#### Create a Namespace
``` 
mc.create_namespace(name="accounting_team",
    owner="Gabriel G. Marquez",
    description="Jobs and data sets related to accounting.")

```

#### Create a Job
```
mc.create_job(job_name="quarterly_summary",
    location="accounting_summary.py",
    description="generates the quarterly accounting report",
    input_dataset_urns=["urn:dataset:accounting_db:monthly_rollups_q2_2019"],
    output_dataset_urns=["urn:dataset:accounting_db:quarterly_reports_q2_2019"],
    namespace_name="accounting_team")
```

#### Create a JobRun
```
quarterly_job_run = mc.create_job_run(
    job_name="quarterly_summary",
    job_run_args="{\"email\":\"accounting@wework.com\"}")
```

### Output Format
The output format will just be a dictionary containing key-value pairs.
Example output from a job run:
```
quarterly_job_info = mc.get_job(
    job_name="quarterly_summary",
    namespace_name="accounting_team")
```
Value of `quarterly_job_info`:
```
{
    "runId": "870492da-ecfb-4be0-91b9-9a89ddd3db90",
    "runState": "COMPLETED",
    "runArgs": "{\"email\": \"accounting@wework.com\"}"
}
```

## Documentation For Authorization

 All endpoints do not require authorization.





