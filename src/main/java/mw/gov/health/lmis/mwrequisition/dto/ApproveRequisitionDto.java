/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package mw.gov.health.lmis.mwrequisition.dto;

import com.google.common.collect.Lists;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class ApproveRequisitionDto {
  private UUID id;

  @JsonSerialize(as = BasicProcessingPeriodDto.class)
  private BasicProcessingPeriodDto processingPeriod;

  @JsonSerialize(as = BasicFacilityDto.class)
  private BasicFacilityDto facility;

  private List<ApproveRequisitionLineItemDto> requisitionLineItems;

  /**
   * Creates instance with data from original requisition.
   */
  public ApproveRequisitionDto(RequisitionDto requisition) {
    this.id = requisition.getId();
    this.facility = requisition.getFacility();
    this.processingPeriod = requisition.getProcessingPeriod();
    this.requisitionLineItems = requisition
        .getRequisitionLineItems()
        .stream()
        .map(ApproveRequisitionLineItemDto::new)
        .collect(Collectors.toList());
  }

  /**
   * Creates instance with data from original basic requisition.
   */
  public ApproveRequisitionDto(BasicRequisitionDto requisition) {
    this.id = requisition.getId();
    this.facility = requisition.getFacility();
    this.processingPeriod = requisition.getProcessingPeriod();
    this.requisitionLineItems = Lists.newArrayList();
  }

}
