package com.prtech.perun.ws;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.prtech.perun_core.ws.WsConf;
import com.prtech.perun_core.ws.WsReactElements;
import com.prtech.perun_core.ws.WsSecurityActions;
import com.prtech.svarog.SvConf;
import com.prtech.svarog.SvCore;
import com.prtech.svarog.SvException;
import com.prtech.svarog.SvParameter;
import com.prtech.svarog.SvReader;
import com.prtech.svarog.SvSecurity;
import com.prtech.svarog.SvUtil;
import com.prtech.svarog.SvWriter;
import com.prtech.svarog.svCONST;
import com.prtech.svarog_common.DbDataArray;
import com.prtech.svarog_common.DbDataObject;
import com.prtech.svarog_common.DbSearchCriterion;
import com.prtech.svarog_common.DbSearchExpression;
import com.prtech.svarog_common.DbSearchCriterion.DbCompareOperand;

public class Perun_core_tests {
	@Test
	public void formWizard() {
		SvSecurity svc;
		WsConf wsc = new WsConf();
		try {
			svc = new SvSecurity();
			String sessionId = svc.logon("ADMIN", SvUtil.getMD5("welcome"));
			//wsc.saveFormWizard(sessionId, "FILIP_TEST", "VFARMER", 1, null, null);
			System.out.print("finish");
		} catch (SvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void assigneUserToGroup() {
		SvSecurity svc;
		SvReader svr;
		try {
			svc = new SvSecurity();
			String sessionId = svc.logon("ADMIN", SvUtil.getMD5("welcome"));
			svr = new SvReader(sessionId);
			DbDataObject user = svc.getUser("Z.PESHEVSKI");
			DbDataObject userGroupa = svr.getObjectByUnqConfId("ADVANCED_ADM_CTRL_USERS", "SVAROG_USER_GROUPS");
			svc.addUserToGroup(user, userGroupa, false);
			System.out.print("finish");
		} catch (SvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getTableJSONSchema() {
		WsReactElements wsc = new WsReactElements();
		SvSecurity svc;
		try {
			svc = new SvSecurity();
			String sessionId = svc.logon("ADMIN", SvUtil.getMD5("welcome"));
			Response test = wsc.getTableJSONSchema(sessionId, "LAND_USE_PLAN", null);
			System.out.print(test);
		} catch (SvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void changePass() {
		WsSecurityActions wsc = new WsSecurityActions();
			Response test = wsc.changePassword("ADMIN", "2211221122112", "WELCOME", "WELCOME", null);
			System.out.print(test);
		}

//	@Test
//	public void getModule() {
//		WsSecurityActions wsa = new WsSecurityActions();
//		SvSecurity svc;
//		try {
//			svc = new SvSecurity();
//			String sessionId = svc.logon("ADMIN", SvUtil.getMD5("welcome"));
//			Response test = wsa.getCofnigModules(sessionId);
//		} catch (SvException e) {
//			e.printStackTrace();
//		}
//
//	}

	// @Test
	// public void sendActivationMail() {
	// WsSecurityActions wsa = new WsSecurityActions();
	// MultivaluedMap<String, String> formVals = new MultivaluedHashMap<>();
	// try {
	//
	// formVals.add("username", "00000258550");
	// formVals.add("password", SvUtil.getMD5("welcome"));
	// formVals.add("idNo", "1604957450101");
	// Response test = wsa.sendActivationLink(formVals, httpRequest);
	// System.out.print(test);
	// } catch (SvException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	@Test
	public void getUserGroups() {
		WsSecurityActions wsa = new WsSecurityActions();
		SvSecurity svc;
		try {
			svc = new SvSecurity();
			String sessionId = svc.logon("00000258550", SvUtil.getMD5("welcome"));
			JsonObject test = wsa.getCofnigModules(sessionId);
			System.out.print(test);
		} catch (SvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void createAllParamsForMailActivation() {
		setParamsForMailActivation("frontend.services_host", "http://192.168.9.46:8080/triglav_rest/");
		setParamsForMailActivation("frontend.gui_host", "http://192.168.100.155:8091/perun/index.html");
		setParamsForMailActivation("mail.from", "mail");
		setParamsForMailActivation("mail.username", "username");
		setParamsForMailActivation("mail.password", "password");
		setParamsForMailActivation("mail.host", "mail.ipardpa.gov.mk");
		setParamsForMailActivation("mail.smtp.auth", "true");
		setParamsForMailActivation("mail.smtp.starttls.enable", "true");
		setParamsForMailActivation("mail.smtp.port", "25");
		setParamsForMailActivation("mail.format", "text/html; charset=UTF-8");
	}

	public void setParamsForMailActivation(String label_code, String value_element) {
		SvReader svr = null;
		SvWriter svw = null;
		SvParameter svp = null;
		SvSecurity svc = null;
		try {
			svc = new SvSecurity();
			DbDataObject parentDbt = SvCore.getDbt(svCONST.OBJECT_TYPE_SECURITY_LOG);
			String sessionId = svc.logon("ADMIN", SvUtil.getMD5("welcome"));
			svr = new SvReader(sessionId);
			svw = new SvWriter(svr);
			svw.setAutoCommit(false);
			svp = new SvParameter(svw);

			DbDataObject paramType = searchForObject(svCONST.OBJECT_TYPE_PARAM_TYPE, "LABEL_CODE", label_code, svr);
			// if it doesn't exist, than it will create new type of parameter
			if (paramType == null) {
				createParamType(svCONST.OBJECT_TYPE_SECURITY_LOG, label_code, "NVARCHAR", "TEXT_AREA", null, null, null,
						null, 1L, "true", "mk_MK", svr, svw);
				// check if paramType created
				paramType = searchForObject(svCONST.OBJECT_TYPE_PARAM_TYPE, "LABEL_CODE", label_code, svr);
			}

			if (paramType != null) {
				svp.setParamString(parentDbt, label_code, value_element);
				String value = svp.getParamString(parentDbt, label_code);
				if (value == null)
					fail("Can't get value of parameters module for repo object");
				if (!value.equals(value_element)) {
					fail("Param value not as expected");
				}
			}
			svp.dbCommit();
		} catch (SvException ex) {
			ex.printStackTrace();
			fail(ex.getFormattedMessage());
		} finally {
			if (svr != null)
				svr.release();
			if (svw != null)
				svw.release();
			if (svp != null)
				svp.release();
		}
	}

	public DbDataObject searchForObject(long objToSearch, String columnToSearch, String valueToSearch,
			SvReader svReader) {
		DbDataArray reg_office = new DbDataArray();
		DbDataObject result = null;
		try {
			com.prtech.svarog_common.DbSearchExpression expr = new com.prtech.svarog_common.DbSearchExpression();
			expr.addDbSearchItem(new com.prtech.svarog_common.DbSearchCriterion(columnToSearch, DbCompareOperand.EQUAL,
					valueToSearch));
			/*
			 * expr.addDbSearchItem( new DbSearchCriterion("PARENT_CODE_VALUE",
			 * DbCompareOperand.EQUAL, "POPULATED_AREAS_IPARD"));
			 */
			reg_office = svReader.getObjects(expr, objToSearch, null, 0, 0);
			ArrayList<DbDataObject> list = reg_office.getItems();
			if (list.size() > 0) {
				result = list.get(0);
			}

		} catch (Exception ex) {
			System.out.println("Error in it.getSQLExpression()");
			ex.printStackTrace();
		}
		return result;

	}

	public void createParamType(Long parentId, String labelCode, String dataType, String inputType, String defaultValue,
			Long codeListId, String isMultipleValueOfEntry, String guiMetadata, Long paramOrder,
			String paramTypeLabelText, String labelLocaleId, SvReader svr, SvWriter svw) throws SvException {

		// check if paramTypeObject already exists
		DbDataObject paramTypeObj = searchForObject(svCONST.OBJECT_TYPE_PARAM_TYPE, "LABEL_CODE", labelCode, svr);
		// if not exist then create it
		if (paramTypeObj == null) {
			// prepare paramTypeObject and save it
			svw.saveObject(createParamTypeObj(parentId, labelCode, dataType, inputType, defaultValue, codeListId,
					isMultipleValueOfEntry, guiMetadata, paramOrder), false);
			// save label for paramTypeObject
			createLabel(labelCode, paramTypeLabelText, labelLocaleId, svw);
			System.out.println("ParamTypeObject with labelCode:" + labelCode + " saved with autoCommit:false.");
		} else {
			System.out.println("ParamTypeObject with labelCode:" + labelCode + " already exists.");

		}

	}

	public DbDataObject createParamTypeObj(Long parentId, String labelCode, String dataType, String inputType,
			String defaultValue, Long codeListId, String isMultipleValueOfEntry, String guiMetadata, Long paramOrder) {
		DbDataObject paramTypeObj = new DbDataObject();
		paramTypeObj.setObject_type(svCONST.OBJECT_TYPE_PARAM_TYPE);
		paramTypeObj.setStatus("VALID");
		paramTypeObj.setParent_id(parentId);
		paramTypeObj.setDt_insert(new DateTime());
		paramTypeObj.setDt_delete(SvConf.MAX_DATE);
		if (labelCode != null)
			paramTypeObj.setVal("LABEL_CODE", labelCode);
		if (dataType != null)
			paramTypeObj.setVal("DATA_TYPE", dataType);
		if (inputType != null)
			paramTypeObj.setVal("INPUT_TYPE", inputType);
		if (defaultValue != null)
			paramTypeObj.setVal("DEFAULT_VALUE", defaultValue);
		if (codeListId != null)
			paramTypeObj.setVal("CODE_LIST_ID", codeListId);
		if (isMultipleValueOfEntry != null)
			paramTypeObj.setVal("IS_MULTIPLE_VALUE_OF_ENTRY", isMultipleValueOfEntry);
		if (guiMetadata != null)
			paramTypeObj.setVal("GUI_METADATA", guiMetadata);
		if (paramOrder != null)
			paramTypeObj.setVal("PARAM_ORDER", paramOrder);

		return paramTypeObj;
	}

	public void createLabel(String labelCode, String labelDescr, String localeId, SvWriter svWriter)
			throws SvException {

		SvReader svReader = new SvReader(svWriter);
		try {
			DbDataObject labelObj = getLabel(labelCode, localeId, svReader);
			if (labelObj == null) {
				labelObj = new DbDataObject();
				labelObj.setStatus("VALID");
				labelObj.setDt_insert(new DateTime());
				labelObj.setDt_delete(SvConf.MAX_DATE);
				labelObj.setVal("label_code", labelCode);
				labelObj.setVal("label_text", labelDescr);
				labelObj.setObject_type(svCONST.OBJECT_TYPE_LABEL);
				labelObj.setVal("locale_id", localeId);
				svWriter.saveObject(labelObj, false);
			} else {
				String currLabelDescr = labelObj.getVal("LABEL_TEXT").toString();
				if (!currLabelDescr.equals(labelDescr)) {
					labelObj.setVal("LABEL_TEXT", labelDescr);
					svWriter.saveObject(labelObj, false);
					System.out.println("Object of type LABEL updated with label_code" + labelCode);
				} else {
					System.out.println("Object of type LABEL with label_code:" + labelCode + " already exists.");
				}
			}

		} finally {
			if (svReader != null) {
				svReader.release();
			}
		}
	}

	public DbDataObject getLabel(String labelCode, String localeId, SvReader svReader) throws SvException {

		DbDataObject result = null;

		DbSearchExpression getFormType = new DbSearchExpression();
		DbSearchCriterion filterByLabel = new DbSearchCriterion("LABEL_CODE", DbCompareOperand.EQUAL, labelCode);
		DbSearchCriterion filterByLocaleId = new DbSearchCriterion("LOCALE_ID", DbCompareOperand.EQUAL, localeId);
		getFormType.addDbSearchItem(filterByLabel);
		getFormType.addDbSearchItem(filterByLocaleId);
		DbDataArray searchResult = svReader.getObjects(getFormType, svCONST.OBJECT_TYPE_LABEL, null, 0, 0);

		if (searchResult.getItems().size() > 0) {
			result = searchResult.getItems().get(0);
		}
		return result;
	}
}
