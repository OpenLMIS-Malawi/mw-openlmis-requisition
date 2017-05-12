package mw.gov.health.lmis.mwrequisition.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import mw.gov.health.lmis.mwrequisition.dto.RequisitionDto;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionsProcessingStatusDto;
import mw.gov.health.lmis.mwrequisition.service.RequisitionService;

import java.util.List;
import java.util.UUID;

@Controller
public class BatchRequisitionController extends BaseController {

  @Autowired
  private RequisitionService requisitionService;

  /**
   * Attempts to approve requisitions with the provided UUIDs.
   */
  @RequestMapping(value = "/requisitions/approve", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RequisitionsProcessingStatusDto approve(@RequestBody List<UUID> uuids) {
    for (UUID requisitionId : uuids) {
      requisitionService.approve(requisitionId);
    }

    return new RequisitionsProcessingStatusDto();
  }

  /**
   * Attempts to approve requisitions with the provided UUIDs.
   */
  @RequestMapping(value = "/requisitions/save", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RequisitionsProcessingStatusDto update(@RequestBody List<RequisitionDto> dtos) {
    for (RequisitionDto dto : dtos) {
      requisitionService.update(dto);
    }

    return new RequisitionsProcessingStatusDto();
  }
}
