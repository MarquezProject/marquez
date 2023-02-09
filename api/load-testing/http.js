import { SharedArray } from 'k6/data';
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export const errorRate = new Rate('errors');

const metadata = new SharedArray('metadata', function () {
  return JSON.parse(open('./metadata.json'));
});

export default function () {
  const url = 'http://localhost:8080/api/v1/lineage';
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  var ol_event = metadata[__VU-1]

  check(http.post(url, JSON.stringify(ol_event), params), {
    'status is 201': (r) => r.status == 201,
  }) || errorRate.add(1);

  sleep(1);
}
