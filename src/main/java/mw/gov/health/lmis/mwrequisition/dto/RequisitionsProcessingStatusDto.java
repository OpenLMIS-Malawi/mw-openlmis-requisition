package mw.gov.health.lmis.mwrequisition.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the results of a batch processing of requisitions.
 */
@Getter
public class RequisitionsProcessingStatusDto {

  private List<RequisitionDto> requisitionDtos;
  private List<RequisitionErrorMessage> requisitionErrors;

  public RequisitionsProcessingStatusDto() {
    this.requisitionDtos = new ArrayList<>();
    this.requisitionErrors = new ArrayList<>();
  }

  public void addProcessedRequisition(RequisitionDto requisitionDto) {
    requisitionDtos.add(requisitionDto);
  }

  public void addProcessingError(RequisitionErrorMessage errorMessage) {
    this.requisitionErrors.add(errorMessage);
  }
}
