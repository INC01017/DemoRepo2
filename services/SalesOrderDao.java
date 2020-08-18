package com.incture.delfi.dao;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.Query;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.incture.delfi.constant.DelfiReturnConstants;
import com.incture.delfi.dto.CommentListDto;
import com.incture.delfi.dto.CreditLimitDto;
import com.incture.delfi.dto.CustomerDto;
import com.incture.delfi.dto.OrderRfcStatusDto;
import com.incture.delfi.dto.OrderStatusDTO;
import com.incture.delfi.dto.PaymentTermsDto;
import com.incture.delfi.dto.PendingInvoiceDto;
import com.incture.delfi.dto.PreviousTransactions;
import com.incture.delfi.dto.ResponseDto;
import com.incture.delfi.dto.SalesOrderDTO;
import com.incture.delfi.dto.SalesOrderLineItemDTO;
import com.incture.delfi.dto.SalesOrgDetailsDto;
import com.incture.delfi.dto.SalesRepCustDto;
import com.incture.delfi.dto.WorkflowDetailsDto;
import com.incture.delfi.entity.OrderStatusDO;
import com.incture.delfi.entity.SalesOrderDO;
import com.incture.delfi.entity.SalesOrderLineItemDO;
import com.incture.delfi.entity.StockDetailsDo;
import com.incture.delfi.entity.StockLineItemsDo;
import com.incture.delfi.services.CustomerServiceLocal;
import com.incture.delfi.services.HciGetCustomerDetailsServiceLocal;
import com.incture.delfi.services.HciGetMaterialDetailsServiceLocal;
import com.incture.delfi.services.HciPostSalesOrderLocal;
import com.incture.delfi.services.WorkflowTriggerServiceLocal;
import com.incture.delfi.uidto.ApprovalDto;
import com.incture.delfi.uidto.CategoriesDto;
import com.incture.delfi.uidto.GetDraftsDto;
import com.incture.delfi.uidto.MaterialDto;
import com.incture.delfi.uidto.OrderDetailsDto;
import com.incture.delfi.uidto.PendingApprovalDto;
import com.incture.delfi.uidto.PreviousSalesOrderTransaction;
import com.incture.delfi.uidto.SalesOrderKeyDto;
import com.incture.delfi.uidto.TrackingDto;
import com.incture.delfi.util.JavaMailSender;
import com.incture.delfi.util.ServicesUtil;

@Repository("SalesOrderDao")
public class SalesOrderDao extends BaseDao<SalesOrderDO, SalesOrderDTO> implements SalesOrderDaoLocal {

	private static final Logger logger = LoggerFactory.getLogger(SalesOrderDao.class);

	@Autowired
	private HciGetMaterialDetailsServiceLocal service;

	@Autowired
	private HciGetCustomerDetailsServiceLocal hciGetCustomerService;
	
	@Autowired
	private CustomerDaoLocal customerDaoLocal;

	@Autowired
	private UserDaoLocal userDao;

	@Autowired
	private CustomerServiceLocal customerServicelocal;

	@Autowired
	private WorkflowTriggerServiceLocal workflowTriggerService;

	@Autowired
	private StockDetailsDaoLocal stockDetailsDaoLocal;

	@Autowired
	private WorkflowDetailsDaoLocal workflowDetailsDao;

	@Autowired
	private SalesManCustRelDaoLocal salesManCustRelDaoLocal;

	@Autowired
	private HciPostSalesOrderLocal hciPost;

	@Autowired
	private JavaMailSender notifyMail;

	@Autowired
	private PendingInvoiceDaoLocal pendingInvoiceDaoLocal;

	Map<String, Date> recentSalesMap;
	OrderDetailsDto orderDetailsDto;
	List<MaterialDto> materialDtoList;

	@Override
	public SalesOrderDTO exportDto(SalesOrderDO entity) {

		SalesOrderDTO dto = new SalesOrderDTO();

		if (!ServicesUtil.isEmpty(entity)) {

			dto.setPreOrdersValue(entity.getPreOrdersValue());

			dto.setPlatform(entity.getPlatform());

			dto.setExposure(entity.getExposure());

			dto.setOrderStatusRfc(entity.getOrderStatusRfc() != null ? entity.getOrderStatusRfc() : "");

			dto.setStatusRfcDate(entity.getStatusRfcDate());

			dto.setSoldToCustId(entity.getSoldToCustId());

			dto.setTerms(entity.getTerms());

			dto.setCreditLimit(entity.getCreditLimit());

			dto.setExceedAmount(entity.getExceedAmount());

			dto.setShippingToId(entity.getShippingToId());

			dto.setEccPostError(entity.getEccPostError());

			dto.setRequestedDeliveryDate(entity.getRequestedDeliveryDate());

			//CR change
			dto.setCompleteOrRejectDate(entity.getCompleteOrRejectDate());

			dto.setGuaranteeDate(entity.getGuaranteeDate());

			dto.setComments(entity.getComments());

			dto.setWorkflowInstanceId(entity.getWorkflowInstanceId());

			dto.setCurrency(entity.getCurrency());

			dto.setSalesOrderID(entity.getSalesOrderID());

			dto.setShippingToAddress(entity.getShippingToAddress());

			dto.setCustomerId(entity.getCustomerId());

			dto.setSalesRep(entity.getSalesRep());

			dto.setSAPSalesOrderNo(entity.getSAPSalesOrderNo());

			dto.setTempSalesOrderNo(entity.getTempSalesOrderNo());

			dto.setTotalPrice(entity.getTotalPrice());

			dto.setPoDate(entity.getPoDate());

			dto.setPoNumber(entity.getPoNumber());

			dto.setShippingText(entity.getShippingText());

			List<SalesOrderLineItemDTO> salesItemList = null;

			if (entity.getListOfItems() != null) {

				salesItemList = getItemDetail(entity.getListOfItems(), dto);
			}
			dto.setListOfItems(salesItemList);

			dto.setOrderPlacementDate(entity.getOrderPlacementDate());

			dto.setCreatedBy(entity.getCreatedBy());

			dto.setCreatedDate(entity.getCreatedDate());

			List<OrderStatusDTO> orderStatusDTOList = null;

			if (entity.getStatusList() != null && !entity.getStatusList().isEmpty()) {
				orderStatusDTOList = getStatusDetail(entity.getStatusList(), dto);
			}

			dto.setStatusList(orderStatusDTOList);
			dto.setIsDraft(entity.getIsDraft());
			dto.setDraftId(entity.getDraftId());

		}
		return dto;
	}

	@Override
	public SalesOrderDO importDto(SalesOrderDTO dto) {

		SalesOrderDO entity = new SalesOrderDO();

		if (!ServicesUtil.isEmpty(dto)) {

			entity.setPreOrdersValue(dto.getPreOrdersValue());

			entity.setPlatform(dto.getPlatform());

			entity.setExposure(dto.getExposure());

			entity.setOrderStatusRfc(dto.getOrderStatusRfc() != null ? dto.getOrderStatusRfc() : "");

			entity.setStatusRfcDate(dto.getStatusRfcDate());

			entity.setSoldToCustId(dto.getSoldToCustId());

			entity.setTerms(dto.getTerms());

			entity.setCreditLimit(dto.getCreditLimit());

			entity.setExceedAmount(dto.getExceedAmount());

			entity.setShippingToId(dto.getShippingToId());

			entity.setEccPostError(dto.getEccPostError());

			entity.setRequestedDeliveryDate(dto.getRequestedDeliveryDate());

			//CR change
			entity.setCompleteOrRejectDate(dto.getCompleteOrRejectDate());

			entity.setComments(dto.getComments());

			entity.setCurrency(dto.getCurrency());

			entity.setWorkflowInstanceId(dto.getWorkflowInstanceId());

			entity.setSalesOrderID(dto.getSalesOrderID());

			entity.setShippingToAddress(dto.getShippingToAddress());

			entity.setCustomerId(dto.getCustomerId());

			entity.setSalesRep(dto.getSalesRep());

			entity.setSAPSalesOrderNo(dto.getSAPSalesOrderNo());

			entity.setTempSalesOrderNo(dto.getTempSalesOrderNo());

			entity.setTotalPrice(dto.getTotalPrice());

			entity.setPoDate(dto.getPoDate());

			entity.setPoNumber(dto.getPoNumber());

			entity.setShippingText(dto.getShippingText());

			entity.setGuaranteeDate(dto.getGuaranteeDate());

			List<SalesOrderLineItemDO> salesItemList = null;

			if (dto.getListOfItems() != null && !dto.getListOfItems().isEmpty()) {
				salesItemList = setItemDetail(dto.getListOfItems(), entity);
			}

			entity.setListOfItems(salesItemList);

			if (dto.getOrderPlacementDate() != null) {

				entity.setOrderPlacementDate(ServicesUtil.convertDate(dto.getOrderPlacementDate()));
				entity.setCreatedDate(ServicesUtil.convertDate(dto.getOrderPlacementDate()));

			}
			entity.setCreatedBy(dto.getCreatedBy());

			List<OrderStatusDO> orderStatusDOList = null;

			if (dto.getStatusList() != null) {
				orderStatusDOList = setStatusDetail(dto.getStatusList(), entity);
			}
			entity.setStatusList(orderStatusDOList);

			entity.setIsDraft(dto.getIsDraft());
			entity.setDraftId(dto.getDraftId());
		}
		return entity;
	}

	private List<SalesOrderLineItemDO> setItemDetail(List<SalesOrderLineItemDTO> itemSet, SalesOrderDO entity) {

		List<SalesOrderLineItemDO> setItem = new ArrayList<>();

		for (SalesOrderLineItemDTO itemDto : itemSet) {

			setItem.add(importItemDto(itemDto, entity));
		}
		return setItem;
	}

	private List<OrderStatusDO> setStatusDetail(List<OrderStatusDTO> statusList, SalesOrderDO entity) {

		List<OrderStatusDO> orderStatusDOList = new ArrayList<>();

		for (OrderStatusDTO statusDto : statusList) {

			orderStatusDOList.add(importStatusDto(statusDto, entity));
		}
		return orderStatusDOList;
	}

	public SalesOrderLineItemDO importItemDto(SalesOrderLineItemDTO itemDto, SalesOrderDO entity) {

		SalesOrderLineItemDO item = new SalesOrderLineItemDO();

		// item.setId(itemDto.getId());
		item.setMatId(itemDto.getMatId());
		item.setPrice(itemDto.getPrice());
		item.setSalesOrder(entity);
		item.setUnits(itemDto.getUnits());
		if (itemDto.getUom() != null) {
			item.setUom(itemDto.getUom().toUpperCase());
		}

		if (itemDto.getIsFree() == null || itemDto.getIsFree().equals(Boolean.FALSE)) {
			item.setIsFree(Boolean.FALSE);
		} else if (itemDto.getIsFree()) {
			item.setIsFree(Boolean.TRUE);
		}
		item.setItemCategory(itemDto.getItemCategory());
		item.setTotalItemPrice(itemDto.getTotalItemPrice());
		item.setMatDesc(itemDto.getMatDesc());

		item.setBrand(itemDto.getBrand());
		item.setCategory(itemDto.getCategory());
		item.setTax(itemDto.getTax());
		item.setDiscount1(itemDto.getDiscount1());
		item.setDiscount2(itemDto.getDiscount2());
		item.setDiscount3(itemDto.getDiscount3());

		return item;
	}

