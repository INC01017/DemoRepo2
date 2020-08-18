package com.incture.delfi.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.incture.delfi.constant.Message;
import com.incture.delfi.dao.OrderStatusDaoLocal;
import com.incture.delfi.dao.PaymentTermDaoLocal;
import com.incture.delfi.dao.SalesManCustRelDaoLocal;
import com.incture.delfi.dao.SalesOrderDaoLocal;
import com.incture.delfi.dao.WorkflowDetailsDaoLocal;
import com.incture.delfi.dto.OrderRfcStatusDto;
import com.incture.delfi.dto.OrderStatusDTO;
import com.incture.delfi.dto.PaymentTermsDto;
import com.incture.delfi.dto.ResponseDto;
import com.incture.delfi.dto.SalesOrderDTO;
import com.incture.delfi.dto.SalesOrgDetailsDto;
import com.incture.delfi.dto.WorkflowDetailsDto;
import com.incture.delfi.entity.OrderStatusDO;
import com.incture.delfi.entity.SalesOrderDO;
import com.incture.delfi.uidto.ApprovalDto;
import com.incture.delfi.uidto.GetDraftsDto;
import com.incture.delfi.uidto.SalesOrderKeyDto;

@Service("SalesOrderServices")
@Transactional
public class SalesOrderServices implements SalesOrderServicesLocal {

	private static final Logger logger = LoggerFactory.getLogger(SalesOrderServices.class);

	@Autowired
	private SalesOrderDaoLocal dao;

	@Autowired
	private WorkflowDetailsDaoLocal workflowDet;

	@Autowired
	private HciPostSalesOrderLocal hciPostSalesOrderLocal;

	@Autowired
	private OrderStatusDaoLocal orderStatusDaoLocal;

	@Autowired
	private PaymentTermDaoLocal paymentTermDaoLocal;

	@Autowired
	private HciGetCustomerDetailsServiceLocal hciCustomerService;

	@Autowired
	private SalesManCustRelDaoLocal salesManCustRelDao;

	@Override
	public ResponseDto createSalesOrder(SalesOrderDTO dto) {

		ResponseDto responseDto = new ResponseDto();

		try {

			String term = "";

			// set credit term and pre orders value
			if (!dto.getIsDraft()) {

				term = paymentTermDaoLocal.getTerm(dto.getSoldToCustId());

				if (term != null && !(term.trim().isEmpty())) {

					dto.setCreditTerm(Integer.parseInt(term));

				} else {

					logger.error("Term:networkcall");

					SalesOrgDetailsDto salesOrgDetailsDto = salesManCustRelDao.getSalesOrgDetails(dto.getSalesRep(),
							dto.getCustomerId());

//					term = hciCustomerService
//							.getCustomerPaymentTerms(salesOrgDetailsDto.getSalesOrganization(), dto.getSoldToCustId())
//							.getZfael();
					
					
					 PaymentTermsDto customerPaymentTerms = hciCustomerService
						.getCustomerPaymentTerms(salesOrgDetailsDto.getSalesOrganization(), dto.getSoldToCustId());
					term =customerPaymentTerms.getZfael();

					if (term != null && !(term.trim().isEmpty())) {
						dto.setCreditTerm(Integer.parseInt(term));
					}

				}

				BigDecimal value = orderStatusDaoLocal.getPreviousOrderValues(dto.getSalesRep(), dto.getCustomerId());
				dto.setPreOrdersValue(value);

			}

			if ((dto.getIsDraft()) || ((term != null) && !(term.trim().isEmpty()))) {

				SalesOrderKeyDto salesOrderKeyDto = orderCreation(dto);
				
				if (salesOrderKeyDto.getWorkflowInstanceId() != null
						&& salesOrderKeyDto.getTempSalesOrderNo() != null) {

					dao.updateWorkflowInstanceId(salesOrderKeyDto.getTempSalesOrderNo(),
							salesOrderKeyDto.getWorkflowInstanceId());

				}

				responseDto.setData(salesOrderKeyDto);
				responseDto.setCode(HttpStatus.SC_OK);
				responseDto.setStatus(true);
				responseDto.setMessage(Message.SUCCESS.toString());

			} else {

				responseDto.setStatus(Boolean.FALSE);
				responseDto.setMessage("Payment term is not availble for this customer, cannot proceed.");

			}

		} catch (JSONException e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage("Network failure, Please try after some time");
		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}
 
		return responseDto;
	}

	private SalesOrderKeyDto orderCreation(SalesOrderDTO dto) {
		return dao.createSalesOrder(dto);
	} 

	@Override
	public ResponseDto updateSalesOrder(SalesOrderDTO dto) {
		ResponseDto responseDto = new ResponseDto();
		try {
			dao.updateSalesOrder(dto);
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}
		return responseDto;
	}

	@Override
	public ResponseDto deleteSalesOrder(SalesOrderDTO dto) {
		ResponseDto responseDto = new ResponseDto();
		try {
			dao.deleteSalesOrder(dto);
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}
		return responseDto;
	}

