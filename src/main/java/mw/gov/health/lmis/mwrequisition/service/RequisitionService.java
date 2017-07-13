package mw.gov.health.lmis.mwrequisition.service;

import static mw.gov.health.lmis.mwrequisition.service.AuthService.ACCESS_TOKEN;
import static mw.gov.health.lmis.util.RequestHelper.createUri;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import mw.gov.health.lmis.mwrequisition.dto.BasicRequisitionDto;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionDto;

import java.util.UUID;

@Service
public class RequisitionService {
  private static final String URL = "/api/requisitions/";
  private static final String APPROVE_ENDPOINT = "/approve";

  private RestOperations restTemplate = new RestTemplate();

  @Value("${requisition.url}")
  private String requisitionUrl;

  /**
   * Sends a request to the openlmis-requisition service to retrieve the requisition of the given
   * UUID.
   *
   * @param uuid the UUID of the requisition to retrieve
   */
  public RequisitionDto retrieve(UUID uuid, String token) {
    String url = requisitionUrl + URL + uuid.toString();

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, token);

    return restTemplate
        .getForEntity(createUri(url, parameters), RequisitionDto.class)
        .getBody();
  }

  /**
   * Sends a request to the openlmis-requisition service to approve the requisition of the given
   * UUID.
   *
   * @param uuid the UUID of the requisition to approve
   */
  public BasicRequisitionDto approve(UUID uuid, String token) {
    String url = requisitionUrl + URL + uuid.toString() + APPROVE_ENDPOINT;

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, token);

    return restTemplate
        .postForEntity(createUri(url, parameters), null, BasicRequisitionDto.class)
        .getBody();
  }

  /**
   * Sends a request to the openlmis-requisition service to save the requisition.
   *
   * @param requisition the representation of the object to save
   */
  public RequisitionDto update(RequisitionDto requisition, String token)
      throws RestClientException {
    String url = requisitionUrl + URL + requisition.getId();

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, token);

    HttpEntity<RequisitionDto> body = new HttpEntity<>(requisition);

    return restTemplate
        .exchange(createUri(url, parameters), HttpMethod.PUT, body, RequisitionDto.class)
        .getBody();
  }

}
