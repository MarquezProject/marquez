/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/** An exception thrown to indicate an HTTP error. */
@NoArgsConstructor
@ToString
public final class MarquezHttpException extends MarquezClientException {
  private static final long serialVersionUID = 1L;

  @Getter @Nullable private Integer code;
  @Getter @Nullable private String message;
  @Getter @Nullable private String details;

  /** Constructs a {@code MarquezHttpException} with the HTTP error {@code error}. */
  MarquezHttpException(@NonNull final MarquezHttp.HttpError error) {
    this.code = error.getCode();
    this.message = error.getMessage();
    this.details = error.getDetails();
  }
}
