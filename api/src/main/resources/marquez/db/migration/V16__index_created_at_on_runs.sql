/* SPDX-License-Identifier: Apache-2.0 */

create index runs_created_at_index
    on runs (created_at desc);
