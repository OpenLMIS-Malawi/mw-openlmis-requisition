package mw.gov.health.lmis.mwrequisition.dto;

import lombok.Getter;

import java.util.UUID;

/**
 * Error message linked with a specific requisition.
 */
@Getter
public class RequisitionErrorMessage {

  private UUID requisitionId;
  private LocalizedMessageDto errorMessage;

  public RequisitionErrorMessage(UUID requisitionId, String messageKey, String message) {
    this.requisitionId = requisitionId;
    this.errorMessage = new LocalizedMessageDto(messageKey, message);
  }
}
