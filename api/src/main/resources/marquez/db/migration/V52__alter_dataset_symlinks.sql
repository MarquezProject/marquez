/* SPDX-License-Identifier: Apache-2.0 */

DO
$$
DECLARE
    datasets_view_exists boolean;
    datasets_view_definition text;
BEGIN
    SELECT EXISTS (
        SELECT * FROM information_schema.views
        WHERE table_schema='public' AND table_name='datasets_view'
    ) INTO datasets_view_exists;

    IF datasets_view_exists THEN
        -- Altering is not allowed when the column is being used from views. So here,
        -- we temporarily drop the view before altering and recreate it.
        SELECT view_definition FROM information_schema.views
        WHERE table_schema='public' AND table_name='datasets_view'
        INTO datasets_view_definition;

        DROP VIEW datasets_view;
        ALTER TABLE dataset_symlinks ALTER COLUMN name TYPE VARCHAR;
        EXECUTE format('CREATE VIEW datasets_view AS %s', datasets_view_definition);
    ELSE
        ALTER TABLE dataset_symlinks ALTER COLUMN name TYPE VARCHAR;
    END IF;
END
$$ LANGUAGE plpgsql;
