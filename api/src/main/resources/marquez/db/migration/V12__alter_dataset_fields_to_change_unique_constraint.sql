/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE dataset_fields DROP CONSTRAINT dataset_fields_dataset_uuid_name_key;
ALTER TABLE dataset_fields ADD UNIQUE (dataset_uuid, name, type);
