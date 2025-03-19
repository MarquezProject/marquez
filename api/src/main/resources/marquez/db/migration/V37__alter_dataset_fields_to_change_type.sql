/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE dataset_fields ALTER COLUMN type TYPE TEXT;

-- Implementation required for streaming CDC support
ALTER TABLE dataset_fields REPLICA IDENTITY FULL;