	@Override
	public ResponseDto getSalesOrder(SalesOrderDTO dto) {
		ResponseDto responseDto = new ResponseDto();
		try {
			responseDto.setData(dao.getSalesOrder(dto));
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}
		return responseDto;
	}

	@Override
	public ResponseDto getAllSalesOrder() {

		ResponseDto responseDto = new ResponseDto();
		try {
			responseDto.setData(dao.getAllSalesOrder());
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}
		return responseDto;
	}

	// Customized search
	@Override
	public ResponseDto getSalesOrderBySalesRepAndCustId(SalesOrderDTO dto) {
		ResponseDto responseDto = new ResponseDto();

		try {
			responseDto.setData(dao.getSalesOrderBySalesRepAndCustId(dto));
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}

		return responseDto;
	}

	@Override
	public ResponseDto getTrackingDetailsOfOrder(String tempSalesOrderNo) {
		ResponseDto responseDto = new ResponseDto();

		try {
			responseDto.setData(dao.getTrackingDetailsOfOrder(tempSalesOrderNo));
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}

		return responseDto;
	}

	@Override
	public ResponseDto updateOrderStatus(ApprovalDto dto) {
		ResponseDto responseDto = new ResponseDto();

		try {
			dao.updateOrderStatus(dto);
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}

		return responseDto;
	}

	@Override
	public ResponseDto getDrafts(GetDraftsDto getDraftsDto) {
		ResponseDto responseDto = new ResponseDto();

		try {
			responseDto.setData(dao.getDraftSalesOrders(getDraftsDto));
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}

		return responseDto;
	}

	@Override
	public ResponseDto getAllMatIdList(String salesRep, String customerId) {
		ResponseDto responseDto = new ResponseDto();

		try {
			responseDto.setData(dao.getAllMatIdList(salesRep, customerId));
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}

		return responseDto;
	}

	@Override
	public ResponseDto getCopySalesOrder(SalesOrderDTO dto) {
		ResponseDto responseDto = new ResponseDto();

		try {
			responseDto.setData(dao.getCopySalesOrder(dto));
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}

		return responseDto;
	}

	@Override
	public ResponseDto deleteDraftSalesOrder(Long salesOrderId) {
		ResponseDto responseDto = new ResponseDto();

		try {
			dao.deleteDraftSalesOrder(salesOrderId);
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}

		return responseDto;
	}

	@Override
	public ResponseDto getDraftById(String draftId) {
		ResponseDto responseDto = new ResponseDto();

		try {
			responseDto.setData(dao.getDraftById(draftId));
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}

		return responseDto;
	}

	@Override
	public ResponseDto getSalesOrderByCustId(String customerId) {

		ResponseDto responseDto = new ResponseDto();
		try {
			responseDto.setData(dao.getPendingApproval(customerId));
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}
		return responseDto;
	}

	@Override
	public List<WorkflowDetailsDto> getWfDetails(String orderStatusId) {
		return workflowDet.getworkflowDetailsByorderId(orderStatusId);
	}

	@Override
	public ResponseDto getSalesOrderIdById(String tempSalesOrderNo) {
		ResponseDto responseDto = new ResponseDto();
		try {
			responseDto.setData(dao.getsalesOrderById(tempSalesOrderNo));
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}
		return responseDto;
	}

	@Override
	public ResponseDto deleteDraftsWhichExceeds3days() {

		ResponseDto responseDto = new ResponseDto();

		try {
			responseDto.setData(dao.deleteDraftsWhichExceeds3days());
			responseDto.setStatus(Boolean.TRUE);
			responseDto.setMessage("SUCCESS");
		} catch (Exception e) {
			responseDto.setStatus(Boolean.FALSE);
			responseDto.setMessage(e.getMessage());

			logger.error("[SalesOrderServices][deleteDraftsWhichExceeds3days]" + e.getMessage());

		}

		return responseDto;
	}

	@Override
	public ResponseDto addSecondaryComment(String tempSalesOrderNumber, String remark) {

		ResponseDto responseDto = new ResponseDto();

		try {

			if (tempSalesOrderNumber != null && !tempSalesOrderNumber.isEmpty() && remark != null
					&& !remark.trim().isEmpty()) {

				String comment = dao.getComment(tempSalesOrderNumber);

				if (comment != null) {

					comment = comment + "\n" + remark;

				} else {

					comment = remark;
				}

				dao.updateComment(tempSalesOrderNumber, comment);

			}

			// dao.addSecondaryComment(tempSalesOrderNumber, remark);
			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}
		return responseDto;
	}

	@Override
	public ResponseDto updateTrackOrderInHanaFromRfc() {

		ResponseDto responseDto = new ResponseDto();

		try {

			List<String> idList = dao.getSapOrderIds();

			if (idList != null && !idList.isEmpty()) {

				List<OrderRfcStatusDto> orderRfcStatusList = hciPostSalesOrderLocal.trackSalesOrder(idList);

				if (orderRfcStatusList != null && !orderRfcStatusList.isEmpty()) {

					dao.updateOrderRfcStatus(orderRfcStatusList);

				}

			}

			responseDto.setCode(HttpStatus.SC_OK);
			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}
		return responseDto;

	}

