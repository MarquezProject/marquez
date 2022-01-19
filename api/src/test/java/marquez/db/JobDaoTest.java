/* SPDX-License-Identifier: Apache-2.0 */

package marquez.db;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class JobDaoTest {

  private static JobDao jobDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    jobDao = jdbi.onDemand(JobDao.class);
  }

  @Test
  public void emptyUrl() {
    assertNull(jobDao.toUrlString(null));
  }

  @Test
  public void pgObjectException() throws JsonProcessingException {
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException());
    assertNull(jobDao.toJson(null, objectMapper));
  }
}
