/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE datasets ADD namespace_name varchar;

UPDATE datasets SET
namespace_name = n.name
FROM namespaces n
WHERE n.uuid = datasets.namespace_uuid;

ALTER TABLE datasets ADD source_name varchar;
UPDATE datasets SET
    source_name = s.name
FROM sources s
WHERE s.uuid = datasets.source_uuid;