	@Override
	public ResponseDto postFailedOrderToEcc(String tempSalesOrderNumber) {

		ResponseDto responseDto = new ResponseDto();
		SalesOrderDO salesOrderDO = new SalesOrderDO();
		List<Long> orderStatusIdList = new ArrayList<>();

		SalesOrderDTO salesOrderDTO = dao.getSalesOrderByTempId(tempSalesOrderNumber);

		if (salesOrderDTO != null) {

			salesOrderDO.setSalesOrderID(salesOrderDTO.getSalesOrderID());

			List<OrderStatusDTO> orderStatusList = salesOrderDTO.getStatusList();

			if (orderStatusList != null && !orderStatusList.isEmpty()) {

				for (OrderStatusDTO orderStatusDTO : orderStatusList) {

					orderStatusIdList.add(orderStatusDTO.getOrderStatusId());

				}
			}

			if (orderStatusIdList != null && !orderStatusIdList.isEmpty()) {

				workflowDet.deleteTrackByOrderId(orderStatusIdList);

				orderStatusDaoLocal.deleteOrderIdList(orderStatusIdList);

			}

			OrderStatusDO orderStatusDo = new OrderStatusDO();

			orderStatusDo.setApprovedBy("ADMIN");
			orderStatusDo.setApproverComments(" ");
			orderStatusDo.setApprovedDate(new Date());
			orderStatusDo.setPendingWith("");
			orderStatusDo.setStatus("COMPLETED");
			orderStatusDo.setStatusUpdatedBy("SYSTEM");
			orderStatusDo.setStatusUpdatedDate(new Date());

			orderStatusDo.setSalesOrder(salesOrderDO);

			orderStatusDaoLocal.saveOrderStatus(orderStatusDo);

			responseDto = hciPostSalesOrderLocal.pushSalesOrderToEcc(salesOrderDTO);

			dao.updateSapSalesOrderNo(tempSalesOrderNumber, responseDto.getMessage());

		}

		return responseDto;

	}

	@Override
	public ResponseDto deleteOrderTempNo(String tempSalesOrderNo) {

		ResponseDto responseDto = new ResponseDto();

		try {

			if (tempSalesOrderNo != null && !tempSalesOrderNo.trim().isEmpty()) {

				List<Long> orderStatusIdList = new ArrayList<>();

				SalesOrderDO salesOrderDO = dao.getSalesOrderByTempSalesNo(tempSalesOrderNo);

				List<OrderStatusDO> orderStatusList = salesOrderDO.getStatusList();

				if (orderStatusList != null && !orderStatusList.isEmpty()) {

					for (OrderStatusDO orderStatusDO : orderStatusList) {

						orderStatusIdList.add(orderStatusDO.getOrderStatusId());

					}
				}

				if (orderStatusIdList != null && !orderStatusIdList.isEmpty()) {

					workflowDet.deleteTrackByOrderId(orderStatusIdList);

				}

				dao.deleteOrder(salesOrderDO);

			}

			responseDto.setStatus(true);
			responseDto.setMessage(Message.SUCCESS.toString());

		} catch (Exception e) {
			responseDto.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseDto.setStatus(false);
			responseDto.setMessage(Message.FAILED + " " + e.getMessage());
		}

		return responseDto;

	}

	@Override
	public ResponseDto postFailedOrdersListToEcc() {

		ResponseDto responseDto = new ResponseDto();
		responseDto.setStatus(Boolean.TRUE);
		responseDto.setMessage("Success");

		try {

			List<String> orderList = new ArrayList<>();

			orderList = orderStatusDaoLocal.getCompletedOrders();

			logger.error("Completed orders with null sap number:" + orderList.size());

			List<String> failedOrderList = dao.getFailedOrders();

			logger.error("Failed orders due to network:" + failedOrderList.size());

			orderList.addAll(failedOrderList);

			if (orderList != null && !orderList.isEmpty()) {

				List<SalesOrderDTO> salesOrderDTOList = dao.getOrdersBytempSaleOrderIDList(orderList);

				if (salesOrderDTOList != null && !salesOrderDTOList.isEmpty()) {

					for (SalesOrderDTO dto : salesOrderDTOList) {

						// to avoid sap number duplicate
						Thread.sleep(2000);

						ResponseDto response = hciPostSalesOrderLocal.pushSalesOrderToEcc(dto);

						if (response.getMessage().equalsIgnoreCase("order failure")) {
							break;
						}

						dao.updateSapSalesOrderNo(dto.getTempSalesOrderNo(), response.getMessage());

					}

				}

			}

		} catch (Exception e) {
			logger.error("[SalesOrderServices][postFailedOrderListToEcc]" + e.getMessage());
			responseDto.setStatus(Boolean.FALSE);
			responseDto.setMessage(e.getMessage());
		}

		return responseDto;
	}
}