	public OrderStatusDO importStatusDto(OrderStatusDTO statusDto, SalesOrderDO entity) {

		OrderStatusDO statusDo = new OrderStatusDO();

		if (statusDto.getOrderStatusId() != null) {
			statusDo.setOrderStatusId(statusDto.getOrderStatusId());
		}

		statusDo.setSalesOrder(entity);
		statusDo.setStatusUpdatedBy(statusDto.getStatusUpdatedBy());
		statusDo.setStatusUpdatedDate(statusDto.getStatusUpdatedDate());
		statusDo.setStatus(statusDto.getStatus());
		statusDo.setPendingWith(statusDto.getPendingWith());
		statusDo.setApprovedBy(statusDto.getApprovedBy());
		statusDo.setApprovedDate(statusDto.getApprovedDate());
		statusDo.setApproverComments(statusDto.getApproverComments());
		statusDo.setApprovalLevel(statusDto.getApprovalLevel());
		return statusDo;
	}

	private List<OrderStatusDTO> getStatusDetail(List<OrderStatusDO> statusList, SalesOrderDTO dto) {

		List<OrderStatusDTO> orderStatusDTOList = new ArrayList<>();

		for (OrderStatusDO statusDo : statusList) {

			orderStatusDTOList.add(exportStatusDo(statusDo, dto));
		}
		return orderStatusDTOList;
	}

	public OrderStatusDTO exportStatusDo(OrderStatusDO statusDo, SalesOrderDTO dto) {
		OrderStatusDTO statusDto = new OrderStatusDTO();
		statusDto.setOrderStatusId(statusDo.getOrderStatusId());
		// statusDto.setSalesOrder(dto);
		statusDto.setStatusUpdatedBy(statusDo.getStatusUpdatedBy());
		statusDto.setStatusUpdatedDate(statusDo.getStatusUpdatedDate());
		statusDto.setStatus(statusDo.getStatus());
		statusDto.setPendingWith(statusDo.getPendingWith());
		statusDto.setApprovedBy(statusDo.getApprovedBy());
		statusDto.setApprovedDate(statusDo.getApprovedDate());
		statusDto.setApproverComments(statusDo.getApproverComments());
		statusDto.setApprovalLevel(statusDo.getApprovalLevel());		
		return statusDto;
	}

	//CR chnage
	private List<SalesOrderLineItemDTO> getItemDetail(List<SalesOrderLineItemDO> itemList, SalesOrderDTO dto) {

		List<SalesOrderLineItemDTO> itemListDto = new ArrayList<>();

		/*for (SalesOrderLineItemDO itemDto : itemList) {

			itemListDto.add(exportItemDto(itemDto, dto));
		}*/
		for(int i=itemList.size()-1;i>=0;i--){
			itemListDto.add(exportItemDto(itemList.get(i), dto));
		}

		return itemListDto;
	}

	public SalesOrderLineItemDTO exportItemDto(SalesOrderLineItemDO itemDo, SalesOrderDTO dto) {

		SalesOrderLineItemDTO item = new SalesOrderLineItemDTO();

		item.setMatId(itemDo.getMatId());
		item.setPrice(itemDo.getPrice());
		if (itemDo.getUom() != null) {
			item.setUom(itemDo.getUom().toUpperCase());
		}
		item.setUnits(itemDo.getUnits());
		item.setIsFree(itemDo.getIsFree());
		item.setTotalItemPrice(itemDo.getTotalItemPrice());
		item.setMatDesc(itemDo.getMatDesc());
		item.setItemCategory(itemDo.getItemCategory());
		item.setBrand(itemDo.getBrand());
		item.setCategory(itemDo.getCategory());
		item.setTax(itemDo.getTax());
		item.setDiscount1(itemDo.getDiscount1());
		item.setDiscount2(itemDo.getDiscount2());
		item.setDiscount3(itemDo.getDiscount3());

		return item;
	}

	// this method is used to create sales order
	public SalesOrderKeyDto createSalesOrder(SalesOrderDTO dto) {

		SalesOrderKeyDto orderKey = new SalesOrderKeyDto();
		String tempSalesOrderNo = "";

		if (!dto.getIsDraft()) {

			if (ServicesUtil.isEmpty(dto.getDraftId())) {

				SalesOrderDTO salerOrderPosted = new SalesOrderDTO();

				tempSalesOrderNo = "S" + ServicesUtil.getLoggedInUser(dto.getSalesRep()) + System.currentTimeMillis();

				dto.setTempSalesOrderNo(tempSalesOrderNo);

				salerOrderPosted = triggerWFandPostECC(dto);

				orderKey.setTempSalesOrderNo(tempSalesOrderNo);
				orderKey.setSAPSalesOrderNo(salerOrderPosted.getSAPSalesOrderNo());
				orderKey.setWorkflowInstanceId(salerOrderPosted.getWorkflowInstanceId());

			} else {

				tempSalesOrderNo = "S" + ServicesUtil.getLoggedInUser(dto.getSalesRep()) + System.currentTimeMillis();

				dto.setTempSalesOrderNo(tempSalesOrderNo);

				CustomerDto customerDto = new CustomerDto();
				customerDto.setCustId(dto.getCustomerId());
				CustomerDto custDto = customerDaoLocal.getCustomer(customerDto);
				Boolean crFlag = custDto.getCrFlag();
				
				SalesOrgDetailsDto salesOrgDetailsDto = salesManCustRelDaoLocal.getSalesOrgDetails(dto.getSalesRep(),
						dto.getCustomerId());
				
				 PaymentTermsDto customerPaymentTerms = hciGetCustomerService
							.getCustomerPaymentTerms(salesOrgDetailsDto.getSalesOrganization(), dto.getSoldToCustId());
				boolean isExceedingDays = isExceedingDays(ServicesUtil.convertDate(dto.getOrderPlacementDate()),custDto.getSpCustId(),customerPaymentTerms);

				long exceedsDays = getAgingDays(ServicesUtil.convertDate(dto.getOrderPlacementDate()),
						dto.getCreditTerm(), custDto.getSpCustId());

				int exceedAmt = dto.getTotalPrice().compareTo(dto.getCreditLimit());

				if (exceedAmt < 0) {
					exceedAmt = 0;
				}

//				if (!crFlag && (dto.getCreditLimit() != null) && !(exceedAmt == 0 && exceedsDays == 0)) {
//					if (dto.getTotalPrice().compareTo(dto.getCreditLimit()) > -1 || exceedsDays > -1) {
					
				if (!crFlag && (dto.getCreditLimit() != null) && !(exceedAmt == 0 && !isExceedingDays)) {
					if (dto.getTotalPrice().compareTo(dto.getCreditLimit()) > -1 || isExceedingDays) {
						
						

						List<OrderStatusDTO> orderStatusDTOList = new ArrayList<>();

						OrderStatusDTO orderStatusDTO = new OrderStatusDTO();

						orderStatusDTOList.add(orderStatusDTO);
						dto.setStatusList(orderStatusDTOList);

						dto.setDraftId("");
						dto.setExceedAmount(dto.getTotalPrice().subtract(dto.getCreditLimit()));

						// delete items of draft
						String query = "delete from SalesOrderLineItemDO s where  s.salesOrder.salesOrderID=:salesOrderID";
						Query q = getSession().createQuery(query);
						q.setParameter("salesOrderID", dto.getSalesOrderID());
						q.executeUpdate();

						getSession().update(importDto(dto));

						Long orderStatusId = getSalesOrderByTempId(tempSalesOrderNo).getStatusList().get(0)
								.getOrderStatusId();

						JSONObject inputJsonObj = new JSONObject();

						JSONObject context = new JSONObject();
						inputJsonObj.put("definitionId", "delfi_wf");
						JSONObject conditions = new JSONObject();
						conditions.put("__type__", "Condition");
						if (dto.getTotalPrice().subtract(dto.getCreditLimit()).signum() == 1) {
							conditions.put("CreaditLimit", dto.getTotalPrice().subtract(dto.getCreditLimit()));
						} else {
							conditions.put("CreaditLimit", 0);
						}

						if (exceedsDays > -1) {
							conditions.put("CreaditTerm", exceedsDays);
						} else {
							conditions.put("CreaditTerm", 0);
						}
						context.put("Conditions", conditions);
						context.put("SalesOrderNo", tempSalesOrderNo);
						context.put("ORDER_STATUS_ID", orderStatusId);
						context.put("createdBy", dto.getCreatedBy());
						context.put("loginUser", dto.getSalesRep());
						context.put("custCode", custDto.getCustId());
						context.put("custName", custDto.getCustName());

						inputJsonObj.put("context", context);

						String response = workflowTriggerService.triggerWorkflow(inputJsonObj.toString());

						JSONObject responseObject = new JSONObject(response);

						orderKey.setTempSalesOrderNo(tempSalesOrderNo);

						if (responseObject != null) {

							logger.error(tempSalesOrderNo + ":workflow ID inside:" + responseObject.getString("id"));
							orderKey.setWorkflowInstanceId(responseObject.getString("id"));
						}

					} else {

						orderKey = autoApprovalsOfDraft(dto);
					}
				} else {

					orderKey = autoApprovalsOfDraft(dto);

				}
			}

		} else {

			if (ServicesUtil.isEmpty(dto.getDraftId())) {

				List<String> draftIdList = new ArrayList<>();

				List<SalesOrderDTO> salesOrderDTOList = splitDraftIntoMultples(dto);

				for (SalesOrderDTO salesOrderDTO : salesOrderDTOList) {

					String draftId = "D" + ServicesUtil.getLoggedInUser(dto.getSalesRep()) + System.currentTimeMillis();

					draftIdList.add(draftId);

					salesOrderDTO.setDraftId(draftId);

					getSession().save(importDto(salesOrderDTO));

				}

				orderKey.setDraftIdList(draftIdList);

			} else {

				updateDraft(dto);
			}

		}

		return orderKey;
	}

