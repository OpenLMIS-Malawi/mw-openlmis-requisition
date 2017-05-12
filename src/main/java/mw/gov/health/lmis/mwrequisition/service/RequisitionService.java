package mw.gov.health.lmis.mwrequisition.service;

import static mw.gov.health.lmis.mwrequisition.service.AuthService.ACCESS_TOKEN;
import static mw.gov.health.lmis.util.RequestHelper.createUri;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;

import mw.gov.health.lmis.mwrequisition.dto.RequisitionDto;

import java.util.UUID;

public class RequisitionService extends BaseCommunicationService<RequisitionDto> {

  private static final String APPROVE_ENDPOINT = "/approve";

  @Value("${requisition.url}")
  private String requisitionUrl;

  @Override
  protected String getServiceUrl() {
    return requisitionUrl;
  }

  @Override
  protected String getUrl() {
    return "/api/requisitions/";
  }

  @Override
  protected Class<RequisitionDto> getResultClass() {
    return RequisitionDto.class;
  }

  @Override
  protected Class<RequisitionDto[]> getArrayResultClass() {
    return RequisitionDto[].class;
  }

  /**
   * Sends a request to the openlmis-requisition service to approve the requisition of the given
   * UUID.
   * @param uuid the UUID of the requisition to approve
   * @return
   */
  public boolean approve(UUID uuid) {
    String url = getServiceUrl() + getUrl() + uuid.toString() + APPROVE_ENDPOINT ;

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    try {
      restTemplate.postForEntity(createUri(url, parameters), null, Object.class);
    } catch (RestClientException ex) {
      logger.error("Can not approve requisition ", ex);
      return false;
    }
    return true;
  }

  /**
   * Sends a request to the openlmis-requisition service to save the requisition.
   * @param requisitionDto the representation of the object to save
   * @return
   */
  public boolean update(RequisitionDto requisitionDto) {
    String url = getServiceUrl() + getUrl();

    RequestParameters parameters = RequestParameters
        .init()
        .set(ACCESS_TOKEN, authService.obtainAccessToken());

    HttpEntity<RequisitionDto> body = new HttpEntity<>(requisitionDto);

    try {
      restTemplate.put(createUri(url, parameters), body);
    } catch (RestClientException ex) {
      logger.error("Can not save requisition ", ex);
      return false;
    }
    return true;
  }
}
