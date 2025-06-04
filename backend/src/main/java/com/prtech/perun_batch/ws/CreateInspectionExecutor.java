package com.prtech.perun_batch.ws;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.prtech.svarog.SvConf;
import com.prtech.svarog.SvCore;
import com.prtech.svarog.SvException;
import com.prtech.svarog.SvLock;
import com.prtech.svarog.SvReader;
import com.prtech.svarog.SvSequence;
import com.prtech.svarog.SvWriter;
import com.prtech.svarog_common.DbDataObject;
import com.prtech.svarog_interfaces.ISvCore;
import com.prtech.svarog_interfaces.ISvExecutor;

public class CreateInspectionExecutor implements ISvExecutor {
	static final Logger log4j = SvConf.getLogger(CreateInspectionExecutor.class);
	private static final String CATEGORY = "CREATE_INSPECTION";
	private static final String NAME = "PROCESS";
	private static final String DESCRIPTION = "Create inspection";
	private static final DateTime START = new DateTime("2025-01-01T00:00:00+00");
	private static final DateTime END = new DateTime("9999-12-31T00:00:00+00");
	private static final Class<?> TYPE = DbDataObject.class;

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
		DbDataObject dboInspection = new DbDataObject();
		DbDataObject application = null;
		try (SvReader svr = new SvReader((SvCore) core); SvWriter svw = new SvWriter(svr);) {
			Long appObjectId = (Long) params.get("OBJECT_ID");
			String inspectionId = generateInspectionId(svr);
			String inspectionTypeString = "RANDOM";
			if (params.get("INSPECTION_TYPE") != null) {
				inspectionTypeString = (String) params.get("INSPECTION_TYPE");
			}
			application = svr.getObjectById(appObjectId, SvCore.getDbtByName("APPLICATION"), null);
			
			dboInspection.setParentId(application.getParentId());
			dboInspection.setVal("INSPECTION_ID", inspectionId);
			dboInspection.setVal("APPLICATION_ID", appObjectId);
			dboInspection.setVal("INSPECTION_TYPE", inspectionTypeString);
			dboInspection.setVal("ARCHIVE_NO", application.getVal("ARCH_NO"));
			dboInspection.setVal("HOLDING_ID", application.getParentId());
			dboInspection.setVal("START_DATE", DateTime.now());
		}
		return dboInspection;
	}

	
	private String generateInspectionId(SvReader svr) throws SvException {
		String inspectionId = null;
		try (SvSequence svs = new SvSequence(svr.getSessionId())) {
			ReentrantLock lock = null;
			Long sequenceValue = null;
			try {
				lock = SvLock.getLock(String.valueOf("INSPECTION_ID_SEQUENCE"), false, 0);
				if (lock == null) {
					throw (new SvException("sequence inspection id already in use", svr.getInstanceUser()));
				}
					sequenceValue = svs.getSeqNextVal("INSPECTION_ID_SEQUENCE", true);
			} finally {
				if (lock != null) {
					SvLock.releaseLock(String.valueOf("INSPECTION_ID_SEQUENCE"), lock);
				}
			}
			if (sequenceValue != null)
				inspectionId = String.format("%07d", sequenceValue.intValue());
		}
		return inspectionId;
	}
}