	// this is for draft make as sales order
	private SalesOrderKeyDto autoApprovalsOfDraft(SalesOrderDTO dto) {

		Map<String, String> settingMap = new HashMap<>();

		SalesOrderKeyDto salesOrderKeyDto = new SalesOrderKeyDto();

		List<OrderStatusDTO> orderStatusDtoList = new ArrayList<>();

		ResponseDto responseDto = hciPost.pushSalesOrderToEcc(dto);
		String sapSalesOrderNo = "";
		if (responseDto != null) {

			sapSalesOrderNo = responseDto.getMessage();
		}

		logger.error(dto.getTempSalesOrderNo() + ":auto:" + sapSalesOrderNo);

		dto.setSAPSalesOrderNo(sapSalesOrderNo);
		OrderStatusDTO orderStatusDto = new OrderStatusDTO();
		orderStatusDto.setApprovedBy("SYSTEM");
		orderStatusDto.setApproverComments(" ");
		orderStatusDto.setApprovedDate(new Date());
		orderStatusDto.setPendingWith("");
		orderStatusDto.setStatus("COMPLETED");
		orderStatusDto.setStatusUpdatedBy("SYSTEM");
		orderStatusDto.setStatusUpdatedDate(new Date());

		// orderStatusDtoList.clear();
		orderStatusDtoList.add(orderStatusDto);

		dto.setDraftId("");
		dto.setStatusList(orderStatusDtoList);

		// delete line times of Draft
		String query = "delete from SalesOrderLineItemDO s where  s.salesOrder.salesOrderID=:salesOrderID";
		Query q = getSession().createQuery(query);
		q.setParameter("salesOrderID", dto.getSalesOrderID());
		q.executeUpdate();

		// update draft as salesOrder in hana DB
		getSession().update(importDto(dto));

		salesOrderKeyDto.setSAPSalesOrderNo(sapSalesOrderNo);
		salesOrderKeyDto.setTempSalesOrderNo(dto.getTempSalesOrderNo());

		if (sapSalesOrderNo != null && !sapSalesOrderNo.trim().isEmpty()) {

			settingMap = buildMail(dto.getSalesRep(), sapSalesOrderNo, dto.getCreditLimit());

			notifyMail.sendMailCloud(settingMap.get("recipient"), settingMap.get("subject"),
					settingMap.get("mailBody"));

		}

		return salesOrderKeyDto;
	}

	private Map<String, String> buildMail(String requester, String sapOrderId, BigDecimal creditLimit) {

		Map<String, String> mailMap = new HashMap<>();

		mailMap.put("subject", "OrderApprovals : Order with ID: " + sapOrderId + " is Auto Approved");
		mailMap.put("recipient", requester);

		StringBuffer html = new StringBuffer();

		html.append("<p>Dear Requester,");
		html.append("<br><br>");
		html.append("Order with ID: ");
		html.append(sapOrderId);
		html.append("  is Auto Approved");
		html.append("<br><br>");
		html.append("Best regards,");
		html.append("<br>");
		html.append("<font color='EE104C'><i><b>Delfi Team</b></i></font>");
		html.append("<br><br>");
		html.append("<font color='5D90AC'>");
		html.append("Note: This is auto generated email please do not reply.</font>");
		html.append("<br><br><br>");
		html.append("</p>");

		mailMap.put("mailBody", new String(html));

		return mailMap;
	}

	private SalesOrderDTO triggerWFandPostECC(SalesOrderDTO dto) {

		Long orderStatusId = 0L;
		List<OrderStatusDTO> orderStatusDtoList = new ArrayList<>();
		OrderStatusDTO newOrderStatusDTO = new OrderStatusDTO();

		CustomerDto customerDto = new CustomerDto();

		customerDto.setCustId(dto.getCustomerId());

		CustomerDto custDto = customerDaoLocal.getCustomer(customerDto);

		Boolean crFlag = custDto.getCrFlag();

		SalesOrgDetailsDto salesOrgDetailsDto = salesManCustRelDaoLocal.getSalesOrgDetails(dto.getSalesRep(),
				dto.getCustomerId());
		
		PaymentTermsDto customerPaymentTerms = hciGetCustomerService
					.getCustomerPaymentTerms(salesOrgDetailsDto.getSalesOrganization(), dto.getSoldToCustId());
		boolean isExceedingDays = isExceedingDays(ServicesUtil.convertDate(dto.getOrderPlacementDate()),custDto.getSpCustId(),customerPaymentTerms);
		
		long exceedsDays = getAgingDays(ServicesUtil.convertDate(dto.getOrderPlacementDate()), dto.getCreditTerm(),
						custDto.getSpCustId());
		int exceedAmt = dto.getTotalPrice().compareTo(dto.getCreditLimit());
		//logger.error("Is exceeding Days: " + isExceedingDays + "exceedAmount: "+exceedAmt );
		
		if (exceedAmt < 0) {
			exceedAmt = 0;
		}

		
		
		
		// checking crFlag 
		//if (!crFlag && (dto.getCreditLimit() != null) && !(exceedAmt == 0 && exceedsDays == 0)) {// why giving auto approval here @0
		//if (dto.getTotalPrice().compareTo(dto.getCreditLimit()) > -1 || exceedsDays > -1) {// why asking auto approval here @ 0
		
		
		  if (!crFlag && (dto.getCreditLimit() != null) && !(exceedAmt == 0 && !isExceedingDays)) {
			  if (dto.getTotalPrice().compareTo(dto.getCreditLimit()) > -1 || isExceedingDays) {
			  		
			  		
			  		
				// Here we are triggering workflow services

				orderStatusDtoList.add(newOrderStatusDTO);
				dto.setStatusList(orderStatusDtoList);
				dto.setExceedAmount(dto.getTotalPrice().subtract(dto.getCreditLimit()));
				getSession().persist(importDto(dto));

				orderStatusId = getSalesOrderByTempId(dto.getTempSalesOrderNo()).getStatusList().get(0)
						.getOrderStatusId();

				JSONObject inputJsonObj = new JSONObject();

				JSONObject context = new JSONObject();
				inputJsonObj.put("definitionId", "delfi_wf");
				JSONObject conditions = new JSONObject();

				conditions.put("__type__", "Condition");
				if (dto.getTotalPrice().subtract(dto.getCreditLimit()).signum() == 1) {
					conditions.put("CreaditLimit", dto.getTotalPrice().subtract(dto.getCreditLimit()));
				} else {
					conditions.put("CreaditLimit", 0);
				}
				if (exceedsDays > -1) {
					conditions.put("CreaditTerm", exceedsDays);
				} else {
					conditions.put("CreaditTerm", 0);
				}

				context.put("Conditions", conditions);
				context.put("SalesOrderNo", dto.getTempSalesOrderNo());
				context.put("ORDER_STATUS_ID", orderStatusId);
				context.put("createdBy", dto.getCreatedBy());
				context.put("loginUser", dto.getSalesRep());
				context.put("custCode", custDto.getCustId());
				context.put("custName", custDto.getCustName());

				inputJsonObj.put("context", context);

				String response = workflowTriggerService.triggerWorkflow(inputJsonObj.toString());

				JSONObject responseObject = new JSONObject(response);

				if (responseObject != null) {
					dto.setWorkflowInstanceId(responseObject.getString("id"));
					logger.error(dto.getTempSalesOrderNo() + ":workflow ID Direct:" + responseObject.getString("id"));
				}

			} else {

				dto = autoApprovals(dto);

			}

		} else {

			dto = autoApprovals(dto);

		}

		return dto;
	}

	private boolean isExceedingDays(Date soDate, String custId,
			PaymentTermsDto customerPaymentTerms) {
		
		boolean exceedingDays = false;
	
		List<String>  list = new  ArrayList<>();
		list.add(custId);
		 List<PendingInvoiceDto> pendingInvoices = pendingInvoiceDaoLocal.getPendingInvoicesNew(list);
				//getFirstOfCreditMonth(custId); 

		for (PendingInvoiceDto pendingInvoiceDto : pendingInvoices) {
			LocalDate creditMonth= null;
			LocalDate localSODate= null;
			LocalDate creditMonthAndCreditTerm = null;
			
			BigDecimal zero = new BigDecimal(0);
			int value = pendingInvoiceDto.getAmount().compareTo(zero);
			if(value>0){
				
				Date date = ServicesUtil.convertDate(pendingInvoiceDto.getDocumentDate());
				Instant instant = date.toInstant();
				if(!customerPaymentTerms.getZterm().contains("CZ")){
					creditMonth = instant.atZone(ZoneId.systemDefault()).toLocalDate().with(TemporalAdjusters.firstDayOfNextMonth());  //  jump to next month
					 creditMonthAndCreditTerm = creditMonth.plusDays(Integer.parseInt(customerPaymentTerms.getZfael()) + 1); // add the Zfael 
				}else{
					int numberOfDays = Integer.parseInt(customerPaymentTerms.getZfael());		// will always be 30
					creditMonth = instant.atZone(ZoneId.systemDefault()).toLocalDate();			//document date format conversion
					creditMonthAndCreditTerm = creditMonth.plusDays(numberOfDays+ Integer.parseInt(customerPaymentTerms.getZfael())); // 1 Day deviation possible...please confirm
					
					//22 JAn--> 22 JAn + 30 = 22 FEB + 30->22 March +1-> 23 MArch;
				}

				localSODate = soDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				long daysDifference = ChronoUnit.DAYS.between(creditMonthAndCreditTerm, localSODate);
				
//				logger.error("Document Date which causes blocking : " + pendingInvoiceDto.getDocumentDate() 
//				+ "  Credit Term of customer: "+ customerPaymentTerms.getZfael() + 1
//				+ "  Credit ActualTerm of customer: "+ customerPaymentTerms.getZfael() + 1
//				+ "  Last date of payment :"+ customerPaymentTerms.getZterm()
//				+ "  Difference in Days: "+ daysDifference);
				
				if(daysDifference > 0){
					exceedingDays = true; // approval required
					logger.error("Above one blocks this ");
					
					break;
				}
				
			}
			
			
		}
return exceedingDays;
			
	}
	
	
	private long getAgingDays(Date soDate, int creditTerm, String custId) {
		
		
		LocalDate creditMonth = pendingInvoiceDaoLocal.getFirstOfCreditMonth(custId); 

		LocalDate creditMonthAndCreditTerm = creditMonth.plusDays(creditTerm + 1); // CZ60 needs to be added

		LocalDate localSODate = soDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		return ChronoUnit.DAYS.between(creditMonthAndCreditTerm, localSODate);	
	}

	// it is for autoApprovals
	private SalesOrderDTO autoApprovals(SalesOrderDTO dto) {

		Map<String, String> settingMap = new HashMap<>();

		List<OrderStatusDTO> orderStatusDTOList = new ArrayList<>();

		ResponseDto responseDto = hciPost.pushSalesOrderToEcc(dto);
		String sapSalesOrderNo = "";

		if (responseDto != null) {
			sapSalesOrderNo = responseDto.getMessage();
		}

		logger.error(dto.getTempSalesOrderNo() + ":auto approve:" + sapSalesOrderNo);

		dto.setSAPSalesOrderNo(sapSalesOrderNo);

		OrderStatusDTO orderStatusDto = new OrderStatusDTO();

		orderStatusDto.setApprovedBy("SYSTEM");
		orderStatusDto.setApproverComments(" ");
		orderStatusDto.setApprovedDate(new Date());
		orderStatusDto.setPendingWith("");
		orderStatusDto.setStatus("COMPLETED");
		orderStatusDto.setStatusUpdatedBy("SYSTEM");
		orderStatusDto.setStatusUpdatedDate(new Date());

		orderStatusDTOList.add(orderStatusDto);
		dto.setStatusList(orderStatusDTOList);

		// save salesOrder
		getSession().persist(importDto(dto));

		if (sapSalesOrderNo != null && !(sapSalesOrderNo.trim().isEmpty())) {

			settingMap = buildMail(dto.getSalesRep(), sapSalesOrderNo, dto.getCreditLimit());

			notifyMail.sendMailCloud(settingMap.get("recipient"), settingMap.get("subject"),
					settingMap.get("mailBody"));

		}

		return dto;
	}

