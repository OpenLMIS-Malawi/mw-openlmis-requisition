package mw.gov.health.lmis.mwrequisition.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LocalizedMessageDto {
  private String messageKey;
  private String message;
}
