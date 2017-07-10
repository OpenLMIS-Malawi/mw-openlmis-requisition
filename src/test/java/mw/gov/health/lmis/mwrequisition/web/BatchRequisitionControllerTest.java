package mw.gov.health.lmis.mwrequisition.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import mw.gov.health.lmis.mwrequisition.dto.ApproveRequisitionDto;
import mw.gov.health.lmis.mwrequisition.dto.ApproveRequisitionLineItemDto;
import mw.gov.health.lmis.mwrequisition.dto.OrderableDto;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionDto;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionLineItemDto;
import mw.gov.health.lmis.mwrequisition.dto.RequisitionsProcessingStatusDto;
import mw.gov.health.lmis.mwrequisition.service.RequisitionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class BatchRequisitionControllerTest {
  private static final UUID ORDERABLE_1_ID = UUID.randomUUID();
  private static final UUID ORDERABLE_2_ID = UUID.randomUUID();
  private static final UUID ORDERABLE_3_ID = UUID.randomUUID();
  private static final Map<UUID, RequisitionDto> REQUISITIONS = Maps.newConcurrentMap();

  static {
    OrderableDto orderable1 = new OrderableDto();
    orderable1.setId(ORDERABLE_1_ID);

    OrderableDto orderable2 = new OrderableDto();
    orderable2.setId(ORDERABLE_2_ID);

    OrderableDto orderable3 = new OrderableDto();
    orderable3.setId(ORDERABLE_3_ID);

    REQUISITIONS.computeIfAbsent(
        UUID.randomUUID(),
        key -> create(key, orderable1, false, orderable2, true, orderable3)
    );

    REQUISITIONS.computeIfAbsent(
        UUID.randomUUID(),
        key -> create(key, orderable1, true, orderable2, false, orderable3)
    );

    REQUISITIONS.computeIfAbsent(
        UUID.randomUUID(),
        key -> create(key, orderable1, false, orderable2, false, orderable3)
    );
  }

  private static RequisitionDto create(UUID key,
                                       OrderableDto orderable1, boolean skipped1,
                                       OrderableDto orderable2, boolean skipped2,
                                       OrderableDto orderable3) {
    RequisitionDto dto = new RequisitionDto();
    dto.setId(key);

    RequisitionLineItemDto line1 = new RequisitionLineItemDto();
    line1.setOrderable(orderable1);
    line1.setSkipped(skipped1);

    RequisitionLineItemDto line2 = new RequisitionLineItemDto();
    line2.setOrderable(orderable2);
    line2.setSkipped(skipped2);

    RequisitionLineItemDto line3 = new RequisitionLineItemDto();
    line3.setOrderable(orderable3);
    line3.setSkipped(true);

    dto.setRequisitionLineItems(Lists.newArrayList(line1, line2, line3));

    return dto;
  }

  @Mock
  private RequisitionService requisitionService;

  @InjectMocks
  private BatchRequisitionController controller = new BatchRequisitionController();

  @Before
  public void setUp() throws Exception {
    ReflectionTestUtils.setField(controller, "poolSize", 1);
    when(requisitionService.findOne(any(UUID.class)))
        .thenAnswer(invocation -> REQUISITIONS.get(invocation.getArguments()[0]));
  }

  @Test
  public void shouldRemoveLineItemsIfProductIsSkippedInAllRequisitions() {
    RequisitionsProcessingStatusDto processingStatus = controller
        .retrieve(new ArrayList<>(REQUISITIONS.keySet()));

    assertTrue(null != processingStatus);

    List<ApproveRequisitionDto> requisitions = processingStatus.getRequisitionDtos();

    assertFalse(requisitions.isEmpty());
    assertEquals(requisitions.size(), 3);

    for (ApproveRequisitionDto requisition : requisitions) {
      List<ApproveRequisitionLineItemDto> lineItems = requisition.getRequisitionLineItems();

      assertEquals(lineItems.size(), 2);
      assertEquals(lineItems.get(0).getOrderable().getId(), ORDERABLE_1_ID);
      assertEquals(lineItems.get(1).getOrderable().getId(), ORDERABLE_2_ID);
    }
  }

}