	public void updateSalesOrder(SalesOrderDTO dto) {

		getSession().update(importDto(dto));

	}

	public void deleteSalesOrder(SalesOrderDTO dto) {

		getSession().delete(importDto(dto));

	}

	@Override
	public void deleteOrder(SalesOrderDO entity) {

		getSession().delete(entity);

	}

	@Override
	public SalesOrderDTO getSalesOrder(SalesOrderDTO dto) {
		SalesOrderDO entity = (SalesOrderDO) getSession().get(SalesOrderDO.class, dto.getSalesOrderID());
		return exportDto(entity);
	}

	@SuppressWarnings("unchecked")
	public List<SalesOrderDTO> getAllSalesOrder() {

		List<SalesOrderDTO> salesOrderDTOList = new ArrayList<>();

		Query q = getSession().createQuery("from SalesOrderDO ");

		List<SalesOrderDO> salesOrderDOList = q.list();

		for (SalesOrderDO salesOrderDO : salesOrderDOList) {
			salesOrderDTOList.add(exportDto(salesOrderDO));
		}
		return salesOrderDTOList;
	}

	@SuppressWarnings("unchecked")
	public List<OrderDetailsDto> getSalesOrderBySalesRepAndCustId(SalesOrderDTO dto) {

		List<OrderDetailsDto> orderDetailsList = new ArrayList<>();

		if (dto.getSalesRep() != null && !dto.getSalesRep().equals("") && dto.getCustomerId() != null
				&& !dto.getCustomerId().equals("")) {

			List<StockDetailsDo> stockDetailsDoList = new ArrayList<>();

			String hqlQuery;

			if (dto.getShippingToId() != null && !dto.getShippingToId().trim().isEmpty()) {

				hqlQuery = "select sro from SalesOrderDO sro where  sro.customerId=:customerId and sro.salesRep=:salesRep "
						+ " and sro.isDraft=:isDraft  and sro.shippingToId='" + dto.getShippingToId()
						+ "'  order by orderPlacementDate desc";
			} else {

				hqlQuery = "select sro from SalesOrderDO sro where sro.customerId=:customerId and sro.salesRep=:salesRep "
						+ "  and sro.isDraft=:isDraft  order by orderPlacementDate desc";

			}

			Query q = getSession().createQuery(hqlQuery);

			q.setParameter("customerId", dto.getCustomerId());
			q.setParameter("isDraft", Boolean.FALSE);
			q.setParameter("salesRep", dto.getSalesRep());
			q.setFirstResult(0);
			q.setMaxResults(3);

			List<SalesOrderDO> salesOrderDOList = q.list();

			try {

				if (salesOrderDOList != null && !salesOrderDOList.isEmpty()) {

					for (SalesOrderDO salesOrderDO : salesOrderDOList) {

						stockDetailsDoList = stockDetailsDaoLocal.getStockForSalesByDate(salesOrderDO.getSalesRep(),
								salesOrderDO.getCustomerId(), salesOrderDO.getOrderPlacementDate());

						orderDetailsDto = new OrderDetailsDto();

						orderDetailsDto.setTempSalesOrderNo(salesOrderDO.getTempSalesOrderNo());
						orderDetailsDto.setOrderPlacementDate(salesOrderDO.getOrderPlacementDate());
						orderDetailsDto.setTotalPrice(salesOrderDO.getTotalPrice());
						orderDetailsDto.setCurrency(salesOrderDO.getCurrency());

						List<CategoriesDto> categoriesList = new ArrayList<>();

						List<SalesOrderLineItemDO> salesOrderLineItemList = salesOrderDO.getListOfItems();

						if (!ServicesUtil.isEmpty(salesOrderLineItemList)) {

							Map<String, Integer> map = new HashMap<>();

							for (SalesOrderLineItemDO salesOrderLineItemDO : salesOrderLineItemList) {
                               
								if (map.containsKey(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
								+ salesOrderLineItemDO.getIsFree())) {
									int count = map.get(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
									+ salesOrderLineItemDO.getIsFree());
									map.put(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
									+ salesOrderLineItemDO.getIsFree(), count + 1);
								} else {
									map.put(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
									+ salesOrderLineItemDO.getIsFree(), 1);
								}

								if (map.get(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
								+ salesOrderLineItemDO.getIsFree()) == 1) {

									CategoriesDto categoriesDto = new CategoriesDto();
									categoriesDto.setMatId(salesOrderLineItemDO.getMatId());
									categoriesDto.setMatDesc(salesOrderLineItemDO.getMatDesc());
									categoriesDto.setIsFree(salesOrderLineItemDO.getIsFree());
									categoriesDto.setBrand(salesOrderLineItemDO.getBrand());
									categoriesDto.setTax(salesOrderLineItemDO.getTax());
									categoriesDto.setCategory(salesOrderLineItemDO.getCategory());
									categoriesDto.setDiscount1(salesOrderLineItemDO.getDiscount1());
									categoriesDto.setDiscount2(salesOrderLineItemDO.getDiscount2());
									categoriesDto.setDiscount3(salesOrderLineItemDO.getDiscount3());
									categoriesDto.setUom(salesOrderLineItemDO.getUom());
									categoriesDto.setUnits(salesOrderLineItemDO.getUnits());
									categoriesDto.setUnitPrice(salesOrderLineItemDO.getPrice());
									categoriesDto.setNetPrice(salesOrderLineItemDO.getTotalItemPrice());
									categoriesDto.setItemCategory(salesOrderLineItemDO.getItemCategory());

									if (!salesOrderLineItemDO.getIsFree()) {

										categoriesDto.setListOfPreviousTransaction(getPrevoiusTransactionForGetStock(
												stockDetailsDoList, salesOrderLineItemDO.getMatId(),
												salesOrderLineItemDO.getUom()));
									}
									categoriesList.add(categoriesDto);
								}
							}
						}
						orderDetailsDto.setListOfCategories(categoriesList);
						orderDetailsList.add(orderDetailsDto);
					}
					// }
				}
			} catch (Exception e) {
				logger.error("[SalesOrderDao][getSalesOrder]:::" + e.getMessage());
			}

		}
		return orderDetailsList;
	}

