// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { JobOrDataset } from '../../components/lineage/types'
import { generateNodeId } from '../../helpers/nodes'
import { genericFetchWrapper } from './index'

export const getLineage = async (
  nodeType: JobOrDataset,
  namespace: string,
  name: string,
  depth: number
) => {
  const nodeId = generateNodeId(nodeType, namespace, name)
  // Node ID cannot be URL encoded
  const url = `${API_URL}/lineage?nodeId=${nodeId}&depth=${depth}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchLineage')
}

// TODO remove this and fix the CORS issue
export const mockCall = JSON.parse(`{
	"graph": [{
		"id": "dataset:hohr:campaigns_sendinblue",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "campaigns_sendinblue"
			},
			"name": "campaigns_sendinblue",
			"physicalName": "campaigns_sendinblue",
			"createdAt": "2023-04-07T08:30:16.454242Z",
			"updatedAt": "2023-04-12T09:47:18.321507Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [{
			"origin": "job:hohr:acquire_campaigns",
			"destination": "dataset:hohr:campaigns_sendinblue"
		}],
		"outEdges": [{
			"origin": "dataset:hohr:campaigns_sendinblue",
			"destination": "job:hohr:acquire_sending_list"
		}]
	}, {
		"id": "dataset:hohr:campaigns_sendinblue_candidates",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "campaigns_sendinblue_candidates"
			},
			"name": "campaigns_sendinblue_candidates",
			"physicalName": "campaigns_sendinblue_candidates",
			"createdAt": "2023-04-12T15:30:19.105448Z",
			"updatedAt": "2023-06-09T08:41:51.536150Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [{
			"origin": "job:hohr:acquire_campaigns_candidates",
			"destination": "dataset:hohr:campaigns_sendinblue_candidates"
		}],
		"outEdges": [{
			"origin": "dataset:hohr:campaigns_sendinblue_candidates",
			"destination": "job:hohr:acquire_sending_list_candidates"
		}]
	}, {
		"id": "dataset:hohr:campaigns_sendinblue_customers",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "campaigns_sendinblue_customers"
			},
			"name": "campaigns_sendinblue_customers",
			"physicalName": "campaigns_sendinblue_customers",
			"createdAt": "2023-04-12T15:30:12.918149Z",
			"updatedAt": "2023-06-09T08:41:51.536150Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [{
			"origin": "job:hohr:acquire_campaigns_customers",
			"destination": "dataset:hohr:campaigns_sendinblue_customers"
		}],
		"outEdges": [{
			"origin": "dataset:hohr:campaigns_sendinblue_customers",
			"destination": "job:hohr:acquire_sending_list_customers"
		}]
	}, {
		"id": "dataset:hohr:master_senders",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "master_senders"
			},
			"name": "master_senders",
			"physicalName": "master_senders",
			"createdAt": "2023-04-07T09:42:29.759999Z",
			"updatedAt": "2023-06-09T08:43:18.308503Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [{
			"origin": "job:hohr:gather_senders",
			"destination": "dataset:hohr:master_senders"
		}],
		"outEdges": [{
			"origin": "dataset:hohr:master_senders",
			"destination": "job:hohr:export_master_senders_gbq"
		}]
	}, {
		"id": "dataset:hohr:master_senders_gbq",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "master_senders_gbq"
			},
			"name": "master_senders_gbq",
			"physicalName": "master_senders_gbq",
			"createdAt": "2023-04-07T09:43:41.400564Z",
			"updatedAt": "2023-06-09T08:43:18.677343Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [{
			"origin": "job:hohr:export_master_senders_gbq",
			"destination": "dataset:hohr:master_senders_gbq"
		}],
		"outEdges": []
	}, {
		"id": "dataset:hohr:params:campaigns.end_date",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "params:campaigns.end_date"
			},
			"name": "params:campaigns.end_date",
			"physicalName": "params:campaigns.end_date",
			"createdAt": "2023-04-07T08:30:16.454242Z",
			"updatedAt": "2023-06-09T08:32:59.025465Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [],
		"outEdges": [{
			"origin": "dataset:hohr:params:campaigns.end_date",
			"destination": "job:hohr:acquire_campaigns"
		}, {
			"origin": "dataset:hohr:params:campaigns.end_date",
			"destination": "job:hohr:acquire_campaigns_candidates"
		}, {
			"origin": "dataset:hohr:params:campaigns.end_date",
			"destination": "job:hohr:acquire_campaigns_customers"
		}, {
			"origin": "dataset:hohr:params:campaigns.end_date",
			"destination": "job:hohr:acquire_templates_customers"
		}]
	}, {
		"id": "dataset:hohr:params:campaigns.mode.candidates",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "params:campaigns.mode.candidates"
			},
			"name": "params:campaigns.mode.candidates",
			"physicalName": "params:campaigns.mode.candidates",
			"createdAt": "2023-04-12T15:30:19.105448Z",
			"updatedAt": "2023-06-09T08:35:36.725893Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [],
		"outEdges": [{
			"origin": "dataset:hohr:params:campaigns.mode.candidates",
			"destination": "job:hohr:acquire_campaigns_candidates"
		}, {
			"origin": "dataset:hohr:params:campaigns.mode.candidates",
			"destination": "job:hohr:acquire_sending_list_candidates"
		}]
	}, {
		"id": "dataset:hohr:params:campaigns.mode.customers",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "params:campaigns.mode.customers"
			},
			"name": "params:campaigns.mode.customers",
			"physicalName": "params:campaigns.mode.customers",
			"createdAt": "2023-04-12T15:30:12.918149Z",
			"updatedAt": "2023-06-09T08:33:47.073402Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [],
		"outEdges": [{
			"origin": "dataset:hohr:params:campaigns.mode.customers",
			"destination": "job:hohr:acquire_campaigns_customers"
		}, {
			"origin": "dataset:hohr:params:campaigns.mode.customers",
			"destination": "job:hohr:acquire_sending_list_customers"
		}, {
			"origin": "dataset:hohr:params:campaigns.mode.customers",
			"destination": "job:hohr:acquire_templates_customers"
		}]
	}, {
		"id": "dataset:hohr:params:campaigns.start_date",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "params:campaigns.start_date"
			},
			"name": "params:campaigns.start_date",
			"physicalName": "params:campaigns.start_date",
			"createdAt": "2023-04-07T08:30:16.454242Z",
			"updatedAt": "2023-06-09T08:32:59.025465Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [],
		"outEdges": [{
			"origin": "dataset:hohr:params:campaigns.start_date",
			"destination": "job:hohr:acquire_campaigns"
		}, {
			"origin": "dataset:hohr:params:campaigns.start_date",
			"destination": "job:hohr:acquire_campaigns_candidates"
		}, {
			"origin": "dataset:hohr:params:campaigns.start_date",
			"destination": "job:hohr:acquire_campaigns_customers"
		}, {
			"origin": "dataset:hohr:params:campaigns.start_date",
			"destination": "job:hohr:acquire_templates_customers"
		}]
	}, {
		"id": "dataset:hohr:senders_sendinblue",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "senders_sendinblue"
			},
			"name": "senders_sendinblue",
			"physicalName": "senders_sendinblue",
			"createdAt": "2023-04-07T08:38:11.473495Z",
			"updatedAt": "2023-04-12T09:47:24.942439Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [{
			"origin": "job:hohr:acquire_sending_list",
			"destination": "dataset:hohr:senders_sendinblue"
		}],
		"outEdges": []
	}, {
		"id": "dataset:hohr:senders_sendinblue_candidates",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "senders_sendinblue_candidates"
			},
			"name": "senders_sendinblue_candidates",
			"physicalName": "senders_sendinblue_candidates",
			"createdAt": "2023-04-12T18:56:04.779642Z",
			"updatedAt": "2023-06-09T08:41:45.020481Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [{
			"origin": "job:hohr:acquire_sending_list_candidates",
			"destination": "dataset:hohr:senders_sendinblue_candidates"
		}],
		"outEdges": [{
			"origin": "dataset:hohr:senders_sendinblue_candidates",
			"destination": "job:hohr:gather_senders"
		}]
	}, {
		"id": "dataset:hohr:senders_sendinblue_customers",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "senders_sendinblue_customers"
			},
			"name": "senders_sendinblue_customers",
			"physicalName": "senders_sendinblue_customers",
			"createdAt": "2023-04-12T19:30:52.508848Z",
			"updatedAt": "2023-06-09T08:41:45.020481Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [{
			"origin": "job:hohr:acquire_sending_list_customers",
			"destination": "dataset:hohr:senders_sendinblue_customers"
		}],
		"outEdges": [{
			"origin": "dataset:hohr:senders_sendinblue_customers",
			"destination": "job:hohr:gather_senders"
		}]
	}, {
		"id": "dataset:hohr:templates_sendinblue_customers",
		"type": "DATASET",
		"data": {
			"type": "DB_TABLE",
			"id": {
				"namespace": "hohr",
				"name": "templates_sendinblue_customers"
			},
			"name": "templates_sendinblue_customers",
			"physicalName": "templates_sendinblue_customers",
			"createdAt": "2023-04-12T15:36:07.505082Z",
			"updatedAt": "2023-06-09T08:32:59.173734Z",
			"namespace": "hohr",
			"sourceName": "default",
			"fields": [],
			"tags": [],
			"lastModifiedAt": null,
			"description": null,
			"lastLifecycleState": ""
		},
		"inEdges": [{
			"origin": "job:hohr:acquire_templates_customers",
			"destination": "dataset:hohr:templates_sendinblue_customers"
		}],
		"outEdges": []
	}, {
		"id": "job:hohr:acquire_campaigns",
		"type": "JOB",
		"data": {
			"type": "BATCH",
			"id": {
				"namespace": "hohr",
				"name": "acquire_campaigns"
			},
			"name": "acquire_campaigns",
			"simpleName": "acquire_campaigns",
			"parentJobName": null,
			"createdAt": "2023-04-07T08:30:24.446777Z",
			"updatedAt": "2023-04-07T08:30:24.446777Z",
			"namespace": "hohr",
			"inputs": [{
				"namespace": "hohr",
				"name": "params:campaigns.end_date"
			}, {
				"namespace": "hohr",
				"name": "params:campaigns.start_date"
			}],
			"outputs": [{
				"namespace": "hohr",
				"name": "campaigns_sendinblue"
			}],
			"location": null,
			"description": null,
			"latestRun": null
		},
		"inEdges": [{
			"origin": "dataset:hohr:params:campaigns.end_date",
			"destination": "job:hohr:acquire_campaigns"
		}, {
			"origin": "dataset:hohr:params:campaigns.start_date",
			"destination": "job:hohr:acquire_campaigns"
		}],
		"outEdges": [{
			"origin": "job:hohr:acquire_campaigns",
			"destination": "dataset:hohr:campaigns_sendinblue"
		}]
	}, {
		"id": "job:hohr:acquire_campaigns_candidates",
		"type": "JOB",
		"data": {
			"type": "BATCH",
			"id": {
				"namespace": "hohr",
				"name": "acquire_campaigns_candidates"
			},
			"name": "acquire_campaigns_candidates",
			"simpleName": "acquire_campaigns_candidates",
			"parentJobName": null,
			"createdAt": "2023-04-12T15:30:19.105448Z",
			"updatedAt": "2023-06-09T08:30:18.054733Z",
			"namespace": "hohr",
			"inputs": [{
				"namespace": "hohr",
				"name": "params:campaigns.end_date"
			}, {
				"namespace": "hohr",
				"name": "params:campaigns.mode.candidates"
			}, {
				"namespace": "hohr",
				"name": "params:campaigns.start_date"
			}],
			"outputs": [{
				"namespace": "hohr",
				"name": "campaigns_sendinblue_candidates"
			}],
			"location": null,
			"description": null,
			"latestRun": {
				"id": "96e56061-b5d6-47d2-85e4-9f4c11b38b22",
				"createdAt": "2023-06-09T08:30:17.170497Z",
				"updatedAt": "2023-06-09T08:30:18.054733Z",
				"nominalStartTime": null,
				"nominalEndTime": null,
				"state": "COMPLETED",
				"startedAt": "2023-06-09T08:30:17.170497Z",
				"endedAt": "2023-06-09T08:30:18.054733Z",
				"durationMs": 884,
				"args": {},
				"jobVersion": {
					"namespace": "hohr",
					"name": "acquire_campaigns_candidates",
					"version": "945556cf-c446-3d08-b2f6-257815eb3f41"
				},
				"inputVersions": [{
					"namespace": "hohr",
					"name": "params:campaigns.start_date",
					"version": "161e6c48-c2c5-3700-883e-7d336675e989"
				}, {
					"namespace": "hohr",
					"name": "params:campaigns.end_date",
					"version": "adc341b8-dae8-3c25-bb3a-98721bf92f68"
				}, {
					"namespace": "hohr",
					"name": "params:campaigns.mode.candidates",
					"version": "bca503fc-fd18-3da2-9132-8ba57d214aee"
				}],
				"outputVersions": [{
					"namespace": "hohr",
					"name": "campaigns_sendinblue_candidates",
					"version": "c8dc18d7-cd51-35f9-9645-9c1ea7438d92"
				}],
				"facets": {
					"extractor_version": "0.1.4",
					"func_name": "acquire_campaigns",
					"tags": []
				}
			}
		},
		"inEdges": [{
			"origin": "dataset:hohr:params:campaigns.end_date",
			"destination": "job:hohr:acquire_campaigns_candidates"
		}, {
			"origin": "dataset:hohr:params:campaigns.mode.candidates",
			"destination": "job:hohr:acquire_campaigns_candidates"
		}, {
			"origin": "dataset:hohr:params:campaigns.start_date",
			"destination": "job:hohr:acquire_campaigns_candidates"
		}],
		"outEdges": [{
			"origin": "job:hohr:acquire_campaigns_candidates",
			"destination": "dataset:hohr:campaigns_sendinblue_candidates"
		}]
	}, {
		"id": "job:hohr:acquire_campaigns_customers",
		"type": "JOB",
		"data": {
			"type": "BATCH",
			"id": {
				"namespace": "hohr",
				"name": "acquire_campaigns_customers"
			},
			"name": "acquire_campaigns_customers",
			"simpleName": "acquire_campaigns_customers",
			"parentJobName": null,
			"createdAt": "2023-04-12T15:30:12.918149Z",
			"updatedAt": "2023-06-09T08:30:11.586116Z",
			"namespace": "hohr",
			"inputs": [{
				"namespace": "hohr",
				"name": "params:campaigns.end_date"
			}, {
				"namespace": "hohr",
				"name": "params:campaigns.start_date"
			}, {
				"namespace": "hohr",
				"name": "params:campaigns.mode.customers"
			}],
			"outputs": [{
				"namespace": "hohr",
				"name": "campaigns_sendinblue_customers"
			}],
			"location": null,
			"description": null,
			"latestRun": {
				"id": "4d44e82b-a1d1-43e5-a2e1-86371537c34b",
				"createdAt": "2023-06-09T08:30:11.029043Z",
				"updatedAt": "2023-06-09T08:30:11.586116Z",
				"nominalStartTime": null,
				"nominalEndTime": null,
				"state": "COMPLETED",
				"startedAt": "2023-06-09T08:30:11.029043Z",
				"endedAt": "2023-06-09T08:30:11.586116Z",
				"durationMs": 557,
				"args": {},
				"jobVersion": {
					"namespace": "hohr",
					"name": "acquire_campaigns_customers",
					"version": "23f26ab2-1db1-3dc0-aa88-568870a16471"
				},
				"inputVersions": [{
					"namespace": "hohr",
					"name": "params:campaigns.start_date",
					"version": "161e6c48-c2c5-3700-883e-7d336675e989"
				}, {
					"namespace": "hohr",
					"name": "params:campaigns.mode.customers",
					"version": "66388e54-c4b9-39ce-81ed-89a68185e7cd"
				}, {
					"namespace": "hohr",
					"name": "params:campaigns.end_date",
					"version": "adc341b8-dae8-3c25-bb3a-98721bf92f68"
				}],
				"outputVersions": [{
					"namespace": "hohr",
					"name": "campaigns_sendinblue_customers",
					"version": "ced21232-ab32-3667-8cbd-24d8f5974a03"
				}],
				"facets": {
					"extractor_version": "0.1.4",
					"func_name": "acquire_campaigns",
					"tags": []
				}
			}
		},
		"inEdges": [{
			"origin": "dataset:hohr:params:campaigns.end_date",
			"destination": "job:hohr:acquire_campaigns_customers"
		}, {
			"origin": "dataset:hohr:params:campaigns.mode.customers",
			"destination": "job:hohr:acquire_campaigns_customers"
		}, {
			"origin": "dataset:hohr:params:campaigns.start_date",
			"destination": "job:hohr:acquire_campaigns_customers"
		}],
		"outEdges": [{
			"origin": "job:hohr:acquire_campaigns_customers",
			"destination": "dataset:hohr:campaigns_sendinblue_customers"
		}]
	}, {
		"id": "job:hohr:acquire_sending_list",
		"type": "JOB",
		"data": {
			"type": "BATCH",
			"id": {
				"namespace": "hohr",
				"name": "acquire_sending_list"
			},
			"name": "acquire_sending_list",
			"simpleName": "acquire_sending_list",
			"parentJobName": null,
			"createdAt": "2023-04-07T08:49:02.670817Z",
			"updatedAt": "2023-04-12T09:44:17.257915Z",
			"namespace": "hohr",
			"inputs": [{
				"namespace": "hohr",
				"name": "campaigns_sendinblue"
			}],
			"outputs": [{
				"namespace": "hohr",
				"name": "senders_sendinblue"
			}],
			"location": null,
			"description": null,
			"latestRun": {
				"id": "8581072c-6143-43e5-b713-0a1f8918b401",
				"createdAt": "2023-04-12T09:37:32.845661Z",
				"updatedAt": "2023-04-12T09:44:17.257915Z",
				"nominalStartTime": null,
				"nominalEndTime": null,
				"state": "COMPLETED",
				"startedAt": "2023-04-12T09:37:32.845661Z",
				"endedAt": "2023-04-12T09:44:17.257915Z",
				"durationMs": 404412,
				"args": {},
				"jobVersion": {
					"namespace": "hohr",
					"name": "acquire_sending_list",
					"version": "8031be47-17d4-3885-b846-460421c8e83b"
				},
				"inputVersions": [{
					"namespace": "hohr",
					"name": "campaigns_sendinblue",
					"version": "91b7bc63-7923-315b-83b4-c0470a9d114a"
				}],
				"outputVersions": [{
					"namespace": "hohr",
					"name": "senders_sendinblue",
					"version": "7a80116b-fc29-36e5-97f1-4ef12b75edc5"
				}],
				"facets": {
					"extractor_version": "0.1.2",
					"func_name": "acquire_sending_list",
					"tags": []
				}
			}
		},
		"inEdges": [{
			"origin": "dataset:hohr:campaigns_sendinblue",
			"destination": "job:hohr:acquire_sending_list"
		}],
		"outEdges": [{
			"origin": "job:hohr:acquire_sending_list",
			"destination": "dataset:hohr:senders_sendinblue"
		}]
	}, {
		"id": "job:hohr:acquire_sending_list_candidates",
		"type": "JOB",
		"data": {
			"type": "BATCH",
			"id": {
				"namespace": "hohr",
				"name": "acquire_sending_list_candidates"
			},
			"name": "acquire_sending_list_candidates",
			"simpleName": "acquire_sending_list_candidates",
			"parentJobName": null,
			"createdAt": "2023-04-12T18:56:04.779642Z",
			"updatedAt": "2023-06-09T08:35:36.725893Z",
			"namespace": "hohr",
			"inputs": [{
				"namespace": "hohr",
				"name": "params:campaigns.mode.candidates"
			}, {
				"namespace": "hohr",
				"name": "campaigns_sendinblue_candidates"
			}],
			"outputs": [{
				"namespace": "hohr",
				"name": "senders_sendinblue_candidates"
			}],
			"location": null,
			"description": null,
			"latestRun": {
				"id": "0ea254d6-a3e5-4f59-b523-cd0a34e25c90",
				"createdAt": "2023-06-09T08:34:12.835012Z",
				"updatedAt": "2023-06-09T08:35:36.725893Z",
				"nominalStartTime": null,
				"nominalEndTime": null,
				"state": "COMPLETED",
				"startedAt": "2023-06-09T08:34:12.835012Z",
				"endedAt": "2023-06-09T08:35:36.725893Z",
				"durationMs": 83890,
				"args": {},
				"jobVersion": {
					"namespace": "hohr",
					"name": "acquire_sending_list_candidates",
					"version": "3b5098fd-aefe-3e67-9db2-44a169d8bd44"
				},
				"inputVersions": [{
					"namespace": "hohr",
					"name": "campaigns_sendinblue_candidates",
					"version": "c8dc18d7-cd51-35f9-9645-9c1ea7438d92"
				}, {
					"namespace": "hohr",
					"name": "params:campaigns.mode.candidates",
					"version": "bca503fc-fd18-3da2-9132-8ba57d214aee"
				}],
				"outputVersions": [{
					"namespace": "hohr",
					"name": "senders_sendinblue_candidates",
					"version": "361c1cd2-ab37-3df6-ab37-d23a7ac07db2"
				}],
				"facets": {
					"extractor_version": "0.1.4",
					"func_name": "acquire_sending_list",
					"tags": []
				}
			}
		},
		"inEdges": [{
			"origin": "dataset:hohr:campaigns_sendinblue_candidates",
			"destination": "job:hohr:acquire_sending_list_candidates"
		}, {
			"origin": "dataset:hohr:params:campaigns.mode.candidates",
			"destination": "job:hohr:acquire_sending_list_candidates"
		}],
		"outEdges": [{
			"origin": "job:hohr:acquire_sending_list_candidates",
			"destination": "dataset:hohr:senders_sendinblue_candidates"
		}]
	}, {
		"id": "job:hohr:acquire_sending_list_customers",
		"type": "JOB",
		"data": {
			"type": "BATCH",
			"id": {
				"namespace": "hohr",
				"name": "acquire_sending_list_customers"
			},
			"name": "acquire_sending_list_customers",
			"simpleName": "acquire_sending_list_customers",
			"parentJobName": null,
			"createdAt": "2023-04-12T19:30:52.508848Z",
			"updatedAt": "2023-06-09T08:33:47.073402Z",
			"namespace": "hohr",
			"inputs": [{
				"namespace": "hohr",
				"name": "campaigns_sendinblue_customers"
			}, {
				"namespace": "hohr",
				"name": "params:campaigns.mode.customers"
			}],
			"outputs": [{
				"namespace": "hohr",
				"name": "senders_sendinblue_customers"
			}],
			"location": null,
			"description": null,
			"latestRun": {
				"id": "aea1c4e5-098e-4c5d-9c80-c78907dfcc24",
				"createdAt": "2023-06-09T08:33:46.962854Z",
				"updatedAt": "2023-06-09T08:33:47.073402Z",
				"nominalStartTime": null,
				"nominalEndTime": null,
				"state": "COMPLETED",
				"startedAt": "2023-06-09T08:33:46.962854Z",
				"endedAt": "2023-06-09T08:33:47.073402Z",
				"durationMs": 111,
				"args": {},
				"jobVersion": {
					"namespace": "hohr",
					"name": "acquire_sending_list_customers",
					"version": "2f932d03-59c0-36b1-8ef8-dfdfe3a44dcd"
				},
				"inputVersions": [{
					"namespace": "hohr",
					"name": "params:campaigns.mode.customers",
					"version": "66388e54-c4b9-39ce-81ed-89a68185e7cd"
				}, {
					"namespace": "hohr",
					"name": "campaigns_sendinblue_customers",
					"version": "ced21232-ab32-3667-8cbd-24d8f5974a03"
				}],
				"outputVersions": [{
					"namespace": "hohr",
					"name": "senders_sendinblue_customers",
					"version": "6fe53b48-e01c-3ad4-84ec-f40cf3955075"
				}],
				"facets": {
					"extractor_version": "0.1.4",
					"func_name": "acquire_sending_list",
					"tags": []
				}
			}
		},
		"inEdges": [{
			"origin": "dataset:hohr:campaigns_sendinblue_customers",
			"destination": "job:hohr:acquire_sending_list_customers"
		}, {
			"origin": "dataset:hohr:params:campaigns.mode.customers",
			"destination": "job:hohr:acquire_sending_list_customers"
		}],
		"outEdges": [{
			"origin": "job:hohr:acquire_sending_list_customers",
			"destination": "dataset:hohr:senders_sendinblue_customers"
		}]
	}, {
		"id": "job:hohr:acquire_templates_customers",
		"type": "JOB",
		"data": {
			"type": "BATCH",
			"id": {
				"namespace": "hohr",
				"name": "acquire_templates_customers"
			},
			"name": "acquire_templates_customers",
			"simpleName": "acquire_templates_customers",
			"parentJobName": null,
			"createdAt": "2023-04-12T15:36:07.505082Z",
			"updatedAt": "2023-06-09T08:32:59.025465Z",
			"namespace": "hohr",
			"inputs": [{
				"namespace": "hohr",
				"name": "params:campaigns.end_date"
			}, {
				"namespace": "hohr",
				"name": "params:campaigns.start_date"
			}, {
				"namespace": "hohr",
				"name": "params:campaigns.mode.customers"
			}],
			"outputs": [{
				"namespace": "hohr",
				"name": "templates_sendinblue_customers"
			}],
			"location": null,
			"description": null,
			"latestRun": {
				"id": "4f04e858-0843-4b68-9de2-bb5249edac11",
				"createdAt": "2023-06-09T08:32:58.675907Z",
				"updatedAt": "2023-06-09T08:32:59.025465Z",
				"nominalStartTime": null,
				"nominalEndTime": null,
				"state": "COMPLETED",
				"startedAt": "2023-06-09T08:32:58.675907Z",
				"endedAt": "2023-06-09T08:32:59.025465Z",
				"durationMs": 350,
				"args": {},
				"jobVersion": {
					"namespace": "hohr",
					"name": "acquire_templates_customers",
					"version": "b080e427-e006-32a4-802b-3ac0dc71df47"
				},
				"inputVersions": [{
					"namespace": "hohr",
					"name": "params:campaigns.start_date",
					"version": "161e6c48-c2c5-3700-883e-7d336675e989"
				}, {
					"namespace": "hohr",
					"name": "params:campaigns.mode.customers",
					"version": "66388e54-c4b9-39ce-81ed-89a68185e7cd"
				}, {
					"namespace": "hohr",
					"name": "params:campaigns.end_date",
					"version": "adc341b8-dae8-3c25-bb3a-98721bf92f68"
				}],
				"outputVersions": [{
					"namespace": "hohr",
					"name": "templates_sendinblue_customers",
					"version": "9230fdf0-9a85-3b1c-ac67-c8b3ff61d0cb"
				}],
				"facets": {
					"extractor_version": "0.1.4",
					"func_name": "acquire_template",
					"tags": []
				}
			}
		},
		"inEdges": [{
			"origin": "dataset:hohr:params:campaigns.end_date",
			"destination": "job:hohr:acquire_templates_customers"
		}, {
			"origin": "dataset:hohr:params:campaigns.mode.customers",
			"destination": "job:hohr:acquire_templates_customers"
		}, {
			"origin": "dataset:hohr:params:campaigns.start_date",
			"destination": "job:hohr:acquire_templates_customers"
		}],
		"outEdges": [{
			"origin": "job:hohr:acquire_templates_customers",
			"destination": "dataset:hohr:templates_sendinblue_customers"
		}]
	}, {
		"id": "job:hohr:export_master_senders_gbq",
		"type": "JOB",
		"data": {
			"type": "BATCH",
			"id": {
				"namespace": "hohr",
				"name": "export_master_senders_gbq"
			},
			"name": "export_master_senders_gbq",
			"simpleName": "export_master_senders_gbq",
			"parentJobName": null,
			"createdAt": "2023-04-07T09:43:42.225519Z",
			"updatedAt": "2023-06-09T08:43:18.308503Z",
			"namespace": "hohr",
			"inputs": [{
				"namespace": "hohr",
				"name": "master_senders"
			}],
			"outputs": [{
				"namespace": "hohr",
				"name": "master_senders_gbq"
			}],
			"location": null,
			"description": null,
			"latestRun": {
				"id": "8f0e354f-a6da-4b17-8b0c-aab8c24e5a75",
				"createdAt": "2023-06-09T08:43:18.232166Z",
				"updatedAt": "2023-06-09T08:43:18.308503Z",
				"nominalStartTime": null,
				"nominalEndTime": null,
				"state": "COMPLETED",
				"startedAt": "2023-06-09T08:43:18.232166Z",
				"endedAt": "2023-06-09T08:43:18.308503Z",
				"durationMs": 76,
				"args": {},
				"jobVersion": {
					"namespace": "hohr",
					"name": "export_master_senders_gbq",
					"version": "ffd9fc43-25b0-3a75-9a65-6b94914450e5"
				},
				"inputVersions": [{
					"namespace": "hohr",
					"name": "master_senders",
					"version": "d546e682-84a0-3303-9c7a-53f6a6ea02d4"
				}],
				"outputVersions": [{
					"namespace": "hohr",
					"name": "master_senders_gbq",
					"version": "7660e695-4f35-39a7-b3bd-50fe8a07b09a"
				}],
				"facets": {
					"extractor_version": "0.1.4",
					"func_name": "identity",
					"tags": []
				}
			}
		},
		"inEdges": [{
			"origin": "dataset:hohr:master_senders",
			"destination": "job:hohr:export_master_senders_gbq"
		}],
		"outEdges": [{
			"origin": "job:hohr:export_master_senders_gbq",
			"destination": "dataset:hohr:master_senders_gbq"
		}]
	}, {
		"id": "job:hohr:gather_senders",
		"type": "JOB",
		"data": {
			"type": "BATCH",
			"id": {
				"namespace": "hohr",
				"name": "gather_senders"
			},
			"name": "gather_senders",
			"simpleName": "gather_senders",
			"parentJobName": null,
			"createdAt": "2023-04-07T09:42:30.882417Z",
			"updatedAt": "2023-06-09T08:41:45.020481Z",
			"namespace": "hohr",
			"inputs": [{
				"namespace": "hohr",
				"name": "senders_sendinblue_candidates"
			}, {
				"namespace": "hohr",
				"name": "senders_sendinblue_customers"
			}],
			"outputs": [{
				"namespace": "hohr",
				"name": "master_senders"
			}],
			"location": null,
			"description": null,
			"latestRun": {
				"id": "cee42615-d474-4d9e-bfd7-21c3c64c8405",
				"createdAt": "2023-06-09T08:41:44.918614Z",
				"updatedAt": "2023-06-09T08:41:45.020481Z",
				"nominalStartTime": null,
				"nominalEndTime": null,
				"state": "COMPLETED",
				"startedAt": "2023-06-09T08:41:44.918614Z",
				"endedAt": "2023-06-09T08:41:45.020481Z",
				"durationMs": 102,
				"args": {},
				"jobVersion": {
					"namespace": "hohr",
					"name": "gather_senders",
					"version": "c7267061-c9a0-3b7d-b78a-a223bc772a73"
				},
				"inputVersions": [{
					"namespace": "hohr",
					"name": "senders_sendinblue_candidates",
					"version": "361c1cd2-ab37-3df6-ab37-d23a7ac07db2"
				}, {
					"namespace": "hohr",
					"name": "senders_sendinblue_customers",
					"version": "6fe53b48-e01c-3ad4-84ec-f40cf3955075"
				}],
				"outputVersions": [{
					"namespace": "hohr",
					"name": "master_senders",
					"version": "d546e682-84a0-3303-9c7a-53f6a6ea02d4"
				}],
				"facets": {
					"extractor_version": "0.1.4",
					"func_name": "concat_df",
					"tags": []
				}
			}
		},
		"inEdges": [{
			"origin": "dataset:hohr:senders_sendinblue_candidates",
			"destination": "job:hohr:gather_senders"
		}, {
			"origin": "dataset:hohr:senders_sendinblue_customers",
			"destination": "job:hohr:gather_senders"
		}],
		"outEdges": [{
			"origin": "job:hohr:gather_senders",
			"destination": "dataset:hohr:master_senders"
		}]
	}]
}`);