# marquez_client.JobsApi

All URIs are relative to *http://localhost:5000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**jobs_runs_id_abort_put**](JobsApi.md#jobs_runs_id_abort_put) | **PUT** /jobs/runs/{id}/abort | Abort a job run
[**jobs_runs_id_complete_put**](JobsApi.md#jobs_runs_id_complete_put) | **PUT** /jobs/runs/{id}/complete | Complete a job run
[**jobs_runs_id_fail_put**](JobsApi.md#jobs_runs_id_fail_put) | **PUT** /jobs/runs/{id}/fail | Fail a job run
[**jobs_runs_id_get**](JobsApi.md#jobs_runs_id_get) | **GET** /jobs/runs/{id} | Retrieve a job run
[**jobs_runs_id_outputs_get**](JobsApi.md#jobs_runs_id_outputs_get) | **GET** /jobs/runs/{id}/outputs | List all job run outputs
[**jobs_runs_id_outputs_put**](JobsApi.md#jobs_runs_id_outputs_put) | **PUT** /jobs/runs/{id}/outputs | Create multiple output datasets
[**jobs_runs_id_run_put**](JobsApi.md#jobs_runs_id_run_put) | **PUT** /jobs/runs/{id}/run | Start a job run
[**namespaces_namespace_jobs_get**](JobsApi.md#namespaces_namespace_jobs_get) | **GET** /namespaces/{namespace}/jobs | List all jobs
[**namespaces_namespace_jobs_job_get**](JobsApi.md#namespaces_namespace_jobs_job_get) | **GET** /namespaces/{namespace}/jobs/{job} | Retrieve a job
[**namespaces_namespace_jobs_job_put**](JobsApi.md#namespaces_namespace_jobs_job_put) | **PUT** /namespaces/{namespace}/jobs/{job} | Create a job
[**namespaces_namespace_jobs_job_runs_get**](JobsApi.md#namespaces_namespace_jobs_job_runs_get) | **GET** /namespaces/{namespace}/jobs/{job}/runs | List all job runs
[**namespaces_namespace_jobs_job_runs_post**](JobsApi.md#namespaces_namespace_jobs_job_runs_post) | **POST** /namespaces/{namespace}/jobs/{job}/runs | Create a job run
[**namespaces_namespace_jobs_job_versions_get**](JobsApi.md#namespaces_namespace_jobs_job_versions_get) | **GET** /namespaces/{namespace}/jobs/{job}/versions | List all job versions


# **jobs_runs_id_abort_put**
> jobs_runs_id_abort_put(id)

Abort a job run

Marks the job run as aborted.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
id = 'id_example' # str | The unique ID of the job run.

try:
    # Abort a job run
    api_instance.jobs_runs_id_abort_put(id)
except ApiException as e:
    print("Exception when calling JobsApi->jobs_runs_id_abort_put: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | [**str**](.md)| The unique ID of the job run. | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **jobs_runs_id_complete_put**
> jobs_runs_id_complete_put(id)

Complete a job run

Marks the job run as completed.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
id = 'id_example' # str | The unique ID of the job run.

try:
    # Complete a job run
    api_instance.jobs_runs_id_complete_put(id)
except ApiException as e:
    print("Exception when calling JobsApi->jobs_runs_id_complete_put: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | [**str**](.md)| The unique ID of the job run. | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **jobs_runs_id_fail_put**
> jobs_runs_id_fail_put(id)

Fail a job run

Marks the job run as failed.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
id = 'id_example' # str | The unique ID of the job run.

try:
    # Fail a job run
    api_instance.jobs_runs_id_fail_put(id)
except ApiException as e:
    print("Exception when calling JobsApi->jobs_runs_id_fail_put: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | [**str**](.md)| The unique ID of the job run. | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **jobs_runs_id_get**
> JobRun jobs_runs_id_get(id)

Retrieve a job run

Retrieve a job run.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
id = 'id_example' # str | The unique ID of the job run.

try:
    # Retrieve a job run
    api_response = api_instance.jobs_runs_id_get(id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling JobsApi->jobs_runs_id_get: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | [**str**](.md)| The unique ID of the job run. | 

### Return type

[**JobRun**](JobRun.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **jobs_runs_id_outputs_get**
> Datasets jobs_runs_id_outputs_get(id)

List all job run outputs

Returns a list job run outputs.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
id = 'id_example' # str | The unique ID of the job run.

try:
    # List all job run outputs
    api_response = api_instance.jobs_runs_id_outputs_get(id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling JobsApi->jobs_runs_id_outputs_get: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | [**str**](.md)| The unique ID of the job run. | 

### Return type

[**Datasets**](Datasets.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **jobs_runs_id_outputs_put**
> Datasets jobs_runs_id_outputs_put(id, job_run_outputs=job_run_outputs)

Create multiple output datasets

Creates a multiple output dataset objects.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
id = 'id_example' # str | The unique ID of the job run.
job_run_outputs = marquez_client.JobRunOutputs() # JobRunOutputs |  (optional)

try:
    # Create multiple output datasets
    api_response = api_instance.jobs_runs_id_outputs_put(id, job_run_outputs=job_run_outputs)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling JobsApi->jobs_runs_id_outputs_put: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | [**str**](.md)| The unique ID of the job run. | 
 **job_run_outputs** | [**JobRunOutputs**](JobRunOutputs.md)|  | [optional] 

### Return type

[**Datasets**](Datasets.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **jobs_runs_id_run_put**
> jobs_runs_id_run_put(id)

Start a job run

Marks the job run as running.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
id = 'id_example' # str | The unique ID of the job run.

try:
    # Start a job run
    api_instance.jobs_runs_id_run_put(id)
except ApiException as e:
    print("Exception when calling JobsApi->jobs_runs_id_run_put: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | [**str**](.md)| The unique ID of the job run. | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **namespaces_namespace_jobs_get**
> Jobs namespaces_namespace_jobs_get(namespace)

List all jobs

Returns a list of jobs.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
namespace = wework # str | The name of the namespace. (default to 'wework')

try:
    # List all jobs
    api_response = api_instance.namespaces_namespace_jobs_get(namespace)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling JobsApi->namespaces_namespace_jobs_get: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **namespace** | **str**| The name of the namespace. | [default to &#39;wework&#39;]

### Return type

[**Jobs**](Jobs.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **namespaces_namespace_jobs_job_get**
> Job namespaces_namespace_jobs_job_get(namespace, job)

Retrieve a job

Retrieve a job.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
namespace = wework # str | The name of the namespace. (default to 'wework')
job = room_bookings_7_days # str | The name of the job. (default to 'room_bookings_7_days')

try:
    # Retrieve a job
    api_response = api_instance.namespaces_namespace_jobs_job_get(namespace, job)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling JobsApi->namespaces_namespace_jobs_job_get: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **namespace** | **str**| The name of the namespace. | [default to &#39;wework&#39;]
 **job** | **str**| The name of the job. | [default to &#39;room_bookings_7_days&#39;]

### Return type

[**Job**](Job.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **namespaces_namespace_jobs_job_put**
> Job namespaces_namespace_jobs_job_put(namespace, job, inline_object1=inline_object1)

Create a job

Creates a new job object. All job objects are immutable and are uniquely identified by a generated ID. Marquez will create a version of a job each time the contents of the object is modified. For example, the `location` of a job may change over time resulting in new versions. The accumulated versions can be listed, used to rerun a specific job version or possibly help debug a failed job run.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
namespace = wework # str | The name of the namespace. (default to 'wework')
job = room_bookings_7_days # str | The name of the job. (default to 'room_bookings_7_days')
inline_object1 = marquez_client.InlineObject1() # InlineObject1 |  (optional)

try:
    # Create a job
    api_response = api_instance.namespaces_namespace_jobs_job_put(namespace, job, inline_object1=inline_object1)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling JobsApi->namespaces_namespace_jobs_job_put: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **namespace** | **str**| The name of the namespace. | [default to &#39;wework&#39;]
 **job** | **str**| The name of the job. | [default to &#39;room_bookings_7_days&#39;]
 **inline_object1** | [**InlineObject1**](InlineObject1.md)|  | [optional] 

### Return type

[**Job**](Job.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **namespaces_namespace_jobs_job_runs_get**
> JobRuns namespaces_namespace_jobs_job_runs_get(namespace, job)

List all job runs

Returns a list of job runs.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
namespace = wework # str | The name of the namespace. (default to 'wework')
job = room_bookings_7_days # str | The name of the job. (default to 'room_bookings_7_days')

try:
    # List all job runs
    api_response = api_instance.namespaces_namespace_jobs_job_runs_get(namespace, job)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling JobsApi->namespaces_namespace_jobs_job_runs_get: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **namespace** | **str**| The name of the namespace. | [default to &#39;wework&#39;]
 **job** | **str**| The name of the job. | [default to &#39;room_bookings_7_days&#39;]

### Return type

[**JobRuns**](JobRuns.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **namespaces_namespace_jobs_job_runs_post**
> JobRunId namespaces_namespace_jobs_job_runs_post(namespace, job, inline_object2=inline_object2)

Create a job run

Creates a new job run object.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
namespace = wework # str | The name of the namespace. (default to 'wework')
job = room_bookings_7_days # str | The name of the job. (default to 'room_bookings_7_days')
inline_object2 = marquez_client.InlineObject2() # InlineObject2 |  (optional)

try:
    # Create a job run
    api_response = api_instance.namespaces_namespace_jobs_job_runs_post(namespace, job, inline_object2=inline_object2)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling JobsApi->namespaces_namespace_jobs_job_runs_post: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **namespace** | **str**| The name of the namespace. | [default to &#39;wework&#39;]
 **job** | **str**| The name of the job. | [default to &#39;room_bookings_7_days&#39;]
 **inline_object2** | [**InlineObject2**](InlineObject2.md)|  | [optional] 

### Return type

[**JobRunId**](JobRunId.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **namespaces_namespace_jobs_job_versions_get**
> JobVersions namespaces_namespace_jobs_job_versions_get(namespace, job)

List all job versions

Returns a list of job versions.

### Example
```python
from __future__ import print_function
import time
import marquez_client
from marquez_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = marquez_client.JobsApi()
namespace = wework # str | The name of the namespace. (default to 'wework')
job = room_bookings_7_days # str | The name of the job. (default to 'room_bookings_7_days')

try:
    # List all job versions
    api_response = api_instance.namespaces_namespace_jobs_job_versions_get(namespace, job)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling JobsApi->namespaces_namespace_jobs_job_versions_get: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **namespace** | **str**| The name of the namespace. | [default to &#39;wework&#39;]
 **job** | **str**| The name of the job. | [default to &#39;room_bookings_7_days&#39;]

### Return type

[**JobVersions**](JobVersions.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