	public List<TrackingDto> getTrackingDetailsOfOrder(String tempSalesOrderNo) {

		List<TrackingDto> resultList = new ArrayList<>();
		List<WorkflowDetailsDto> workflowList = new ArrayList<>();
		Long orderStatusId = 0L;
		try {
			String query = "select sod from SalesOrderDO sod where sod.tempSalesOrderNo=:tempSalesOrderNo";
			Query q = getSession().createQuery(query);
			q.setParameter("tempSalesOrderNo", tempSalesOrderNo.trim());

			SalesOrderDO salesOrderDo = (SalesOrderDO) q.uniqueResult();

			List<OrderStatusDO> orderStatusDOList = salesOrderDo.getStatusList();

			orderStatusId = orderStatusDOList.get(0).getOrderStatusId();
			workflowList = workflowDetailsDao.getworkflowDetailsByorderId(orderStatusId + "");

			TrackingDto trackingDto;

			if (!ServicesUtil.isEmpty(orderStatusDOList)) {

				if (orderStatusDOList.get(0).getStatusUpdatedBy().equalsIgnoreCase("SYSTEM")) {
					String name = "";
					name = userDao.getUserById(salesOrderDo.getCreatedBy()).getUserName();
					trackingDto = new TrackingDto();
					trackingDto.setCreatedBy(salesOrderDo.getCreatedBy());
					trackingDto.setCreatedDate(salesOrderDo.getCreatedDate());
					trackingDto.setTitle("Request Created :" + name);
					trackingDto.setCreatedByName(name);
					trackingDto.setApproverComments(salesOrderDo.getComments());
					resultList.add(trackingDto);
					for (OrderStatusDO orderStatusDO : orderStatusDOList) {
						name = userDao.getUserById(orderStatusDO.getStatusUpdatedBy()).getUserName();
						trackingDto = new TrackingDto();
						trackingDto.setTitle(orderStatusDO.getApprovedBy());
						trackingDto.setApprovedDate(orderStatusDO.getApprovedDate());
						trackingDto.setOrderStatus(orderStatusDO.getStatus());
						trackingDto.setPendingWith(orderStatusDO.getPendingWith());
						trackingDto.setApproverName(name);
						trackingDto.setOrderStatusId(orderStatusDO.getOrderStatusId());
						trackingDto.setApproverComments(orderStatusDO.getApproverComments());
						trackingDto.setApproverId(orderStatusDO.getApprovedBy());
						trackingDto.setTitle("Request completed by :" + name);
						resultList.add(trackingDto);

					}
				} else {
					int counter = 0;
					for (WorkflowDetailsDto workflowDetailsDto : workflowList) {
						String name = "";
						trackingDto = new TrackingDto();
						if (counter == 0) {
							name = userDao.getUserById(workflowDetailsDto.getUpdatedBy()).getUserName();
							trackingDto.setCreatedBy(workflowDetailsDto.getUpdatedBy());
							trackingDto.setCreatedDate(workflowDetailsDto.getUpdatedDate());
							trackingDto.setPendingWith(workflowDetailsDto.getPendingWith());
							trackingDto.setTitle("Request Created By :" + name);
							trackingDto.setCreatedByName(name);
							trackingDto.setOrderStatus(workflowDetailsDto.getWorkflowStatus());
							trackingDto.setOrderStatusId(Long.parseLong(workflowDetailsDto.getOrderStatusId() + ""));
							trackingDto.setApproverComments(salesOrderDo.getComments());

							resultList.add(trackingDto);
							counter++;
						} else {

							name = userDao.getUserById(workflowDetailsDto.getUpdatedBy()).getUserName();
							trackingDto.setApprovedDate(workflowDetailsDto.getApprovedDate());
							trackingDto.setOrderStatus(workflowDetailsDto.getWorkflowStatus());
							trackingDto.setPendingWith(workflowDetailsDto.getPendingWith());
							trackingDto.setOrderStatusId(Long.parseLong(workflowDetailsDto.getOrderStatusId() + ""));
							trackingDto.setApproverName(name);
							if (workflowDetailsDto.getWorkflowStatus().equalsIgnoreCase("Completed")
									|| workflowDetailsDto.getWorkflowStatus().equalsIgnoreCase("Inprogress")) {
								trackingDto.setTitle("Request Completed by :" + name);
							} else {
								trackingDto.setTitle("Request Rejected by :" + name);
							}
							trackingDto.setApproverId(workflowDetailsDto.getUpdatedBy());
							trackingDto.setApproverComments(workflowDetailsDto.getApproverComments());
							resultList.add(trackingDto);
							counter++;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("[SalesOrderDao][getTrackingDetailsOfOrder]" +tempSalesOrderNo +":"+e.getMessage());
		}

		return resultList;

	}

	@Override
	public void updateOrderStatus(ApprovalDto approvalDto) {

		if (!ServicesUtil.isEmpty(approvalDto.getTempSalesOrderNo())) {

			Query query = getSession()
					.createQuery("from SalesOrderDO sod where sod.tempSalesOrderNo=:tempSalesOrderNo");
			query.setParameter("tempSalesOrderNo", approvalDto.getTempSalesOrderNo());
			SalesOrderDO resultSalesOrderDO = (SalesOrderDO) query.uniqueResult();
			boolean check = true;

			List<OrderStatusDO> orderStatusDOList = new ArrayList<>();
			orderStatusDOList = resultSalesOrderDO.getStatusList();

			for (OrderStatusDO orderStatusDO : orderStatusDOList) {
				if (orderStatusDO.getStatus().equals("Approved")) {
					check = false;
					break;
				}
			}

			for (OrderStatusDO orderStatusDO : orderStatusDOList) {

				if (orderStatusDO.getPendingWith() != null && !orderStatusDO.getPendingWith().equals("")) {
					orderStatusDO.setPendingWith("");
					getSession().update(orderStatusDO);

				}

			}

			if (check) {

				OrderStatusDO orderStatusDO = new OrderStatusDO();

				String sapSalesOrderNo = hciPost.pushSalesOrderToEcc(exportDto(resultSalesOrderDO)).getMessage();

				resultSalesOrderDO.setSAPSalesOrderNo(sapSalesOrderNo);
				logger.error("Sap order No :" + sapSalesOrderNo);
				orderStatusDO.setSalesOrder(resultSalesOrderDO);
				orderStatusDO.setApprovedBy(approvalDto.getApprovedBy());
				orderStatusDO.setApproverComments(approvalDto.getApproverComments());
				orderStatusDO.setApprovedDate(new Date());
				orderStatusDO.setPendingWith("");
				orderStatusDO.setStatus(approvalDto.getApprovalStatus());
				orderStatusDO.setStatusUpdatedBy(approvalDto.getApprovedBy());
				orderStatusDO.setStatusUpdatedDate(new Date());

				getSession().save(orderStatusDO);
			}

			logger.error("order status list set");
			getSession().update(resultSalesOrderDO);
			logger.error("updated sales order");

		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PreviousSalesOrderTransaction> getPrevoiusTxByMatId(String matId, String customerId, String salesRep) {

		SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");

		List<PreviousSalesOrderTransaction> previousTransactions = new ArrayList<>();

		try {

			recentSalesMap = getRecentSales(salesRep, customerId, null);

			Set<Entry<String, Date>> entrySet = recentSalesMap.entrySet();

			for (Entry<String, Date> entry : entrySet) {

				String query1 = "select sol.price,sol.units,sol.uom from SalesOrderLineItemDO sol , SalesOrderDO srd "
						+ " where  sol.salesOrder.salesOrderID=srd.salesOrderID "
						+ " and sol.matId=:matId and srd.tempSalesOrderNo=:tempSalesOrderNo";

				Query q1 = getSession().createQuery(query1);
				q1.setParameter("matId", matId);
				q1.setParameter("tempSalesOrderNo", entry.getKey());
				List<Object[]> priceAndUnitList = q1.list();

				if (priceAndUnitList != null) {

					for (Object[] priceAndUnit : priceAndUnitList) {

						PreviousSalesOrderTransaction previousTransaction = new PreviousSalesOrderTransaction();
						previousTransaction.setTransactionId(entry.getKey());
						previousTransaction.setTransactionDate(dateFormater.format(entry.getValue()));
						previousTransaction.setUnitPrice((BigDecimal) priceAndUnit[0]);
						previousTransaction.setUnits((Long) priceAndUnit[1]);
						previousTransaction.setUOM((String) priceAndUnit[2]);

						// add to list
						previousTransactions.add(previousTransaction);
					}
				}

			}
		} catch (Exception e) {
			logger.error("[SalesOrderDao][getPrevoiusTxByMatId]" + e.getMessage());
		}

		return previousTransactions;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesOrderDTO> getDraftSalesOrders(GetDraftsDto getDraftDto) {

		// logger.error("inside draft");

		List<SalesOrderDTO> salesOrderDTOList = new ArrayList<>();
		SalesOrderDTO salesOrderDTO;

		try {

			if (getDraftDto.getIsDraft()) {

				String query = "select sod from SalesOrderDO sod where  sod.salesRep=:salesRep and sod.isDraft=:isDraft order by sod.orderPlacementDate desc";

				Query q = getSession().createQuery(query);
				q.setParameter("salesRep", getDraftDto.getSalesRep());
				q.setParameter("isDraft", getDraftDto.getIsDraft());

				List<SalesOrderDO> salesOrderDOList = q.list();

				if (!ServicesUtil.isEmpty(salesOrderDOList)) {

					CustomerDto customerDto = new CustomerDto();

					Map<String, CustomerDto> customerDtoMap = new HashMap<>();
					List<CustomerDto> customerDtoList = new ArrayList<>();

					List<String> customerList = new ArrayList<>();

					for (SalesOrderDO s : salesOrderDOList) {

						customerList.add(s.getCustomerId());
					}

					Map<String, CreditLimitDto> resultMap = new HashMap<>();

					Map<String, String> shipToSoldMap = new HashMap<>();

					if (customerList != null && !customerList.isEmpty()) {

						customerDtoList = customerDaoLocal.getAllCustomerDetails(customerList);

						for (CustomerDto c : customerDtoList) {
							customerDtoMap.put(c.getCustId(), c);
							shipToSoldMap.put(c.getCustId(), c.getSpCustId());
						}

						// getting credit limit from RFC
						resultMap = getAllcustLimits(customerList, getDraftDto.getSalesRep(), shipToSoldMap);

					}

					if (resultMap != null && !resultMap.isEmpty() && customerDtoList != null
							&& !customerDtoList.isEmpty()) {

						for (SalesOrderDO salesOrderDO : salesOrderDOList) {

							if (salesOrderDO.getCustomerId() != null
									&& !salesOrderDO.getCustomerId().trim().isEmpty()) {

								customerDto = customerDtoMap.get(salesOrderDO.getCustomerId());

								if (customerDto != null) {

									CreditLimitDto creditLimitDto = resultMap
											.get(shipToSoldMap.get(salesOrderDO.getCustomerId()));

									BigDecimal creditLimit;

									BigDecimal exposure;

									if (creditLimitDto != null) {

										creditLimit = creditLimitDto.getCreditLimit();
										exposure = creditLimitDto.getExposure();

									} else {

										creditLimit = null;
										exposure = null;
									}

									customerDto.setCustCreditLimit(exposure != null ? exposure.toString() : null);
									customerDto.setExposure(creditLimit);

								}
								salesOrderDTO = exportDto(salesOrderDO);
								
								//CR Change Start
								List<SalesOrderLineItemDTO> filnalList=new ArrayList<SalesOrderLineItemDTO>();
								for(int i=salesOrderDTO.getListOfItems().size()-1;i>=0;i--) {
									filnalList.add(salesOrderDTO.getListOfItems().get(i));	
								}
								salesOrderDTO.setListOfItems(filnalList);
								//CR Change done

								customerDto.setTerms(salesOrderDTO.getTerms());

								salesOrderDTO.setCustomerDto(customerDto);

								salesOrderDTOList.add(salesOrderDTO);

							}
						}

					}
				}
			}
		} 
		catch (Exception e) {
			logger.error("[SalesOrderDao][getDraftSalesOrders]" + e.getMessage());
		}
		return salesOrderDTOList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesOrderLineItemDTO> getAllMatIdList(String salesRep, String customerId) {

		List<SalesOrderLineItemDO> salesOrderLineItemDOList = new ArrayList<>();

		List<SalesOrderLineItemDTO> salesOrderLineItemDTOList = new ArrayList<>();

		SalesOrderLineItemDTO salesOrderLineItemDTO;

		if (salesRep != null && customerId != null) {

			String query = " select sold from SalesOrderLineItemDO sold where "
					+ " sold.salesOrder.salesRep=:salesRep and sold.salesOrder.customerId=:customerId ";

			Query q = getSession().createQuery(query);
			q.setParameter("salesRep", salesRep);
			q.setParameter("customerId", customerId);

			salesOrderLineItemDOList = q.list();

			Map<String, Integer> uniqueTest = new HashMap<>();

			for (SalesOrderLineItemDO model : salesOrderLineItemDOList) {

				if (uniqueTest.containsKey(model.getMatId())) {

					uniqueTest.put(model.getMatId(), uniqueTest.get(model.getMatId()) + 1);

				} else {

					uniqueTest.put(model.getMatId(), 1);

				}

				if (uniqueTest.get(model.getMatId()) == 1) {

					salesOrderLineItemDTO = new SalesOrderLineItemDTO();

					salesOrderLineItemDTO.setMatId(model.getMatId());
					salesOrderLineItemDTO.setMatDesc(model.getMatDesc());
					salesOrderLineItemDTO.setBrand(model.getBrand());
					salesOrderLineItemDTO.setCategory(model.getCategory());

					salesOrderLineItemDTOList.add(salesOrderLineItemDTO);
				}
			}
		}

		return salesOrderLineItemDTOList;
	}

	@Override
	public OrderDetailsDto getCopySalesOrder(SalesOrderDTO dto) {

		if (dto.getTempSalesOrderNo() != null && !dto.getTempSalesOrderNo().equals("")) {

			try {

				String query = "select sro from SalesOrderDO sro where  sro.tempSalesOrderNo=:tempSalesOrderNo";

				Query q = getSession().createQuery(query);

				q.setParameter("tempSalesOrderNo", dto.getTempSalesOrderNo());

				SalesOrderDO salesOrderDO = (SalesOrderDO) q.uniqueResult();

				List<SalesOrderDO> entityList = new ArrayList<>();

				entityList.add(salesOrderDO);

				getMaterialDtoList(entityList, salesOrderDO.getCustomerId());

				orderDetailsDto = new OrderDetailsDto();

				orderDetailsDto.setTempSalesOrderNo(salesOrderDO.getTempSalesOrderNo());
				orderDetailsDto.setOrderPlacementDate(salesOrderDO.getOrderPlacementDate());
				orderDetailsDto.setTotalPrice(salesOrderDO.getTotalPrice());
				orderDetailsDto.setCurrency(salesOrderDO.getCurrency());

				List<CategoriesDto> categoriesList = new ArrayList<>();

				List<StockDetailsDo> stockDetailsDoList = new ArrayList<>();

				List<SalesOrderLineItemDO> salesOrderLineItemList = salesOrderDO.getListOfItems();

				if (!ServicesUtil.isEmpty(salesOrderLineItemList)) {

					stockDetailsDoList = stockDetailsDaoLocal.getStockForSalesByDate(salesOrderDO.getSalesRep(),
							salesOrderDO.getCustomerId(), salesOrderDO.getOrderPlacementDate());

					Map<String, Integer> map = new HashMap<>();

					for (SalesOrderLineItemDO salesOrderLineItemDO : salesOrderLineItemList) {

						if (map.containsKey(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
						+ salesOrderLineItemDO.getIsFree())) {
							int count = map.get(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
							+ salesOrderLineItemDO.getIsFree());
							map.put(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
							+ salesOrderLineItemDO.getIsFree(), count + 1);
						} else {
							map.put(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
							+ salesOrderLineItemDO.getIsFree(), 1);
						}

						if (map.get(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
						+ salesOrderLineItemDO.getIsFree()) == 1) {

							CategoriesDto categoriesDto = new CategoriesDto();

							MaterialDto materialDto = getMaterialDtoForSales(salesOrderLineItemDO.getMatId(),
									salesOrderLineItemDO.getUom());

							categoriesDto.setBrand(materialDto.getBrand());
							categoriesDto.setCategory(materialDto.getCategory());
							categoriesDto.setCategoryName(materialDto.getCategoryName());
							categoriesDto.setCurrency(materialDto.getCurrency());
							categoriesDto.setItemCategory(materialDto.getItemCategory());
							categoriesDto.setDiscount1(materialDto.getDiscount1());
							categoriesDto.setDiscount2(materialDto.getDiscount2());
							categoriesDto.setDiscount3(materialDto.getDiscount3());
							categoriesDto.setMatDesc(materialDto.getMatDesc());
							categoriesDto.setMatId(materialDto.getMatId());
							categoriesDto.setTax(materialDto.getTax());
							categoriesDto.setUnitPrice(materialDto.getUnitPrice());
							categoriesDto.setUom(salesOrderLineItemDO.getUom());
							categoriesDto.setUnits(salesOrderLineItemDO.getUnits());
							categoriesDto.setDiscount1Type(materialDto.getDiscount1Type());
							categoriesDto.setDiscount2Type(materialDto.getDiscount2Type());
							categoriesDto.setDiscount3Type(materialDto.getDiscount3Type());
							categoriesDto.setAvailableQuantity(materialDto.getAvailableQuantity());
							categoriesDto.setBaseValue(materialDto.getBaseValue());
							categoriesDto.setNetPrice(materialDto.getNetPrice());
							categoriesDto.setIsFree(salesOrderLineItemDO.getIsFree());

							if (!salesOrderLineItemDO.getIsFree()) {
								categoriesDto.setListOfPreviousTransaction(
										getPrevoiusTransactionForGetStock(stockDetailsDoList,
												salesOrderLineItemDO.getMatId(), salesOrderLineItemDO.getUom()));
							}
							categoriesList.add(categoriesDto);
						}

					}
				}

				orderDetailsDto.setListOfCategories(categoriesList);

			} catch (Exception e) {

				logger.error("[SalesOrderDao][getCopySalesOrder]:::" + e.getMessage());
			}
		}
		return orderDetailsDto;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Date> getRecentSales(String salesRep, String customerId, String shippingToAddress) {

		if (salesRep != null && customerId != null) {

			try {

				String query;

				if (shippingToAddress != null && !shippingToAddress.equals("")) {

					query = "select sro from SalesOrderDO sro where  sro.customerId=:customerId and sro.salesRep=:salesRep "
							+ " and sro.isDraft=:isDraft  and sro.shippingToAddress=' " + shippingToAddress
							+ "'  order by orderPlacementDate desc";
				} else {

					query = "select sro from SalesOrderDO sro where sro.customerId=:customerId and sro.salesRep=:salesRep "
							+ "  and sro.isDraft=:isDraft  order by orderPlacementDate desc";

				}

				logger.error("[SalesOrderDao][getRecentSales] :" + query);

				Query q = getSession().createQuery(query);
				q.setParameter("customerId", customerId);
				q.setParameter("salesRep", salesRep);
				q.setParameter("isDraft", Boolean.FALSE);
				q.setFirstResult(0);
				q.setMaxResults(3);
				List<SalesOrderDO> salesOrderDOList = q.list();

				recentSalesMap = new HashMap<>();

				salesOrderDOList.forEach(salesOrderDo -> recentSalesMap.put(salesOrderDo.getTempSalesOrderNo(),
						salesOrderDo.getOrderPlacementDate()));

			} catch (Exception e) {
				logger.error("[SalesOrderDao][getRecentSales]" + e.getMessage());
			}
		}

		return recentSalesMap;
	}

	@Override
	public void deleteDraftSalesOrder(Long salesOrderId) {

		try {

			SalesOrderDO salesOrderDO = (SalesOrderDO) getSession().get(SalesOrderDO.class, salesOrderId);

			getSession().delete(salesOrderDO);

		} catch (Exception e) {

			logger.error("[SalesOrderDao][deleteDraftSalesOrder]" + e.getMessage());

		}

	}

	// update draft
	private void updateDraft(SalesOrderDTO dto) {

		SalesOrderDO salesOrderDoUi = importDto(dto);

		SalesOrderDO salesOrderDO = (SalesOrderDO) getSession().get(SalesOrderDO.class, dto.getSalesOrderID());

		salesOrderDO.getListOfItems().clear();
		salesOrderDO.getListOfItems().addAll(salesOrderDoUi.getListOfItems());
		salesOrderDO.setTotalPrice(salesOrderDoUi.getTotalPrice());
		salesOrderDO.setShippingToAddress(salesOrderDoUi.getShippingToAddress());
		salesOrderDO.setCreatedBy(salesOrderDoUi.getCreatedBy());
		salesOrderDO.setCreatedDate(salesOrderDoUi.getCreatedDate());
		salesOrderDO.setCustomerId(salesOrderDoUi.getCustomerId());
		salesOrderDO.setDraftId(salesOrderDoUi.getDraftId());
		salesOrderDO.setIsDraft(salesOrderDoUi.getIsDraft());
		salesOrderDO.setOrderPlacementDate(salesOrderDoUi.getOrderPlacementDate());
		salesOrderDO.setSalesRep(salesOrderDoUi.getSalesRep());
		salesOrderDO.setSAPSalesOrderNo(salesOrderDoUi.getSAPSalesOrderNo());
		salesOrderDO.setTempSalesOrderNo(salesOrderDoUi.getTempSalesOrderNo());
		salesOrderDO.setSoldToCustId(salesOrderDoUi.getSoldToCustId());
		salesOrderDO.setTerms(salesOrderDoUi.getTerms());
		salesOrderDO.setPoDate(salesOrderDoUi.getPoDate());
		salesOrderDO.setPoNumber(salesOrderDoUi.getPoNumber());
		salesOrderDO.setGuaranteeDate(salesOrderDoUi.getGuaranteeDate());
		salesOrderDO.setShippingText(salesOrderDoUi.getShippingText());
		salesOrderDO.setCreditLimit(salesOrderDoUi.getCreditLimit());
		salesOrderDO.setComments(salesOrderDoUi.getComments());
		// salesOrderDO.setWorkflowInstanceId(salesOrderDoUi.getWorkflowInstanceId());
		// salesOrderDO.setEccPostError(salesOrderDoUi.getEccPostError());
		salesOrderDO.setExceedAmount(salesOrderDoUi.getExceedAmount());
		salesOrderDO.setRequestedDeliveryDate(salesOrderDoUi.getRequestedDeliveryDate());
		salesOrderDO.setShippingToId(salesOrderDoUi.getShippingToId());
		salesOrderDO.setCurrency(salesOrderDoUi.getCurrency());
		// salesOrderDO.setSalesOrderID(salesOrderDoUi.getSalesOrderID());

		getSession().update(salesOrderDO);

	}

	@Override
	public OrderDetailsDto getDraftById(String draftId) {

		if (draftId != null && !draftId.equals("")) {

			try {

				String hql = "select sro from SalesOrderDO sro where sro.draftId=:draftId";

				Query q = getSession().createQuery(hql);
				q.setParameter("draftId", draftId);

				SalesOrderDO entity = (SalesOrderDO) q.uniqueResult();

				List<SalesOrderDO> entityList = new ArrayList<>();

				entityList.add(entity);

				// get Material list from RFC
				getMaterialDtoList(entityList, entity.getCustomerId());

				orderDetailsDto = new OrderDetailsDto();

				if (materialDtoList != null && !materialDtoList.isEmpty()) {

					List<StockDetailsDo> stockDetailsDoList = new ArrayList<>();

					orderDetailsDto.setTempSalesOrderNo(entity.getTempSalesOrderNo());
					orderDetailsDto.setOrderPlacementDate(entity.getOrderPlacementDate());
					orderDetailsDto.setCurrency(entity.getCurrency());
					orderDetailsDto.setTotalPrice(entity.getTotalPrice());

					List<CategoriesDto> categoriesList = new ArrayList<>();

					if (!ServicesUtil.isEmpty(entity.getListOfItems())) {

						stockDetailsDoList = stockDetailsDaoLocal.getStockForSalesByDate(entity.getSalesRep(),
								entity.getCustomerId(), entity.getOrderPlacementDate());

						Map<String, Integer> map = new HashMap<>();

						for (SalesOrderLineItemDO salesOrderLineItemDO : entity.getListOfItems()) {
							 //for CR change Added  salesOrderLineItemDO.getUnits() for map key 
							if (map.containsKey(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
							+ salesOrderLineItemDO.getIsFree()+salesOrderLineItemDO.getUnits())) {
								int count = map.get(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
								+ salesOrderLineItemDO.getIsFree()+salesOrderLineItemDO.getUnits());
								map.put(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
								+ salesOrderLineItemDO.getIsFree()+salesOrderLineItemDO.getUnits(), count + 1);
							} else {
								map.put(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
								+ salesOrderLineItemDO.getIsFree()+salesOrderLineItemDO.getUnits(), 1);
							}

							if (map.get(salesOrderLineItemDO.getMatId() + salesOrderLineItemDO.getUom()
							+ salesOrderLineItemDO.getIsFree()+salesOrderLineItemDO.getUnits()) == 1) {

								CategoriesDto categoriesDto = new CategoriesDto();

								MaterialDto materialDto = getMaterialDtoForSales(salesOrderLineItemDO.getMatId(),
										salesOrderLineItemDO.getUom());

								categoriesDto.setBrand(materialDto.getBrand());
								categoriesDto.setCategory(materialDto.getCategory());
								categoriesDto.setCategoryName(materialDto.getCategoryName());
								categoriesDto.setCurrency(materialDto.getCurrency());
								categoriesDto.setItemCategory(salesOrderLineItemDO.getItemCategory());
								categoriesDto.setDiscount1(materialDto.getDiscount1());
								categoriesDto.setDiscount2(materialDto.getDiscount2());
								categoriesDto.setDiscount3(materialDto.getDiscount3());
								categoriesDto.setMatDesc(materialDto.getMatDesc());
								categoriesDto.setMatId(materialDto.getMatId());
								categoriesDto.setTax(materialDto.getTax());
								categoriesDto.setUnitPrice(materialDto.getUnitPrice());
								categoriesDto.setUom(salesOrderLineItemDO.getUom());
								categoriesDto.setUnits(salesOrderLineItemDO.getUnits());
								categoriesDto.setDiscount1Type(materialDto.getDiscount1Type());
								categoriesDto.setDiscount2Type(materialDto.getDiscount2Type());
								categoriesDto.setDiscount3Type(materialDto.getDiscount3Type());
								categoriesDto.setAvailableQuantity(materialDto.getAvailableQuantity());
								categoriesDto.setBaseValue(materialDto.getBaseValue());
								categoriesDto.setNetPrice(materialDto.getNetPrice());
								categoriesDto.setIsFree(salesOrderLineItemDO.getIsFree());
								categoriesDto.setTotalItemPrice(salesOrderLineItemDO.getTotalItemPrice());

								if (!salesOrderLineItemDO.getIsFree()) {

									categoriesDto.setListOfPreviousTransaction(
											getPrevoiusTransactionForGetStock(stockDetailsDoList,
													salesOrderLineItemDO.getMatId(), salesOrderLineItemDO.getUom()));
								}

								categoriesList.add(categoriesDto);
							}
						}
					}

					orderDetailsDto.setListOfCategories(categoriesList);
				}

			} catch (Exception e) {
				logger.error("[SalesOrderDao][getDraftById]:::" + e.getMessage());
			}

		}
		return orderDetailsDto;
	}

	@Override
	public SalesOrderDTO getSalesOrderByTempId(String tempSalesOrderNo) {

		String hql = "select sro from SalesOrderDO sro where sro.tempSalesOrderNo=:tempSalesOrderNo";
		Query q = getSession().createQuery(hql);
		q.setParameter("tempSalesOrderNo", tempSalesOrderNo);

		SalesOrderDO salesOrderDO = new SalesOrderDO();
		salesOrderDO = (SalesOrderDO) q.uniqueResult();

		return exportDto(salesOrderDO);

	}

	@Override
	public int updateWorkflowInstanceId(String tempSalesOrderNo, String workflowInstanceId) {

		String hql = " update SalesOrderDO s set s.workflowInstanceId=:workflowInstanceId where s.tempSalesOrderNo=:tempSalesOrderNo ";
		Query q = getSession().createQuery(hql);
		q.setParameter("tempSalesOrderNo", tempSalesOrderNo);
		q.setParameter("workflowInstanceId", workflowInstanceId);

		return q.executeUpdate();
	}

	@Override
	public void updateSapSalesOrderNo(String tempSalesOrderNo, String sapSalesOrderNo) {

		String hql = "update SalesOrderDO s set s.sAPSalesOrderNo=:sAPSalesOrderNo where s.tempSalesOrderNo=:tempSalesOrderNo";
		Query q = getSession().createQuery(hql);
		q.setParameter("tempSalesOrderNo", tempSalesOrderNo);
		q.setParameter("sAPSalesOrderNo", sapSalesOrderNo);
		q.executeUpdate();

	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PendingApprovalDto> getPendingApproval(String customerId) {

		List<PendingApprovalDto> resultList = new ArrayList<>();
		CustomerDto customerDto;
		PendingApprovalDto pendingApprovalDto;

		String query = "from SalesOrderDO so where so.customerId=:customerId and isDraft=:isDraft order by so.orderPlacementDate desc";
		Query q = getSession().createQuery(query);
		q.setParameter("customerId", customerId);
		q.setParameter("isDraft", Boolean.FALSE);

		List<SalesOrderDO> salesOrderDOList = q.list();
		

		customerDto = new CustomerDto();
		customerDto.setCustId(customerId);
		customerDto = (CustomerDto) customerServicelocal.getCustomer(customerDto).getData();

		for (SalesOrderDO salesOrderDO : salesOrderDOList) {

			OrderStatusDO orderStatusDO = new OrderStatusDO();
			System.err.println(salesOrderDO.getStatusList().size());
			orderStatusDO = salesOrderDO.getStatusList().get(0);
			//test start
			/*if(salesOrderDO.getStatusList().size()==1)
			orderStatusDO = salesOrderDO.getStatusList().get(0);
			
			else{
			for(int i=0;i<salesOrderDO.getStatusList().size();i++){
				if(salesOrderDO.getStatusList().get(i).getStatus() != null){
					orderStatusDO = salesOrderDO.getStatusList().get(i);
				}
			}
			}*/
			//test end
			
			pendingApprovalDto = new PendingApprovalDto();

			pendingApprovalDto.setStatus(orderStatusDO.getStatus());

			pendingApprovalDto.setCustomerId(orderStatusDO.getSalesOrder().getCustomerId());
			pendingApprovalDto.setCustomerName(customerDto.getCustName());
			pendingApprovalDto.setCustomerAddress(customerDto.getCustCity() + " " + customerDto.getCustCountry() + " "
					+ customerDto.getCustPostalCode());

			pendingApprovalDto.setSoldToPartyId(customerDto.getSpCustId());
			pendingApprovalDto.setSoldToPartyName(customerDto.getSpName());

			pendingApprovalDto.setRefPoNo(salesOrderDO.getTempSalesOrderNo());
			pendingApprovalDto.setOrderAmount(orderStatusDO.getSalesOrder().getTotalPrice());

			try {
				pendingApprovalDto.setExceededLimit(salesOrderDO.getExceedAmount());
			} catch (Exception e) {
				logger.error("[SalesOrderDao][getPendingApproval]:::" + e.getMessage());
			}

			pendingApprovalDto.setCurrency(orderStatusDO.getSalesOrder().getCurrency());
			pendingApprovalDto.setComments(orderStatusDO.getSalesOrder().getComments());

			pendingApprovalDto.setOrderPlacementDate(salesOrderDO.getOrderPlacementDate());
			pendingApprovalDto.setComments(salesOrderDO.getComments());
			pendingApprovalDto.setSapSalesOrderNumber(salesOrderDO.getSAPSalesOrderNo());
			pendingApprovalDto.setOrderStatusRfc(salesOrderDO.getOrderStatusRfc());
			pendingApprovalDto.setExposure(salesOrderDO.getExposure());

			resultList.add(pendingApprovalDto);

		}

		return resultList;
	}

	@Override
	public SalesOrderDTO getsalesOrderById(String tempSalesOrderNo) {
		SalesOrderDTO salesOrderDto = new SalesOrderDTO();
		String query = "from SalesOrderDO so where so.tempSalesOrderNo=:tempSalesOrderNo";
		Query q = getSession().createQuery(query);

		q.setParameter("tempSalesOrderNo", tempSalesOrderNo);
		salesOrderDto = exportDto((SalesOrderDO) q.uniqueResult());
	
		return salesOrderDto;

	}

	private List<MaterialDto> getMaterialDtoList(List<SalesOrderDO> salesOrderDOList, String customerId) {

		materialDtoList = new ArrayList<>();

		materialDtoList = service.getMaterialDtoForSales(salesOrderDOList, customerId);

		return materialDtoList;

	}

	private MaterialDto getMaterialDtoForSales(String matId, String uom) {

		MaterialDto materialDto = new MaterialDto();

		if (matId != null && uom != null) {

			for (MaterialDto matDto : materialDtoList) {

				if (matId.equalsIgnoreCase(matDto.getMatId()) && uom.equalsIgnoreCase(matDto.getSalesUnit())) {

					materialDto = matDto;

					break;

				}

			}

		}

		return materialDto;

	}

	private List<PreviousTransactions> getPrevoiusTransactionForGetStock(List<StockDetailsDo> stockDetailsDoList,
			String matId, String uom) {

		List<PreviousTransactions> previousTransactionsList = new ArrayList<>();
		PreviousTransactions previousTransaction;

		try {

			if (stockDetailsDoList != null) {

				for (StockDetailsDo stockDetailsDo : stockDetailsDoList) {

					for (StockLineItemsDo stockLineItemsDo : stockDetailsDo.getListOfStockLineItems()) {

						if (stockLineItemsDo.getMatId().equalsIgnoreCase(matId)
								&& stockLineItemsDo.getUom().equalsIgnoreCase(uom)) {

							previousTransaction = new PreviousTransactions();

							previousTransaction.setStockIn(stockLineItemsDo.getStockIn());
							previousTransaction.setStockOut(stockLineItemsDo.getStockOut());
							previousTransaction.setUnitPrice(stockLineItemsDo.getUnitPrice());
							previousTransaction.setUom(stockLineItemsDo.getUom());
							previousTransaction.setTransactionDate(stockDetailsDo.getStockDate());
							previousTransaction.setTransactionId(stockDetailsDo.getStockPrimaryId());

							previousTransactionsList.add(previousTransaction);

							break;

						}

					}

				}

			}

		} catch (Exception e) {

			logger.error("[SalesOrderDao][getPrevoiusTransactionForGetStock]:::" + e.getMessage());

		}

		return previousTransactionsList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int deleteDraftsWhichExceeds3days() {

		List<SalesOrderDO> salesOrderDOList = new ArrayList<>();

		LocalDateTime resultTime = LocalDateTime.now().minusDays(3);
		ZonedDateTime zdt = resultTime.atZone(ZoneId.systemDefault());
		Date outputTime = Date.from(zdt.toInstant());

		String hql = " select sod from SalesOrderDO sod  where sod.isDraft=:isDraft and sod.orderPlacementDate < :outputTime ";

		Query q = getSession().createQuery(hql);
		q.setParameter("isDraft", Boolean.TRUE);
		q.setParameter("outputTime", ServicesUtil.convertDate(outputTime));

		salesOrderDOList = q.list();

		for (SalesOrderDO order : salesOrderDOList) {

			getSession().delete(order);

		}

		return salesOrderDOList.size();
	}

	private Map<String, CreditLimitDto> getAllcustLimits(List<String> custList, String salesRep,
			Map<String, String> shipToSoldMap) {

		Map<String, CreditLimitDto> resultMap = new HashMap<>();

		List<SalesRepCustDto> salesRepCustDtoList = new ArrayList<>();

		Map<String, String> MapForRfc = new HashMap<>();

		String pernrId;

		pernrId = userDao.getUserById(salesRep).getPernrId();

		salesRepCustDtoList = salesManCustRelDaoLocal.getControllingAreasByCustList(custList, pernrId);

		if (salesRepCustDtoList != null && !salesRepCustDtoList.isEmpty()) {

			for (SalesRepCustDto s : salesRepCustDtoList) {

				MapForRfc.put(shipToSoldMap.get(s.getCust_code()), s.getControlling_area());
			}

			if (MapForRfc != null && !MapForRfc.isEmpty()) {

				resultMap = hciGetCustomerService.getCreditLimitsOfCutomerList(MapForRfc);
			}
		}

		return resultMap;
	}

	


	private List<SalesOrderDTO> splitDraftIntoMultples(SalesOrderDTO dto) {

		List<SalesOrderDTO> salesOrderDTOList = new ArrayList<>();

		List<SalesOrderLineItemDTO> itemList = dto.getListOfItems();

		int listSize = itemList.size();

		int multiples = listSize / 30;

		int finalListsize = listSize - 30 * multiples;

		int itemInc = 0;

		int splitInc = 1;

		List<SalesOrderLineItemDTO> itemNewList = new ArrayList<>();

		for (SalesOrderLineItemDTO item : itemList) {

			itemInc++;

			itemNewList.add(item);

			if (itemInc == 30 * splitInc) {

				salesOrderDTOList.add(cloneObjectSplitDrafts(dto, itemNewList));

				itemNewList = new ArrayList<>();

				splitInc++;

			} else if (splitInc > multiples && itemNewList.size() == finalListsize) {

				salesOrderDTOList.add(cloneObjectSplitDrafts(dto, itemNewList));

			}

		}

		return salesOrderDTOList;
	}

	private SalesOrderDTO cloneObjectSplitDrafts(SalesOrderDTO dto, List<SalesOrderLineItemDTO> itemNewList) {

		SalesOrderDTO salesOrderDTO = new SalesOrderDTO();

		salesOrderDTO.setSoldToCustId(dto.getSoldToCustId());
		salesOrderDTO.setTerms(dto.getTerms());
		salesOrderDTO.setCreditLimit(dto.getCreditLimit());
		salesOrderDTO.setExceedAmount(dto.getExceedAmount());
		salesOrderDTO.setShippingToId(dto.getShippingToId());
		salesOrderDTO.setEccPostError(dto.getEccPostError());
		salesOrderDTO.setRequestedDeliveryDate(dto.getRequestedDeliveryDate());
		salesOrderDTO.setComments(dto.getComments());
		salesOrderDTO.setCurrency(dto.getCurrency());
		salesOrderDTO.setWorkflowInstanceId(dto.getWorkflowInstanceId());
		salesOrderDTO.setSalesOrderID(dto.getSalesOrderID());
		salesOrderDTO.setShippingToAddress(dto.getShippingToAddress());
		salesOrderDTO.setCustomerId(dto.getCustomerId());
		salesOrderDTO.setSalesRep(dto.getSalesRep());
		salesOrderDTO.setSAPSalesOrderNo(dto.getSAPSalesOrderNo());
		salesOrderDTO.setTempSalesOrderNo(dto.getTempSalesOrderNo());
		salesOrderDTO.setTotalPrice(dto.getTotalPrice());
		salesOrderDTO.setPoDate(dto.getPoDate());
		salesOrderDTO.setPoNumber(dto.getPoNumber());
		salesOrderDTO.setShippingText(dto.getShippingText());
		salesOrderDTO.setGuaranteeDate(dto.getGuaranteeDate());
		salesOrderDTO.setOrderPlacementDate(dto.getOrderPlacementDate());
		salesOrderDTO.setCreatedBy(dto.getCreatedBy());
		salesOrderDTO.setCreatedDate(dto.getOrderPlacementDate());
		salesOrderDTO.setIsDraft(dto.getIsDraft());
		salesOrderDTO.setDraftId(dto.getDraftId());

		salesOrderDTO.setListOfItems(itemNewList);

		return salesOrderDTO;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getSapOrderIds() {

		List<String> orderIdList = new ArrayList<>();

		List<String> conditionList = new ArrayList<>();

		conditionList.add(DelfiReturnConstants.CANCELLED);
		conditionList.add(DelfiReturnConstants.SHIPPED);
		conditionList.add(DelfiReturnConstants.PARTIALLY_SHIPPED);

		String hql = " select  sAPSalesOrderNo from SalesOrderDO where not orderStatusRfc in (:conditionList) and  SAP_SALES_ORDER_NO  IS NOT NULL ";

		Query q = getSession().createQuery(hql);

		q.setParameterList("conditionList", conditionList);

		orderIdList = q.list();

		return orderIdList;
	}

	@Override
	public void updateOrderRfcStatus(List<OrderRfcStatusDto> orderRfcStatusList) {

		int count = 0;

		for (OrderRfcStatusDto orderRfcStatusDto : orderRfcStatusList) {

			count++;

			String hql = "update SalesOrderDO set orderStatusRfc=:orderStatusRfc  where sAPSalesOrderNo=:sAPSalesOrderNo";

			Query q = getSession().createQuery(hql);

			q.setParameter("orderStatusRfc", orderRfcStatusDto.getStatus());
			q.setParameter("sAPSalesOrderNo", orderRfcStatusDto.getOrderId());
			// q.setParameter("statusRfcDate", orderRfcStatusDto.getZdate());

			q.executeUpdate();

			if (count > 0 && count % 50 == 0) {

				getSession().flush();
				getSession().clear();

			}

		}

	}

	@Override
	public String getComment(String tempSalesOrder) {

		String hql = "select comments  from SalesOrderDO where tempSalesOrderNo=:tempSalesOrderNo ";

		Query q = getSession().createQuery(hql);

		q.setParameter("tempSalesOrderNo", tempSalesOrder);

		return (String) q.uniqueResult();

	}

	@Override
	public void updateComment(String tempSalesOrder, String comment) {

		String hql = " update SalesOrderDO set comments=:comments where tempSalesOrderNo=:tempSalesOrderNo ";

		Query q = getSession().createQuery(hql);

		q.setParameter("tempSalesOrderNo", tempSalesOrder);
		q.setParameter("comments", comment);

		q.executeUpdate();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesOrderDTO> getOrdersByOrderId(List<Long> orderIdList) {

		List<SalesOrderDTO> salesOrderDTOList = new ArrayList<>();

		String hql = "from SalesOrderDO where salesOrderID in (:salesOrderIDList) ";

		Query q = getSession().createQuery(hql);

		q.setParameterList("salesOrderIDList", orderIdList);

		List<SalesOrderDO> salesOrderDOList = q.list();

		for (SalesOrderDO entity : salesOrderDOList) {

			salesOrderDTOList.add(exportDto(entity));

		}

		return salesOrderDTOList;

	}

	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Override public List<Long> getFailedOrdersList() {
	 * 
	 * List<Long> orderIdList = new ArrayList<>();
	 * 
	 * String hql =
	 * "  select salesOrderID from SalesOrderDO so where so.sAPSalesOrderNo is null and so.isDraft=:isDraft "
	 * ;
	 * 
	 * Query q = getSession().createQuery(hql);
	 * 
	 * q.setParameter("isDraft", Boolean.FALSE);
	 * 
	 * orderIdList = q.list();
	 * 
	 * return orderIdList;
	 * 
	 * }
	 */

	@Override
	public void updateSapOrderNumber(Map<String, String> tempSap) {

		int count = 0;

		if (tempSap != null && !tempSap.isEmpty()) {

			Set<Entry<String, String>> entrySet = tempSap.entrySet();

			for (Entry<String, String> entry : entrySet) {

				count++;

				String hql = "update SalesOrderDO set  sAPSalesOrderNo=:sAPSalesOrderNo where tempSalesOrderNo=:tempSalesOrderNo ";

				Query q = getSession().createQuery(hql);

				q.setParameter("sAPSalesOrderNo", entry.getValue());
				q.setParameter("tempSalesOrderNo", entry.getKey());

				q.executeUpdate();

				if (count > 0 && count % 50 == 0) {

					getSession().flush();
					getSession().clear();

				}

			}
		}

	}

	@Override
	public SalesOrderDO getSalesOrderByTempSalesNo(String tempSalesOrderNo) {

		String hql = "select sro from SalesOrderDO sro where sro.tempSalesOrderNo=:tempSalesOrderNo";
		Query q = getSession().createQuery(hql);
		q.setParameter("tempSalesOrderNo", tempSalesOrderNo);

		SalesOrderDO salesOrderDO = (SalesOrderDO) q.uniqueResult();

		return salesOrderDO;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getFailedOrders() {

		List<String> failedOrders = new ArrayList<>();

		String hql = "select sro.tempSalesOrderNo from SalesOrderDO sro where sro.sAPSalesOrderNo=:sAPSalesOrderNo";
		Query q = getSession().createQuery(hql);
		q.setParameter("sAPSalesOrderNo", "order failure");

		failedOrders = q.list();

		return failedOrders;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesOrderDTO> getOrdersBytempSaleOrderIDList(List<String> orderList) {

		List<SalesOrderDTO> salesOrderDTOList = new ArrayList<>();

		String hql = "select sro from SalesOrderDO sro where sro.tempSalesOrderNo in (:orderList)";
		Query q = getSession().createQuery(hql);
		q.setParameterList("orderList", orderList);

		List<SalesOrderDO> salesOrderDOList = q.list();

		if (salesOrderDOList != null && !salesOrderDOList.isEmpty()) {

			for (SalesOrderDO entity : salesOrderDOList) {

				salesOrderDTOList.add(exportDto(entity));
			}

		}

		return salesOrderDTOList;

	}

	//CR Change
	@SuppressWarnings("unchecked")
	@Override
	public List<CommentListDto> getCommentsList(String tempSalesOrderNo) {

		String hql = "from CommentListDo where tempSalesOrderNo=:tempSalesOrderNo order by commentId desc";
		Query q = getSession().createQuery(hql);
		q.setParameter("tempSalesOrderNo", tempSalesOrderNo);
		List<CommentListDto> list=q.list();
		System.err.println(list);
		/*for (CommentListDto commentListDto : list) {
			commentListDto.setComment(commentListDto.getCommentedBy()+": "+commentListDto.getComment());
		}*/
		return list;
	}
	//CR Change
	@SuppressWarnings("unchecked")
	@Override
	public List<SalesOrderDTO> getSalesOrderByTempIdTest(List<String> tempSalesOrderNoList) {
		List<SalesOrderDO> salesOrderDO = new ArrayList<SalesOrderDO>();
		List<SalesOrderDTO> salesOrderDto = new ArrayList<SalesOrderDTO>();
		String hql = "select sro from SalesOrderDO sro where sro.tempSalesOrderNo IN (:tempSalesOrderNo)";
		Query q = getSession().createQuery(hql);
		//q.setParameter("tempSalesOrderNo", tempSalesOrderNo);
		q.setParameterList("tempSalesOrderNo", tempSalesOrderNoList);
		salesOrderDO = q.list();			

		for (SalesOrderDO salesOrderDOObj : salesOrderDO) {
			salesOrderDto.add(exportDto(salesOrderDOObj));
		}

		return salesOrderDto;
		/*	return exportDto(salesOrderDO);
          List<Object> obj=q.list();

        		  //q.setCacheable(true).list();
		 */	}



}
