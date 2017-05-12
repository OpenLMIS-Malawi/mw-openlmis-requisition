package mw.gov.health.lmis.mwrequisition.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientResponseException;

import mw.gov.health.lmis.mwrequisition.dto.LocalizedMessageDto;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionDto;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionErrorMessage;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionsProcessingStatusDto;
import mw.gov.health.lmis.mwrequisition.service.RequisitionService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
public class BatchRequisitionController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(BatchRequisitionController.class);

  @Autowired
  private RequisitionService requisitionService;

  private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Attempts to approve requisitions with the provided UUIDs.
   */
  @RequestMapping(value = "/requisitions/approve", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<RequisitionsProcessingStatusDto> approve(@RequestBody List<UUID> uuids) {
    RequisitionsProcessingStatusDto processingStatus = new RequisitionsProcessingStatusDto();

    for (UUID requisitionId : uuids) {
      try {
        RequisitionDto requisitionDto = requisitionService.approve(requisitionId).getBody();
        processingStatus.addProcessedRequisition(requisitionDto);
      } catch (RestClientResponseException ex) {
        LocalizedMessageDto messageDto = parseErrorResponse(ex.getResponseBodyAsString());
        processingStatus.addProcessingError(new RequisitionErrorMessage(requisitionId,
            messageDto.getMessageKey(), messageDto.getMessage()));
      }
    }

    return new ResponseEntity<>(processingStatus, processingStatus.getRequisitionErrors().isEmpty()
        ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
  }

  /**
   * Attempts to approve requisitions with the provided UUIDs.
   */
  @RequestMapping(value = "/requisitions/save", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<RequisitionsProcessingStatusDto> update(@RequestBody List<RequisitionDto>
                                                                     dtos) {
    RequisitionsProcessingStatusDto processingStatus = new RequisitionsProcessingStatusDto();

    for (RequisitionDto dto : dtos) {
      try {
        RequisitionDto requisitionDto = requisitionService.update(dto).getBody();
        processingStatus.addProcessedRequisition(requisitionDto);
      } catch (RestClientResponseException ex) {
        LocalizedMessageDto messageDto = parseErrorResponse(ex.getResponseBodyAsString());
        processingStatus.addProcessingError(new RequisitionErrorMessage(dto.getId(), messageDto
            .getMessageKey(), messageDto.getMessage()));
      }
    }

    return new ResponseEntity<>(processingStatus, processingStatus.getRequisitionErrors().isEmpty()
        ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
  }

  private LocalizedMessageDto parseErrorResponse(String response) {
    try {
      return objectMapper.reader(LocalizedMessageDto.class).readValue(response);
    } catch (IOException ex) {
      LOGGER.error("Cannot deserialize the error messsage", ex);
      return new LocalizedMessageDto("requisition.error", response);
    }
  }
}
