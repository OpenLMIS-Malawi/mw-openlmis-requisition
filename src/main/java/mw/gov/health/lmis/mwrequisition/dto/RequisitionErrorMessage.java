package mw.gov.health.lmis.mwrequisition.dto;

import lombok.Getter;
import mw.gov.health.lmis.mwrequisition.errorhandling.Message;

import java.util.UUID;

/**
 * Error message linked with a specific requisition.
 */
@Getter
public class RequisitionErrorMessage {

  private UUID requisitionId;
  private Message errorMessage;

  public RequisitionErrorMessage(UUID requisitionId, String key, Object... messageParameters) {
    this.requisitionId = requisitionId;
    this.errorMessage = new Message(key, messageParameters);
  }
}
