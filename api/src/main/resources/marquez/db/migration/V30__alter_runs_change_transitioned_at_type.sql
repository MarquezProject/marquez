/* SPDX-License-Identifier: Apache-2.0 */

alter table runs alter column transitioned_at type timestamp without time zone
    USING transitioned_at::timestamp without time zone;