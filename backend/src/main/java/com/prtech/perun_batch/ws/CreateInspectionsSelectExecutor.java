package com.prtech.perun_batch.ws;

import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.prtech.svarog.SvConf;
import com.prtech.svarog.SvCore;
import com.prtech.svarog.SvException;
import com.prtech.svarog.SvReader;
import com.prtech.svarog.SvWriter;
import com.prtech.svarog_common.DbDataArray;
import com.prtech.svarog_common.DbQueryObject;
import com.prtech.svarog_common.DbSearch.DbLogicOperand;
import com.prtech.svarog_common.DbSearchCriterion;
import com.prtech.svarog_common.DbSearchCriterion.DbCompareOperand;
import com.prtech.svarog_common.DbSearchExpression;
import com.prtech.svarog_interfaces.ISvCore;
import com.prtech.svarog_interfaces.ISvExecutor;

public class CreateInspectionsSelectExecutor implements ISvExecutor {
	static final Logger log4j = SvConf.getLogger(CreateInspectionsSelectExecutor.class);
	private static final String CATEGORY = "CREATE_INSPECTION";
	private static final String NAME = "SELECT";
	private static final String DESCRIPTION = "Create inspection";
	private static final DateTime START = new DateTime("2025-01-01T00:00:00+00");
	private static final DateTime END = new DateTime("9999-12-31T00:00:00+00");
	private static final Class<?> TYPE = DbDataArray.class;

	@Override
	public long versionUID() {
		return 3L;
	}

	@Override
	public Class<?> getReturningType() {
		return TYPE;
	}

	@Override
	public String getCategory() {
		return CATEGORY;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public DateTime getStartDate() {
		return START;
	}

	@Override
	public DateTime getEndDate() {
		return END;
	}

	@Override
	public Object execute(Map<String, Object> params, ISvCore core) throws SvException {
		DbDataArray dbDataArray = new DbDataArray();
		try (SvReader svr = new SvReader((SvCore) core); SvWriter svw = new SvWriter(svr);) {
			DbSearchExpression dbse = null;
			DbSearchCriterion dbc1 = null;
			DbSearchCriterion dbc2 = null;
			dbse = new DbSearchExpression();
			dbc1 = new DbSearchCriterion("STATUS", DbCompareOperand.EQUAL, "APPROVED");
			dbc1.setNextCritOperand(DbLogicOperand.AND.toString());
			dbse.addDbSearchItem(dbc1);
			if (params.get("APPLICATION_TYPE") != null) {
				Long appTypeId = (Long) params.get("APPLICATION_TYPE");
				dbc2 = new DbSearchCriterion("APP_TYPE_ID", DbCompareOperand.EQUAL, appTypeId);
				dbse.addDbSearchItem(dbc2);
			}
			DbQueryObject query = new DbQueryObject(SvCore.getDbtByName("APPLICATION"), dbse, null, null);
			dbDataArray = svr.getObjects(query, 0, 0);
		}
		return dbDataArray;
	}
}