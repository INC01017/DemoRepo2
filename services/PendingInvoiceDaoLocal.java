package com.incture.delfi.dao;

import java.time.LocalDate;
import java.util.List;

import com.incture.delfi.dto.PendingInvoiceDto;
import com.incture.delfi.entity.PendingInvoiceDo;

public interface PendingInvoiceDaoLocal {

	void savePendingInvoices(List<PendingInvoiceDo> pendingInvList);

	List<PendingInvoiceDto> getPendingInvoices(List<String> cutomersList);

	LocalDate getFirstOfCreditMonth(String custId);

	List<PendingInvoiceDto> getPendingInvoicesNew(List<String> list);

}