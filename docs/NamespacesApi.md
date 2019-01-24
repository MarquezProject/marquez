# openapi_client.NamespacesApi

All URIs are relative to *http://localhost:5000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**namespaces_get**](NamespacesApi.md#namespaces_get) | **GET** /namespaces | List all namespaces
[**namespaces_namespace_get**](NamespacesApi.md#namespaces_namespace_get) | **GET** /namespaces/{namespace} | Retrieve a namespace
[**namespaces_namespace_put**](NamespacesApi.md#namespaces_namespace_put) | **PUT** /namespaces/{namespace} | Create a namespace


# **namespaces_get**
> Namespaces namespaces_get()

List all namespaces

Returns a list of namespaces.

### Example
```python
from __future__ import print_function
import time
import openapi_client
from openapi_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = openapi_client.NamespacesApi()

try:
    # List all namespaces
    api_response = api_instance.namespaces_get()
    pprint(api_response)
except ApiException as e:
    print("Exception when calling NamespacesApi->namespaces_get: %s\n" % e)
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Namespaces**](Namespaces.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **namespaces_namespace_get**
> Namespace namespaces_namespace_get(namespace)

Retrieve a namespace

Returns a namespace.

### Example
```python
from __future__ import print_function
import time
import openapi_client
from openapi_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = openapi_client.NamespacesApi()
namespace = wework # str | The name of the namespace. (default to 'wework')

try:
    # Retrieve a namespace
    api_response = api_instance.namespaces_namespace_get(namespace)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling NamespacesApi->namespaces_namespace_get: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **namespace** | **str**| The name of the namespace. | [default to &#39;wework&#39;]

### Return type

[**Namespace**](Namespace.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **namespaces_namespace_put**
> Namespace namespaces_namespace_put(namespace, inline_object=inline_object)

Create a namespace

Creates a new namespace object. A namespace enables the contextual grouping of related jobs and datasets. Namespaces must contain only letters (`a-z`, `A-Z`), numbers (`0-9`), or underscores (`_`). A namespace is case-insensitive with a maximum length of `1024` characters. Note jobs and datasets will be unique within a namespace, but not across namespaces.

### Example
```python
from __future__ import print_function
import time
import openapi_client
from openapi_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = openapi_client.NamespacesApi()
namespace = wework # str | The name of the namespace. (default to 'wework')
inline_object = openapi_client.InlineObject() # InlineObject |  (optional)

try:
    # Create a namespace
    api_response = api_instance.namespaces_namespace_put(namespace, inline_object=inline_object)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling NamespacesApi->namespaces_namespace_put: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **namespace** | **str**| The name of the namespace. | [default to &#39;wework&#39;]
 **inline_object** | [**InlineObject**](InlineObject.md)|  | [optional] 

### Return type

[**Namespace**](Namespace.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

