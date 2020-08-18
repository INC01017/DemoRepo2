package com.incture.delfi.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.incture.delfi.dto.CustomerLocationDto;
import com.incture.delfi.dto.ResponseDto;
import com.incture.delfi.dto.SalesOrgDetailsDto;
import com.incture.delfi.dto.SalesRepCustDto;
import com.incture.delfi.dto.UserDto;
import com.incture.delfi.entity.SalesManCustRelDo;

@Repository("SalesManCustRelDao")
public class SalesManCustRelDao extends BaseDao<SalesManCustRelDo, SalesRepCustDto> implements SalesManCustRelDaoLocal {

	private static final Logger logger = LoggerFactory.getLogger(SalesManCustRelDao.class);

	@Autowired
	private UserDaoLocal userDaoLocal;
	
	@Autowired
	private CustomerLocationDaoLocal customerLocationDaoLocal;

	@Override
	public SalesManCustRelDo importDto(SalesRepCustDto dto) {

		SalesManCustRelDo entity = new SalesManCustRelDo();

		if (dto != null) {

			entity.setPrimaryId(dto.getPrimaryId());
			entity.setApproval1Code(dto.getAppr_1_code());
			entity.setApproval1Name(dto.getAppr_1());
			entity.setApproval2Code(dto.getAppr_2_code());
			entity.setApproval2Name(dto.getAppr_2());
			entity.setApproval3Code(dto.getAppr_3_code());
			entity.setApproval3Name(dto.getAppr_3());
			entity.setAreaCode(dto.getArea_code());
			entity.setAreaName(dto.getArea_name());
			entity.setBranch(dto.getBranch());
			entity.setCategory(dto.getCategory());
			entity.setControllingArea(dto.getControlling_area());
			entity.setCustGroupCode(dto.getCust_grp_code());
			entity.setCustGroupName(dto.getCust_group_name());
			entity.setCustName(dto.getCust_name());
			entity.setDistributionChannel(dto.getDistribution_channel());
			entity.setDivision(dto.getDivision());
			entity.setManagerCode(dto.getManager_code());
			entity.setManagerName(dto.getManager_name());
			entity.setRegionCode(dto.getRegion_code());
			entity.setRegionName(dto.getRegion_name());
			entity.setSalesOrganization(dto.getSales_organization());
			entity.setSalesRepName(dto.getSalesman_name());
			entity.setSalesTeam(dto.getSalesteam());
			entity.setSapCustCode(dto.getCust_code());
			entity.setSapSalesmanCode(dto.getSalesman_code());
			entity.setTerritory(dto.getTerritory());
			entity.setTradeTypeId(dto.getTrade_type_id());
			entity.setTradeTypeName(dto.getTrade_type_name());
			entity.setCustType(dto.getType());
			entity.setLatitude(dto.getLatitude());
			entity.setLongitude(dto.getLongitude());
		}
		return entity;

	}

	@Override
	public SalesRepCustDto exportDto(SalesManCustRelDo entity) {

		SalesRepCustDto dto = new SalesRepCustDto();

		if (entity != null) {

			dto.setPrimaryId(entity.getPrimaryId());
			dto.setAppr_1_code(entity.getApproval1Code());
			dto.setAppr_1((entity.getApproval1Name()));
			dto.setAppr_2_code((entity.getApproval2Code()));
			dto.setAppr_2(entity.getApproval2Name());
			dto.setAppr_3(entity.getApproval3Name());
			dto.setAppr_3_code(entity.getApproval3Code());
			dto.setArea_code(entity.getAreaCode());
			dto.setArea_name(entity.getAreaName());
			dto.setBranch(entity.getBranch());
			dto.setCategory(entity.getCategory());
			dto.setControlling_area(entity.getControllingArea());
			dto.setCust_code(entity.getSapCustCode());
			dto.setCust_group_name(entity.getCustGroupName());
			dto.setCust_grp_code(entity.getCustGroupCode());
			dto.setCust_name(entity.getCustName());
			dto.setDistribution_channel(entity.getDistributionChannel());
			dto.setDivision(entity.getDivision());
			dto.setManager_code(entity.getManagerCode());
			dto.setManager_name(entity.getManagerName());
			dto.setPrimaryId(entity.getPrimaryId());
			dto.setRegion_code(entity.getRegionCode());
			dto.setRegion_name(entity.getRegionName());
			dto.setSales_organization(entity.getSalesOrganization());
			dto.setSalesman_code(entity.getSapSalesmanCode());
			dto.setSalesman_name(entity.getSalesRepName());
			dto.setSalesteam(entity.getSalesTeam());
			dto.setTerritory(entity.getTerritory());
			dto.setTrade_type_id(entity.getTradeTypeId());
			dto.setTrade_type_name(entity.getTradeTypeName());
			dto.setType(entity.getCustType());
			dto.setLatitude(entity.getLatitude());
			dto.setLongitude(entity.getLongitude());

		}
		return dto;
	}

