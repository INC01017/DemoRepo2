package com.incture.delfi.dao;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.incture.delfi.dto.PendingInvoiceDto;
import com.incture.delfi.dto.PendingInvoiceItemDto;
import com.incture.delfi.entity.PendingInvoiceDo;
import com.incture.delfi.entity.PendingInvoiceItemDo;
import com.incture.delfi.util.ServicesUtil;

@Repository("PendingInvoiceDao")
public class PendingInvoiceDao extends BaseDao<PendingInvoiceDo, PendingInvoiceDto> implements PendingInvoiceDaoLocal {

	private static final Logger logger = LoggerFactory.getLogger(PendingInvoiceDao.class);

	@Override
	public PendingInvoiceDo importDto(PendingInvoiceDto dto) {

		PendingInvoiceDo entity = new PendingInvoiceDo();

		if (dto != null) {

			entity.setAccountingDocument(dto.getAccountingDocument());
			entity.setAmount(dto.getAmount());
			entity.setAmountInLocalCurrency(dto.getAmountInLocalCurrency());
			entity.setBillingDocumentNo(dto.getBillingDocumentNo());
			entity.setCurrency(dto.getCurrency());
			entity.setDocumentDate(dto.getDocumentDate());
			entity.setDocumentType(dto.getDocumentType());
			entity.setPendingAmount(dto.getPendingAmount());
			entity.setPostingDate(dto.getPostingDate());
			entity.setCustId(dto.getCustId());
			entity.setInvoiceNumber(dto.getInvoiceNumber());
			entity.setNetValue(dto.getNetValue());
			entity.setPayer(dto.getPayer());
			entity.setSoldToParty(dto.getSoldToParty());
			entity.setTaxAmount(dto.getTaxAmount());
			List<PendingInvoiceItemDo> pendingInvItemList = null;

			if (dto.getInvoiceItemList() != null && !dto.getInvoiceItemList().isEmpty()) {
				pendingInvItemList = setItemDetail(dto.getInvoiceItemList(), entity);
			}

			entity.setInvoiceItemList(pendingInvItemList);

		}

		return entity;
	}

	@Override
	public PendingInvoiceDto exportDto(PendingInvoiceDo entity) {

		PendingInvoiceDto pendingInvoiceDto = new PendingInvoiceDto();

		if (entity != null) {

			pendingInvoiceDto.setAccountingDocument(entity.getAccountingDocument());
			pendingInvoiceDto.setAmount(entity.getAmount());
			pendingInvoiceDto.setAmountInLocalCurrency(entity.getAmountInLocalCurrency());
			pendingInvoiceDto.setBillingDocumentNo(entity.getBillingDocumentNo());
			pendingInvoiceDto.setCurrency(entity.getCurrency());
			pendingInvoiceDto.setDocumentDate(entity.getDocumentDate());
			pendingInvoiceDto.setDocumentType(entity.getDocumentType());
			pendingInvoiceDto.setPendingAmount(entity.getPendingAmount());
			pendingInvoiceDto.setPostingDate(entity.getPostingDate());
			pendingInvoiceDto.setCustId(entity.getCustId());
			pendingInvoiceDto.setInvoiceNumber(entity.getInvoiceNumber());
			pendingInvoiceDto.setNetValue(entity.getNetValue());
			pendingInvoiceDto.setPayer(entity.getPayer());
			pendingInvoiceDto.setSoldToParty(entity.getSoldToParty());
			pendingInvoiceDto.setTaxAmount(entity.getTaxAmount());
			List<PendingInvoiceItemDto> pendingInvItemList = null;

			if (entity.getInvoiceItemList() != null) {

				pendingInvItemList = getItemDetail(entity.getInvoiceItemList(), pendingInvoiceDto);
			}
			pendingInvoiceDto.setInvoiceItemList(pendingInvItemList);

		}

		return pendingInvoiceDto;
	}

	private List<PendingInvoiceItemDo> setItemDetail(List<PendingInvoiceItemDto> itemSet, PendingInvoiceDo entity) {

		List<PendingInvoiceItemDo> setItem = new ArrayList<>();

		for (PendingInvoiceItemDto itemDto : itemSet) {

			setItem.add(importItemDto(itemDto, entity));
		}
		return setItem;
	}

	public PendingInvoiceItemDo importItemDto(PendingInvoiceItemDto itemDto, PendingInvoiceDo entity) {

		PendingInvoiceItemDo pendingInvoiceItemDo = new PendingInvoiceItemDo();

		if (itemDto != null) {
			pendingInvoiceItemDo.setBilledQty(itemDto.getBilledQty());
			pendingInvoiceItemDo.setDescription(itemDto.getDescription());
			pendingInvoiceItemDo.setFreegoodInd(itemDto.getFreegoodInd());
			pendingInvoiceItemDo.setHighItem(itemDto.getHighItem());
			pendingInvoiceItemDo.setInvoiceNumber(itemDto.getInvoiceNumber());
			pendingInvoiceItemDo.setItemCateg(itemDto.getItemCateg());
			pendingInvoiceItemDo.setItemNumber(itemDto.getItemNumber());
			pendingInvoiceItemDo.setMatID(itemDto.getMatID());
			pendingInvoiceItemDo.setNetPrice(itemDto.getNetPrice());
			pendingInvoiceItemDo.setPendInvItmPrimaryId(itemDto.getPendInvItmPrimaryId());
			pendingInvoiceItemDo.setSalesDoc(itemDto.getSalesDoc());
			pendingInvoiceItemDo.setSalesUnit(itemDto.getSalesUnit());
			pendingInvoiceItemDo.setTaxAmount(itemDto.getTaxAmount());
			pendingInvoiceItemDo.setPendingInvoice(entity);
		}

		return pendingInvoiceItemDo;
	}

