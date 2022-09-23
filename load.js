import { SharedArray } from 'k6/data';
import http from 'k6/http';
import { check } from 'k6';
import { Rate } from 'k6/metrics';
import { uuidv4, randomIntBetween } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";
import exec from 'k6/execution';

export const errorRate = new Rate('errors');

const graph = new SharedArray('graph', function () {
  return JSON.parse(open('./graph.json'));
});

let get_fields = function () {
  return [{
    "name": "field574654616",
    "type": "VARCHAR",
    "description": "description1901257432"
  }, {
    "name": "field1973758593",
    "type": "TEXT",
    "description": "description1919217995"
  }, {
    "name": "field898064800",
    "type": "VARCHAR",
    "description": "description1559432549"
  }, {
    "name": "field1206348422",
    "type": "INTEGER",
    "description": "description1112972360"
  }]
}

let get_dataset = function (namespace, name) {
  return {
    "namespace": namespace,
    "name": name,
    "facets": {
      "schema": {
        "_producer": "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/MetadataCommand.java",
        "_schemaURL": "https://openlineage.io/spec/facets/1-0-0/SchemaDatasetFacet.json#/$defs/SchemaDatasetFacet",
        "fields": get_fields()
      }
    }
  }
}

let get_event = function(job_template, run_id, inputs, outputs) {
  let date = new Date();

  let runFacets = {
    "parent": {
      "_producer": "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/MetadataCommand.java",
      "_schemaURL": "https://openlineage.io/spec/facets/1-0-0/ParentRunFacet.json#/$defs/ParentRunFacet",
      "run": {
        "runId": "11cef0ea-c301-422e-90cf-cf2ba1bb45c1"
      },
      "job": {
        "namespace": "namespace105136515",
        "name": "job335691945"
      }
    },
    "nominalTime": {
      "_producer": "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/MetadataCommand.java",
      "_schemaURL": "https://openlineage.io/spec/facets/1-0-0/NominalTimeRunFacet.json#/$defs/NominalTimeRunFacet",
      "nominalStartTime": "2022-09-12T06:04:41.171378-07:00",
      "nominalEndTime": "2022-09-12T07:04:41.171871-07:00"
    }
  }


    return [{
    "eventType": "START",
    "eventTime": date.toISOString(),
    "run": {
      "runId": run_id,
      "facets": runFacets
    },
    "job": {
      "namespace": "namespace105136515",
      "name": job_template.jobName
    },
    "inputs": inputs,
    "outputs": outputs,
    "producer": "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/MetadataCommand.java",
    "schemaURL": "https://openlineage.io/spec/1-0-3/OpenLineage.json#/$defs/RunEvent"
  }, {
    "eventType": "COMPLETE",
    "eventTime": date.toISOString(),
    "run": {
      "runId": run_id,
      "facets": runFacets
    },
    "job": {
      "namespace": "namespace",
      "name": job_template.jobName
    },
    "inputs": inputs,
    "outputs": outputs,
    "producer": "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/MetadataCommand.java",
    "schemaURL": "https://openlineage.io/spec/1-0-3/OpenLineage.json#/$defs/RunEvent"
  }]
}

export default function () {
  const url = 'http://localhost:5000/api/v1/lineage';
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  var job = graph[exec.vu.idInInstance-1];

  let ol_events = get_event(
      job,
      uuidv4(),
      [get_dataset(`namespace${exec.vu.idInInstance}`, `dataset${randomIntBetween(1, 10000000)}`)],
      [get_dataset(`namespace${exec.vu.idInInstance}`, `dataset${randomIntBetween(1, 10000000)}`)]
  );

  // send start
  check(http.post(url, JSON.stringify(ol_events[0]), params), {
    'status is 201': (r) => r.status === 201,
  }) || errorRate.add(1);

  // send complete
  check(http.post(url, JSON.stringify(ol_events[1]), params), {
    'status is 201': (r) => r.status === 201,
  }) || errorRate.add(1);

}