	@Override
	public String getControllingArea(String salesRepId, String custId) {

		String hql = "select sc.controllingArea from SalesManCustRelDo  sc where sc.sapSalesmanCode=:sapSalesmanCode and sc.sapCustCode=:sapCustCode";

		Query query = getSession().createQuery(hql);

		query.setParameter("sapSalesmanCode", salesRepId);
		query.setParameter("sapCustCode", custId);

		Object obj = (Object) query.uniqueResult();

		return obj.toString();

	}

	@SuppressWarnings("unused")
	@Override
	public List<String> saveAllExcelData(List<SalesRepCustDto> salesRepCustDtoList) {
		logger.error("[SalesManCustRelDao][saveAllExcelData]::::::START=" + new Date());
		//CR open
		ResponseDto response=new ResponseDto();
        int numberOfRowsInserted=0;
        List<String> primaryIdCheck=new ArrayList<String>();
        List<String> primaryId;
        CustomerLocationDto customerLocationDto;
        //CR close
        
		int counter = 0;

		List<String> deleteRows = new ArrayList<>();

		try {

			int dtoSize = salesRepCustDtoList.size();

			//for (int i = 1; i < dtoSize; i++) {
			// change for the iFlow failure
			for (int i = 0; i < dtoSize; i++) {
				counter++;
				primaryId=null;
				SalesRepCustDto dto = salesRepCustDtoList.get(i);
				dto.setPrimaryId(formPrimaryId(dto.getSalesman_code(), dto.getCust_code()));
				
				//primaryId.add(dto.getPrimaryId());
				if(primaryIdCheck.contains(dto.getPrimaryKey())==false){
					
				dto.setCust_code(generateCustomerId(dto.getCust_code()));

				if (dto.getManager_code() == null || (dto.getManager_code().trim().isEmpty())) {

					deleteRows.add(dto.getPrimaryId());

				}
				primaryIdCheck.add(dto.getPrimaryId());
				getSession().saveOrUpdate(importDto(dto));
				//to update customer lat long table
				customerLocationDto=new  CustomerLocationDto();
				customerLocationDto.setCustomerId(dto.getCust_code());
				customerLocationDto.setLatitude(dto.getLatitude());
				customerLocationDto.setLongitude(dto.getLongitude());
				customerLocationDaoLocal.saveOrupdateCustomerLocation(customerLocationDto);
				//CR
				numberOfRowsInserted++;

				if (counter % 50 == 0) {
					getSession().flush();
					getSession().clear();
				}
			}

			}
			//CR
			System.err.println(numberOfRowsInserted);
			response.setData(deleteRows);
			response.setCode(200);;

		} catch (Exception e) {
            response.setData(numberOfRowsInserted);
            response.setCode(500);
			logger.error("[SalesManCustRelDao][saveAllExcelData]" + e.getMessage());
		}

		logger.error("[SalesManCustRelDao][saveAllExcelData]::::::END=" + new Date());

		return deleteRows;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesRepCustDto> getCustList(String salesRep) {

		List<SalesRepCustDto> list = new ArrayList<SalesRepCustDto>();
		List<SalesManCustRelDo> listDo = new ArrayList<SalesManCustRelDo>();
		String hql = "select sc from SalesManCustRelDo  sc where sc.sapSalesmanCode=:sapSalesmanCode";

		Query query = getSession().createQuery(hql);

		query.setParameter("sapSalesmanCode", salesRep);

		listDo = query.list();

		for (SalesManCustRelDo s : listDo) {
			list.add(exportDto(s));
		}
		return list;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllPrimaryCustomer(String salesRep) {

		List<String> customerList = new ArrayList<>();

		String hql = "select sapCustCode from SalesManCustRelDo where sapSalesmanCode=:sapSalesmanCode and custType=:custType";

		Query query = getSession().createQuery(hql);

		query.setParameter("sapSalesmanCode", salesRep);

		query.setParameter("custType", "Primary");

		customerList = query.list();

		return customerList;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllCustomerList() {

		List<String> customerList = new ArrayList<>();

		String hql = "select distinct sapCustCode from SalesManCustRelDo where custType=:custType";

		Query q = getSession().createQuery(hql);

		q.setParameter("custType", "Primary");

		customerList = q.list();

		return customerList;

	}

	private String generateCustomerId(String value) {

		String custId = "";
		int length = value.length();

		if (length < 10) {

			int condVal = 10 - length;

			for (int i = 0; i < condVal; i++) {
				custId = custId + "0";
			}
		}

		return (custId + value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SalesRepCustDto getApproversBySalesRep(String salesRep) {

		List<SalesManCustRelDo> salesManCustRel = new ArrayList<>();

		String hql = "from SalesManCustRelDo where sapSalesmanCode=:sapSalesmanCode";

		Query q = getSession().createQuery(hql);

		q.setParameter("sapSalesmanCode", salesRep);
		q.setFirstResult(0);
		q.setMaxResults(1);

		salesManCustRel = q.list();

		return exportDto(salesManCustRel.get(0));

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesRepCustDto> getControllingAreasByCustList(List<String> custList, String salesRep) {

		List<SalesRepCustDto> list = new ArrayList<SalesRepCustDto>();
		List<SalesManCustRelDo> listDo = new ArrayList<SalesManCustRelDo>();

		String hql = " from SalesManCustRelDo where sapSalesmanCode=:sapSalesmanCode and sapCustCode in (:custList) ";

		Query query = getSession().createQuery(hql);

		query.setParameter("sapSalesmanCode", salesRep);
		query.setParameterList("custList", custList);

		listDo = query.list();

		for (SalesManCustRelDo s : listDo) {
			list.add(exportDto(s));
		}
		return list;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesManCustRelDo> getSalesPerson(String managerCode) {
		String hql = "select s.sapSalesmanCode,s.salesRepName,s.custType,s.category,s.regionName,s.areaName,s.territory  from SalesManCustRelDo s where s.managerCode=:managerCode group by s.sapSalesmanCode,s.salesRepName,s.custType,s.category,s.regionName,s.areaName,s.territory";

		Query query = getSession().createQuery(hql);
		query.setParameter("managerCode", managerCode);

		SalesManCustRelDo entity;
		List<SalesManCustRelDo> list = new ArrayList<>();

		for (Object[] ob : (List<Object[]>) query.list()) {
			entity = new SalesManCustRelDo();

			entity.setSapSalesmanCode((String) ob[0]);
			entity.setSalesRepName((String) ob[1]);
			entity.setCustType((String) ob[2]);
			entity.setCategory((String) ob[3]);
			entity.setRegionName((String) ob[4]);
			entity.setAreaName((String) ob[5]);
			entity.setTerritory((String) ob[6]);

			list.add(entity);
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SalesOrgDetailsDto getSalesOrgDetails(String salesRep, String customerId) {

		SalesOrgDetailsDto salesOrgDetailsDto = new SalesOrgDetailsDto();

		UserDto userDto = userDaoLocal.getUserById(salesRep);

		String hql = "select s from SalesManCustRelDo s where s.sapSalesmanCode=:sapSalesmanCode and s.sapCustCode=:sapCustCode";

		Query q = getSession().createQuery(hql);
		q.setParameter("sapSalesmanCode", userDto.getPernrId());
		q.setParameter("sapCustCode", customerId);

		List<SalesManCustRelDo> list = q.list();

		if (!list.isEmpty()) {

			salesOrgDetailsDto.setSalesOrganization((String) list.get(0).getSalesOrganization());
			salesOrgDetailsDto.setDistributionChannel((String) list.get(0).getDistributionChannel());
			salesOrgDetailsDto.setDivision((String) list.get(0).getDivision());

		}

		return salesOrgDetailsDto;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesManCustRelDo> getSalesCustRelDtoByCustList(List<String> custList) {

		List<SalesManCustRelDo> listDo = new ArrayList<>();

		if (custList != null && !custList.isEmpty()) {

			String hql = " from SalesManCustRelDo where  sapCustCode in (:custList) ";

			Query query = getSession().createQuery(hql);

			query.setParameterList("custList", custList);

			listDo = query.list();
		}

		return listDo;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesRepCustDto> getSalesCustRelDtoByCustListAndSalesRep(String salesRep, List<String> custList) {

		UserDto userDto = userDaoLocal.getUserById(salesRep);

		List<SalesManCustRelDo> listDo = new ArrayList<>();

		List<SalesRepCustDto> dtoList = new ArrayList<>();

		if (custList != null && !custList.isEmpty() && userDto != null && userDto.getPernrId() != null) {

			String hql = " from SalesManCustRelDo where sapSalesmanCode=:sapSalesmanCode and  sapCustCode in (:custList) ";

			Query query = getSession().createQuery(hql);

			query.setParameterList("custList", custList);
			query.setParameter("sapSalesmanCode", userDto.getPernrId());

			listDo = query.list();

			for (SalesManCustRelDo salesManCustRelDo : listDo) {

				dtoList.add(exportDto(salesManCustRelDo));
			}
		}

		return dtoList;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SalesRepCustDto> getCorrespondingCustomer(String salesRep) {
		// UserDto userDto;

		List<SalesManCustRelDo> listDo = new ArrayList<>();

		List<SalesRepCustDto> dtoList = new ArrayList<>();

		if (salesRep != null && !salesRep.isEmpty()) {

			// Pass pernrId
			// userDto = userDaoLocal.getUserById(salesRep);

			String hql = " from SalesManCustRelDo where sapSalesmanCode=:sapSalesmanCode";

			Query query = getSession().createQuery(hql);
			query.setParameter("sapSalesmanCode", salesRep);

			listDo = query.list();

			for (SalesManCustRelDo salesManCustRelDo : listDo) {

				dtoList.add(exportDto(salesManCustRelDo));
			}
		}

		return dtoList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getAreaRegionTeritory(String managerCode) {

		String hql = " Select  regionName, areaName, territory from SalesManCustRelDo where approval1Code=:approval1Code "
				+ " or  approval2Code=:approval2Code or approval3Code=:approval3Code";

		Query query = getSession().createQuery(hql);
		query.setParameter("approval1Code", managerCode);
		query.setParameter("approval2Code", managerCode);
		query.setParameter("approval3Code", managerCode);

		List<Object[]> list = query.list();

		return list;
	}

	@Override
	public Long getCountOfRecords() {

		String hql = "select count(primaryId) from SalesManCustRelDo ";

		Query query = getSession().createQuery(hql);

		return (Long) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getManagerOfsalesRep(String salesRep) {

		String manager = "";

		if (salesRep != null && !salesRep.isEmpty()) {

			String hql = " select distinct managerCode from SalesManCustRelDo  where sapSalesmanCode=:sapSalesmanCode ";

			Query query = getSession().createQuery(hql);

			query.setParameter("sapSalesmanCode", salesRep);

			List<String> resultList = query.list();

			manager = resultList.get(0);

		}

		return manager;
	}

	private String formPrimaryId(String salesmanCode, String customerId) {

		String primaryId = (salesmanCode != null ? salesmanCode.trim() : salesmanCode)
				+ (customerId != null ? customerId.trim() : customerId);

		return primaryId;
	}

	@Override
	public void deleteExcelrows(List<String> primaryIdList) {
		String hql = "delete from SalesManCustRelDo where primaryId in (:primaryIdList)";
		Query query = getSession().createQuery(hql);
		query.setParameterList("primaryIdList", primaryIdList);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public SalesOrgDetailsDto getSalesOrgDetail(String customerNumber) {

		SalesOrgDetailsDto salesOrgDetailsDto = new SalesOrgDetailsDto();

		String hql = "select s from SalesManCustRelDo s where s.sapCustCode=:sapCustCode";

		Query q = getSession().createQuery(hql);
		q.setParameter("sapCustCode", customerNumber);

		List<SalesManCustRelDo> list = q.list();

		if (!list.isEmpty()) {

			salesOrgDetailsDto.setSalesOrganization((String) list.get(0).getSalesOrganization());
			salesOrgDetailsDto.setDistributionChannel((String) list.get(0).getDistributionChannel());
			salesOrgDetailsDto.setDivision((String) list.get(0).getDivision());

		}

		return salesOrgDetailsDto;

	}

}