	private List<PendingInvoiceItemDto> getItemDetail(List<PendingInvoiceItemDo> itemList, PendingInvoiceDto dto) {

		List<PendingInvoiceItemDto> itemListDto = new ArrayList<>();

		for (PendingInvoiceItemDo itemDo : itemList) {

			itemListDto.add(exportItemDto(itemDo, dto));
		}
		return itemListDto;
	}

	public PendingInvoiceItemDto exportItemDto(PendingInvoiceItemDo itemDo, PendingInvoiceDto dto) {

		PendingInvoiceItemDto pendingInvoiceItemdto = new PendingInvoiceItemDto();

		if (itemDo != null) {
			pendingInvoiceItemdto.setBilledQty(itemDo.getBilledQty());
			pendingInvoiceItemdto.setDescription(itemDo.getDescription());
			pendingInvoiceItemdto.setFreegoodInd(itemDo.getFreegoodInd());
			pendingInvoiceItemdto.setHighItem(itemDo.getHighItem());
			pendingInvoiceItemdto.setInvoiceNumber(itemDo.getInvoiceNumber());
			pendingInvoiceItemdto.setItemCateg(itemDo.getItemCateg());
			pendingInvoiceItemdto.setItemNumber(itemDo.getItemNumber());
			pendingInvoiceItemdto.setMatID(itemDo.getMatID());
			pendingInvoiceItemdto.setNetPrice(itemDo.getNetPrice());
			// pendingInvoiceItemdto.setPendInvItmPrimaryId(itemDo.getPendInvItmPrimaryId());
			pendingInvoiceItemdto.setSalesDoc(itemDo.getSalesDoc());
			pendingInvoiceItemdto.setSalesUnit(itemDo.getSalesUnit());
			pendingInvoiceItemdto.setTaxAmount(itemDo.getTaxAmount());
		}

		return pendingInvoiceItemdto;
	}

	@Override
	public void savePendingInvoices(List<PendingInvoiceDo> pendingInvList) {

		logger.error("[PendingInvoiceDao][savePendingInvoices]::Start=" + new Date());

		if (pendingInvList != null && !pendingInvList.isEmpty()) {

			int counter = 0;
			int deletedRows = 0;
			int deletedPendinginvRows = 0;
			try {

				String hql = "delete from PendingInvoiceItemDo";

				Query query = getSession().createQuery(hql);

				deletedRows = query.executeUpdate();

				if (deletedRows > 0) {

					String PendingInvhql = "delete from PendingInvoiceDo";

					Query queryInv = getSession().createQuery(PendingInvhql);

					deletedPendinginvRows = queryInv.executeUpdate();
				}

				logger.error("[PendingInvoiceDao][savePendingInvoices]Flushed Invoices:" + deletedRows + " :items: "
						+ deletedPendinginvRows + " invoices");

				for (PendingInvoiceDo dto : pendingInvList) {

					counter++;
					getSession().save(dto);

					if (counter > 0 && counter % 50 == 0) {

						getSession().flush();
						getSession().clear();

					}

				}

			} catch (Exception e) {

				logger.error("[PendingInvoiceDao][savePendingInvoices]" + e.getMessage());
			}

		}
		logger.error("[PendingInvoiceDao][savePendingInvoices]::END=" + new Date());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PendingInvoiceDto> getPendingInvoices(List<String> cutomersList) {

		List<PendingInvoiceDto> pendingInvoiceDtoList = new ArrayList<>();
		List<PendingInvoiceDo> pendingInvoiceDoList;

		String hql = "from PendingInvoiceDo where custId in (:custList) ";

		Query q = getSession().createQuery(hql);
		q.setParameterList("custList", cutomersList);
		pendingInvoiceDoList = q.list();

		for (PendingInvoiceDo p : pendingInvoiceDoList) {

			pendingInvoiceDtoList.add(exportDto(p));

		}

		return pendingInvoiceDtoList;

	}

	@SuppressWarnings("unchecked")
	@Override
	public LocalDate getFirstOfCreditMonth(String custId) {

		LocalDate localDate;

		List<Date> listOfDate;

		String query = "select documentDate from PendingInvoiceDo  where custId=:customerId and not documentType=:documentType order by documentDate asc";
		Query q = getSession().createQuery(query);
		q.setParameter("customerId", custId);
		q.setParameter("documentType", "DG");
		q.setFirstResult(0);
		q.setMaxResults(1);
		listOfDate = q.list();

		if (listOfDate != null && !listOfDate.isEmpty()) {

			Date date = ServicesUtil.convertDate(listOfDate.get(0));

			Instant instant = date.toInstant();

			localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
					.with(TemporalAdjusters.firstDayOfNextMonth());

		} else {

			localDate = LocalDate.now();
		}

		return localDate;

	}

	@Override
	public List<PendingInvoiceDto> getPendingInvoicesNew(List<String> list) {
		
		List<PendingInvoiceDto> pendingInvoiceDtoList = new ArrayList<>();
		List<PendingInvoiceDo> pendingInvoiceDoList;

	String hql = "from PendingInvoiceDo where custId in (:custList) and not documentType=:documentType";

	Query q = getSession().createQuery(hql);
	q.setParameterList("custList", list);
	q.setParameter("documentType", "DG");// Dg - credit note 
	pendingInvoiceDoList = q.list();

	for (PendingInvoiceDo p : pendingInvoiceDoList) {

		pendingInvoiceDtoList.add(exportDto(p));

	}

	return pendingInvoiceDtoList;}

}
