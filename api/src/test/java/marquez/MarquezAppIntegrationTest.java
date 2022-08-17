/* SPDX-License-Identifier: Apache-2.0 */

package marquez;

import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class MarquezAppIntegrationTest {}
