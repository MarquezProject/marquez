/* SPDX-License-Identifier: Apache-2.0 */
DROP VIEW IF EXISTS datasets_view;
ALTER TABLE dataset_symlinks ALTER COLUMN name TYPE VARCHAR;