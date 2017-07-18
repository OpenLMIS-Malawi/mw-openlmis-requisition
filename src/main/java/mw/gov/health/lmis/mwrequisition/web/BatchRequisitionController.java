package mw.gov.health.lmis.mwrequisition.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientResponseException;

import mw.gov.health.lmis.mwrequisition.dto.ApproveRequisitionDto;
import mw.gov.health.lmis.mwrequisition.dto.ApproveRequisitionLineItemDto;
import mw.gov.health.lmis.mwrequisition.dto.BasicRequisitionDto;
import mw.gov.health.lmis.mwrequisition.dto.LocalizedMessageDto;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionDto;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionErrorMessage;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionsProcessingStatusDto;
import mw.gov.health.lmis.mwrequisition.service.AuthService;
import mw.gov.health.lmis.mwrequisition.service.RequisitionService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Controller
public class BatchRequisitionController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(BatchRequisitionController.class);

  @Autowired
  private RequisitionService requisitionService;

  @Autowired
  private AuthService authService;

  @Value("${batchApprove.retrieveAsync.poolSize}")
  private Integer poolSize = 5;

  private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Attempts to retrieve requisitions with the provided UUIDs.
   */
  @RequestMapping(value = "/requisitions/batch", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RequisitionsProcessingStatusDto retrieve(@RequestBody List<UUID> uuids) {
    String accessToken = authService.obtainAccessToken();
    List<RequisitionDto> requisitions = retrieveAsync(uuids, accessToken);

    RequisitionsProcessingStatusDto processingStatus = new RequisitionsProcessingStatusDto();
    requisitions
        .stream()
        .map(ApproveRequisitionDto::new)
        .forEach(processingStatus::addProcessedRequisition);

    processingStatus.removeSkippedProducts();

    return processingStatus;
  }

  /**
   * Attempts to approve requisitions with the provided UUIDs.
   */
  @RequestMapping(value = "/requisitions/batch/approve", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<RequisitionsProcessingStatusDto> approve(@RequestBody List<UUID> uuids) {
    RequisitionsProcessingStatusDto processingStatus = new RequisitionsProcessingStatusDto();
    String accessToken = authService.obtainAccessToken();

    for (UUID requisitionId : uuids) {
      try {
        BasicRequisitionDto requisitionDto = requisitionService
            .approve(requisitionId, accessToken);

        processingStatus.addProcessedRequisition(new ApproveRequisitionDto(requisitionDto));
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
  @RequestMapping(value = "/requisitions/batch/save", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<RequisitionsProcessingStatusDto> update(
      @RequestBody List<ApproveRequisitionDto> dtos) {

    List<UUID> uuids = dtos.stream().map(ApproveRequisitionDto::getId).collect(Collectors.toList());
    String accessToken = authService.obtainAccessToken();

    List<RequisitionDto> requisitions = retrieveAsync(uuids, accessToken);

    RequisitionsProcessingStatusDto processingStatus = new RequisitionsProcessingStatusDto();

    for (ApproveRequisitionDto dto : dtos) {
      requisitions
          .stream()
          .filter(requisition -> Objects.equals(requisition.getId(), dto.getId()))
          .findFirst()
          .ifPresent(requisition -> updateOne(processingStatus, dto, requisition, accessToken));
    }

    processingStatus.removeSkippedProducts();

    return new ResponseEntity<>(processingStatus, processingStatus.getRequisitionErrors().isEmpty()
        ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
  }

  private void updateOne(RequisitionsProcessingStatusDto processingStatus,
                         ApproveRequisitionDto dto, RequisitionDto requisition,
                         String token) {
    for (ApproveRequisitionLineItemDto line : dto.getRequisitionLineItems()) {
      requisition
          .getRequisitionLineItems()
          .stream()
          .filter(original -> Objects.equals(original.getId(), line.getId()))
          .findFirst()
          .ifPresent(original -> {
            original.setApprovedQuantity(line.getApprovedQuantity());
            original.setTotalCost(line.getTotalCost());
          });
    }

    requisition.setModifiedDate(dto.getModifiedDate());
    requisition.setNullForCalculatedFields();

    try {
      RequisitionDto response = requisitionService.update(requisition, token);
      processingStatus.addProcessedRequisition(new ApproveRequisitionDto(response));
    } catch (RestClientResponseException ex) {
      LocalizedMessageDto messageDto = parseErrorResponse(ex.getResponseBodyAsString());
      processingStatus.addProcessingError(new RequisitionErrorMessage(dto.getId(), messageDto
          .getMessageKey(), messageDto.getMessage()));
    }
  }

  private List<RequisitionDto> retrieveAsync(List<UUID> uuids, String token) {
    ExecutorService executor = Executors.newFixedThreadPool(Math.min(uuids.size(), poolSize));

    List<CompletableFuture<RequisitionDto>> futures = uuids.stream()
        .map(id ->
            CompletableFuture.supplyAsync(() -> requisitionService.retrieve(id, token), executor))
        .collect(Collectors.toList());

    return futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
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
