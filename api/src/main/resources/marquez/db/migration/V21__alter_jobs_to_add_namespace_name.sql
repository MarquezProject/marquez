/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE jobs ADD namespace_name VARCHAR(255);
UPDATE jobs SET namespace_name = namespaces.name FROM namespaces WHERE jobs.namespace_uuid = namespaces.uuid;