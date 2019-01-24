# openapi_client.DatasetsApi

All URIs are relative to *http://localhost:5000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**namespaces_namespace_datasets_get**](DatasetsApi.md#namespaces_namespace_datasets_get) | **GET** /namespaces/{namespace}/datasets | List all datasets


# **namespaces_namespace_datasets_get**
> Datasets namespaces_namespace_datasets_get(namespace)

List all datasets

Returns a list of datasets.

### Example
```python
from __future__ import print_function
import time
import openapi_client
from openapi_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = openapi_client.DatasetsApi()
namespace = wework # str | The name of the namespace. (default to 'wework')

try:
    # List all datasets
    api_response = api_instance.namespaces_namespace_datasets_get(namespace)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling DatasetsApi->namespaces_namespace_datasets_get: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **namespace** | **str**| The name of the namespace. | [default to &#39;wework&#39;]

### Return type

[**Datasets**](Datasets.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

