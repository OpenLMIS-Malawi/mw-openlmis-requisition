package mw.gov.health.lmis.mwrequisition.dto;

import org.apache.commons.lang3.BooleanUtils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents the results of a batch processing of requisitions.
 */
@Getter
public class RequisitionsProcessingStatusDto {

  private List<ApproveRequisitionDto> requisitionDtos;
  private List<RequisitionErrorMessage> requisitionErrors;

  public RequisitionsProcessingStatusDto() {
    this.requisitionDtos = new ArrayList<>();
    this.requisitionErrors = new ArrayList<>();
  }

  public void addProcessedRequisition(ApproveRequisitionDto requisitionDto) {
    requisitionDtos.add(requisitionDto);
  }

  public void addProcessingError(RequisitionErrorMessage errorMessage) {
    this.requisitionErrors.add(errorMessage);
  }

  /**
   * Removes skipped products but only if those products are skipped in all requisitions.
   */
  public void removeSkippedProducts() {
    // all requisition line items
    List<ApproveRequisitionLineItemDto> requisitionLineItems = requisitionDtos
        .stream()
        .map(ApproveRequisitionDto::getRequisitionLineItems)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    // all products. This list contains products that would be removed because all selected
    // requisitions skipped those products.
    Set<UUID> products = requisitionLineItems
        .stream()
        .map(ApproveRequisitionLineItemDto::getOrderable)
        .map(BasicOrderableDto::getId)
        .collect(Collectors.toSet());

    // if the given product is not skipped in any requisition, it will not be removed
    // because it will not be present on the list.
    requisitionLineItems
        .stream()
        .filter(line -> BooleanUtils.isFalse(line.getSkipped()))
        .map(ApproveRequisitionLineItemDto::getOrderable)
        .map(BasicOrderableDto::getId)
        .forEach(products::remove);

    // find those requisition line items that contain skipped (in all requisitions) product
    // and remove it.
    for (ApproveRequisitionDto requisition : requisitionDtos) {
      Iterator<ApproveRequisitionLineItemDto> iterator = requisition
          .getRequisitionLineItems()
          .iterator();

      while (iterator.hasNext()) {
        ApproveRequisitionLineItemDto line = iterator.next();
        BasicOrderableDto orderable = line.getOrderable();
        UUID id = orderable.getId();

        if (products.contains(id)) {
          iterator.remove();
        }
      }
    }
  }

}
