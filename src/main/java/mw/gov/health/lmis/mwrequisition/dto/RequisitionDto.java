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
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package mw.gov.health.lmis.mwrequisition.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionDto {
  private UUID id;
  private ZonedDateTime createdDate;
  private ZonedDateTime modifiedDate;
  private List<RequisitionLineItemDto> requisitionLineItems;
  private String draftStatusMessage;
  private FacilityDto facility;
  private ProgramDto program;
  private ProcessingPeriodDto processingPeriod;
  private RequisitionStatus status;
  private Boolean emergency;
  private UUID supplyingFacility;
  private UUID supervisoryNode;
  private BasicRequisitionTemplateDto template;
  private Set<OrderableDto> availableNonFullSupplyProducts;
  private Map<String, StatusLogEntry> statusChanges = new HashMap<>();
  private List<StatusChangeDto> statusHistory = new ArrayList<>();
}