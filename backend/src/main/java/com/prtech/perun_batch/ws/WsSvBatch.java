package com.prtech.perun_batch.ws;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.prtech.perun.PerunUtil;
import com.prtech.svarog.I18n;
import com.prtech.svarog.SvConf;
import com.prtech.svarog.SvException;
import com.prtech.svarog.SvFileStore;
import com.prtech.svarog.SvLock;
import com.prtech.svarog.SvNote;
import com.prtech.svarog.SvReader;
import com.prtech.svarog.SvWriter;
import com.prtech.svarog.svCONST;
import com.prtech.svarog.batch.SvarogBatchManager;
import com.prtech.svarog_common.DbDataArray;
import com.prtech.svarog_common.DbDataObject;
import com.prtech.svarog_common.DbSearch;
import com.prtech.svarog_common.DbSearchCriterion;
import com.prtech.svarog_common.DbSearchCriterion.DbCompareOperand;
import com.prtech.svarog_common.DbSearchExpression;
import com.prtech.svarog_common.ResponseHandler;
import com.prtech.svarog_common.ResponseHandler.MessageType;

@Path("/SvBatch")
public class WsSvBatch {
	static final Logger log4j = SvConf.getLogger(WsSvBatch.class);

	@Path("/getBatchJobTypes/{token}")
	@GET
	@Produces("text/html;charset=utf-8")
	public Response getBatchJobTypes(@PathParam("token") String token) {
		try (SvReader svr = new SvReader(token);) {
			JsonArray jArray = new JsonArray();
			DbDataArray sbjtArr = SvarogBatchManager.getAllJobType(svr);
			JsonObject jObj = new JsonObject();
			jObj.addProperty("text", I18n.getText("batch.choose_type_process"));
			jObj.addProperty("value", 0);
			jObj.addProperty("selected", true);
			jObj.addProperty("disabled", true);
			jArray.add(jObj);
			ArrayList<DbDataObject> sorted = sbjtArr.getSortedItems("OBJECT_ID", true);
			for (int i = sorted.size() - 1; i >= 0; i--) {
				DbDataObject dbo = sorted.get(i);
				jObj = new JsonObject();
				jObj.addProperty("text", I18n.getText(dbo.getVal("LABEL_CODE").toString()));
				jObj.addProperty("value", dbo.getObjectId());
				jArray.add(jObj);
			}
			return Response.status(200).entity(jArray.toString()).build();
		} catch (SvException e) {
			ResponseHandler jrh = new ResponseHandler();

			if (e.getLabelCode().equals("error.invalid_session")) {
				log4j.error(e.getFormattedMessage());
				jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
						I18n.getText("error.invalid_session"), new JsonObject());
				return Response.status(401).entity(jrh.getAll().toString()).build();
			}
			log4j.error(e.getFormattedMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.get_job_types"),
					I18n.getText("batch.error.get_job_types"), new JsonObject());
			return Response.status(200).entity(jrh.getAll().toString()).build();
		}
	}

	@Path("/getBatchJobTemplates/{token}/{jobTypeId}")
	@GET
	@Produces("text/html;charset=utf-8")
	public Response getBatchJobTemplates(@PathParam("token") String token, @PathParam("jobTypeId") Long jobTypeId) {
		try (SvReader svr = new SvReader(token);) {
			JsonArray jArray = new JsonArray();
			DbDataArray sbjttArr = svr.getObjectsByParentId(jobTypeId, svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE, null, 0,
					0);
			JsonObject jObj = new JsonObject();
			jObj.addProperty("text", I18n.getText("batch.choose_process"));
			jObj.addProperty("value", 0);
			jObj.addProperty("selected", true);
			jObj.addProperty("disabled", true);
			jArray.add(jObj);
			ArrayList<DbDataObject> sorted = sbjttArr.getSortedItems("OBJECT_ID", true);
			for (int i = sorted.size() - 1; i >= 0; i--) {
				DbDataObject dbo = sorted.get(i);
				jObj = new JsonObject();
				jObj.addProperty("text", I18n.getText(dbo.getVal("LABEL_CODE").toString()));
				jObj.addProperty("value", dbo.getObjectId());
				jArray.add(jObj);
			}
			return Response.status(200).entity(jArray.toString()).build();
		} catch (SvException e) {
			ResponseHandler jrh = new ResponseHandler();

			if (e.getLabelCode().equals("error.invalid_session")) {
				log4j.error(e.getFormattedMessage());
				jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
						I18n.getText("error.invalid_session"), new JsonObject());
				return Response.status(401).entity(jrh.getAll().toString()).build();
			}
			log4j.error(e.getFormattedMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.get_job_templates"),
					I18n.getText("batch.error.get_job_templates"), new JsonObject());
			return Response.status(200).entity(jrh.getAll().toString()).build();
		}
	}

	@Path("/getBatchJobTypeTemplates/{token}/{batchJobTypeId}")
	@GET
	@Produces("text/html;charset=utf-8")
	public Response getBatchJobTypeTemplates(@PathParam("token") String token,
			@PathParam("batchJobTypeId") Long batchJobTypeId) {

		try (SvReader svr = new SvReader(token);) {
			DbDataArray sbjtpArr = svr.getObjectsByParentId(batchJobTypeId, svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE,
					null, 0, 0);
			JsonArray jArray = new JsonArray();
			ArrayList<DbDataObject> sorted = sbjtpArr.getSortedItems("OBJECT_ID", true);
			for (int i = sorted.size() - 1; i >= 0; i--) {
				DbDataObject dbo = sorted.get(i);
				JsonObject jObj = new JsonObject();
				jObj.addProperty("key", I18n.getText(dbo.getVal("LABEL_CODE").toString()));
				jObj.addProperty("value", dbo.getObjectId());
				if (dbo.getVal("PARAMS") != null) {
					jArray = (JsonArray) dbo.getVal("PARAMS");
					jObj.addProperty("params", (String) dbo.getVal("PARAMS"));
				}
				jArray.add(jObj);
			}
			return Response.status(200).entity(jArray.toString()).build();
		} catch (SvException e) {
			ResponseHandler jrh = new ResponseHandler();
			if (e.getLabelCode().equals("error.invalid_session")) {
				log4j.error(e.getFormattedMessage());
				jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
						I18n.getText("error.invalid_session"), new JsonObject());
				return Response.status(401).entity(jrh.getAll().toString()).build();
			}
			log4j.error(e.getFormattedMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.get_job_type_templates"),
					I18n.getText("batch.error.get_job_type_templates"), new JsonObject());
			return Response.status(200).entity(jrh.getAll().toString()).build();
		}
	}

	@Path("/getParamByTemplate/{token}/{templateId}")
	@GET
	@Produces("text/html;charset=utf-8")
	public Response getParamByTemplate(@PathParam("token") String token, @PathParam("templateId") Long templateId) {
		ResponseHandler jrh = new ResponseHandler();
		try (SvReader svr = new SvReader(token);) {
			DbDataObject dbTemplate = svr.getObjectById(templateId, svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE, null);
			JsonArray jArry = new JsonArray();
			if (dbTemplate != null) {
				if (dbTemplate.getVal("PARAMS") != null) {
					jArry = new Gson().fromJson((String) dbTemplate.getVal("PARAMS"), JsonArray.class);
				}
				jrh.create(MessageType.SUCCESS, I18n.getText("batch.success.create_job"),
						I18n.getText("batch.success.create_job"), jArry);
			} else {
				jrh.create(MessageType.ERROR, I18n.getText("batch.not_found_template"),
						I18n.getText("batch.not_found_template"), new JsonObject());
			}
		} catch (SvException e) {
			if (e.getLabelCode().equals("error.invalid_session")) {
				log4j.error(e.getFormattedMessage());
				jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
						I18n.getText("error.invalid_session"), new JsonObject());
				return Response.status(401).entity(jrh.getAll().toString()).build();
			}
			log4j.error(e.getFormattedMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.template_params"),
					I18n.getText("batch.error.template_params"), new JsonObject());
		}
		return Response.status(200).entity(jrh.getAll().toString()).build();
	}

	@Path("/getBatchJob/{token}/{templateId}")
	@GET
	@Produces("text/html;charset=utf-8")
	public Response getBatchJob(@PathParam("token") String token, @PathParam("templateId") Long template_id) {
		try (SvReader svr = new SvReader(token);) {
			JsonArray jArray = new JsonArray();
			DbSearch search = new DbSearchCriterion("TEMPLATE_ID", DbCompareOperand.EQUAL, template_id);
			ArrayList<DbDataObject> sbjArr = svr.getObjects(search, svCONST.OBJECT_TYPE_BATCH_JOB, null, 0, 0)
					.getSortedItems("PKID", true);
			// add reverse iterating of the sorted list
			ListIterator<DbDataObject> listIterator = sbjArr.listIterator(sbjArr.size());
			while (listIterator.hasPrevious()) {
				jArray.add(listIterator.previous().toSimpleJson());
			}
			return Response.status(200).entity(jArray.toString()).build();
		} catch (SvException e) {
			ResponseHandler jrh = new ResponseHandler();
			if (e.getLabelCode().equals("error.invalid_session")) {
				log4j.error(e.getFormattedMessage());
				jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
						I18n.getText("error.invalid_session"), new JsonObject());
				return Response.status(401).entity(jrh.getAll().toString()).build();
			} else {
				log4j.error(e.getFormattedMessage(), e);
				jrh.create(MessageType.ERROR, I18n.getText("batch.error.get_jobs"),
						I18n.getText("batch.error.get_jobs"), new JsonObject());
				return Response.status(200).entity(jrh.getAll().toString()).build();
			}
		}
	}

	@Path("/getInfoForActiveJob/{token}/{jobId}")
	@GET
	@Produces("text/html;charset=utf-8")
	public Response getInfoForActiveJob(@PathParam("token") String token, @PathParam("jobId") Long jobId) {
		ResponseHandler jrh = new ResponseHandler();
		try (SvNote svn = new SvNote(token);) {
			jrh.create(MessageType.SUCCESS, I18n.getText("batch.job.info"), I18n.getText("batch.job.info"),
					SvarogBatchManager.getInfoByJob(jobId, svn));
		} catch (SvException e) {
			if (e.getLabelCode().equals("error.invalid_session")) {
				log4j.error(e.getFormattedMessage());
				jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
						I18n.getText("error.invalid_session"), new JsonObject());
				return Response.status(401).entity(jrh.getAll().toString()).build();
			}
			log4j.error(e.getFormattedMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.get_job_info"),
					I18n.getText("batch.error.get_jo_info"), new JsonObject());
		}
		return Response.status(200).entity(jrh.getAll().toString()).build();
	}

	@Path("/createBatchJob/{token}")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/json")
	public Response createBatchJob(@PathParam("token") String token, MultivaluedMap<String, String> formVals,
			@Context HttpServletRequest httpRequest) {
		ResponseHandler jrh = new ResponseHandler();
		String jobName = "";
		String note = "";
		Long jobTyprId = null;
		Long numOfThread = null;
		Long templateId = null;
		String jsonObjString = "";
		ReentrantLock lock = null;
		String LOCK_KEY = "";
		try (SvWriter svw = new SvWriter(token);) {
			if (formVals != null)
				for (Entry<String, List<String>> entry : formVals.entrySet()) {
					if (entry.getKey() != null && !entry.getKey().isEmpty()) {
						String key = entry.getKey();
						jsonObjString = key;
					}
				}

			JsonObject jsonData = null;
			Gson gson = new Gson();
			jsonData = gson.fromJson(jsonObjString, JsonObject.class);
			if (jsonData.has("name")) {
				jobName = jsonData.get("name").getAsString();
				LOCK_KEY += jobName;
			}
			if (jsonData.has("jobTypeId")) {
				jobTyprId = jsonData.get("jobTypeId").getAsLong();
			}
			if (jsonData.has("note")) {
				note = jsonData.get("note").getAsString();
				LOCK_KEY += ("-" + note);
			}
			if (jsonData.has("numOfThread")) {
				numOfThread = jsonData.get("numOfThread").getAsLong();
			}
			if (jsonData.has("templateId")) {
				templateId = jsonData.get("templateId").getAsLong();
			}
			lock = SvLock.getLock(LOCK_KEY, false, 0);
			if (lock != null) {
				DbDataObject dbBatchJob = SvarogBatchManager.createJob(jobName, jobTyprId, note, numOfThread,
						templateId);

				svw.saveObject(dbBatchJob);
				JsonObject jo = new JsonObject();
				jo.addProperty("jobId", dbBatchJob.getObjectId());

				jrh.create(MessageType.SUCCESS, I18n.getText("batch.success.create_job"),
						I18n.getText("batch.success.create_job"), jo);
			} else {
				jrh.create(MessageType.WARNING, I18n.getText("batch.warning.job_already_created"),
						I18n.getText("batch.warning.job_already_created"), new JsonObject());
			}
		} catch (SvException e) {
			if (e.getLabelCode().equals("error.invalid_session")) {
				log4j.error(e.getFormattedMessage());
				jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
						I18n.getText("error.invalid_session"), new JsonObject());
				return Response.status(401).entity(jrh.getAll().toString()).build();
			}
			log4j.error(e.getFormattedMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.create_job"),
					I18n.getText("batch.error.create_job"), new JsonObject());
		} finally {
			if (lock != null) {
				SvLock.releaseLock(LOCK_KEY, lock);
			}
		}
		return Response.status(200).entity(jrh.getAll().toString()).build();
	}

	@Path("/saveParamsByJob/{token}")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/json")
	public Response saveParamsByJob(@PathParam("token") String token, FormDataMultiPart multipart,
			@Context HttpServletRequest httpRequest) {
		ResponseHandler jrh = new ResponseHandler();
		String templateParams = "";
		Long jobId = null;
		String jsonObjString = "";
		byte[] file = null;
		InputStream in = null;
		String contentType = "";
		String fileName = "";
		DbSearch dbs = null;
		DbSearch dbs1 = null;
		DbSearchExpression dbexp = null;
		DbDataArray ret = null;
		DbDataObject dbJob = null;
		try (SvReader svr = new SvReader(token);
				SvWriter svw = new SvWriter(svr);
				SvFileStore svfs = new SvFileStore(svr);) {

			DbDataObject fileDescriptor = null;
			JsonArray jArry = new JsonArray();
			Map<String, List<FormDataBodyPart>> map = multipart.getFields();
			for (Map.Entry<String, List<FormDataBodyPart>> entry : map.entrySet()) {
				for (FormDataBodyPart part : entry.getValue()) {
					if (!part.getName().equals("param")) {
						in = part.getValueAs(InputStream.class);
						file = IOUtils.toByteArray(in);

						contentType = part.getMediaType().toString();
						fileName = part.getName();
						dbs = new DbSearchCriterion("FILE_NAME", DbCompareOperand.EQUAL, fileName);
						dbs1 = new DbSearchCriterion("FILE_TYPE", DbCompareOperand.EQUAL, "ATTACHMENT");
						dbexp = new DbSearchExpression().addDbSearchItem(dbs).addDbSearchItem(dbs1);

						ret = svr.getObjects(dbexp, svCONST.OBJECT_TYPE_FILE, null, 0, 0);

						if (ret == null || ret.getItems().isEmpty()) {

							fileDescriptor = new DbDataObject();
							fileDescriptor.setObjectType(svCONST.OBJECT_TYPE_FILE);
							fileDescriptor.setObjectType(svCONST.OBJECT_TYPE_FILE);
							fileDescriptor.setVal("file_size", file.length);
							fileDescriptor.setVal("file_date", new DateTime());
							fileDescriptor.setVal("file_name", fileName);
							fileDescriptor.setVal("file_type", "ATTACHMENT");
							fileDescriptor.setVal("content_type", contentType);
							fileDescriptor.setVal("FILE_STORE_ID", svCONST.SYSTEM_FILESTORE_ID);

						} else {
							jrh.create(MessageType.ERROR, I18n.getText("batch.error.file_already_exist"),
									I18n.getText("batch.error.file_already_exist"), new JsonObject());
							return Response.status(200).entity(jrh.getAll().toString()).build();
						}
					} else {
						jsonObjString = part.getValue();
					}
				}
			}

			if (!jsonObjString.equals("")) {
				JsonObject jsonData = null;
				Gson gson = new Gson();
				jsonData = gson.fromJson(jsonObjString, JsonObject.class);

				if (jsonData.has("jobId")) {
					jobId = jsonData.get("jobId").getAsLong();
				}

				dbJob = svr.getObjectById(jobId, svCONST.OBJECT_TYPE_BATCH_JOB, null);
				DbDataObject dbTemplate = svr.getObjectById((Long) dbJob.getVal("TEMPLATE_ID"),
						svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE, null);

				if (dbTemplate != null) {
					if (dbTemplate.getVal("PARAMS") != null) {
						jArry = new Gson().fromJson((String) dbTemplate.getVal("PARAMS"), JsonArray.class);
					}
				}
				JsonObject jObj;
				String value = "";
				JsonArray jArryParams = new JsonArray();
				for (JsonElement jElement : jArry) {
					jObj = jElement.getAsJsonObject();

					if (jObj.has("type") && jObj.get("type").getAsString().equalsIgnoreCase("attachment")) {
						jObj.addProperty("value", fileName);
					} else if (jsonData.has(jObj.get("id").getAsString())) {

						value = jsonData.get(jObj.get("id").getAsString()).getAsString().trim();

						jObj.addProperty("value", value);
					}

					jArryParams.add(jObj);
				}
				DbDataArray arrJobParam = svr.getObjectsByParentId(jobId, svCONST.OBJECT_TYPE_BATCH_JOB_PARAM, null, 0,
						0);

				DbDataObject dbJobParam = null;
				if (!arrJobParam.getItems().isEmpty()) {
					dbJobParam = arrJobParam.get(0);
				}
				templateParams = new Gson().toJson(jArryParams);
				DbDataObject dbBatchJobParam = SvarogBatchManager.createJobParam(dbJobParam, jobId, templateParams);

				svw.saveObject(dbBatchJobParam, false);
			}
			if (!fileName.equals("")) {
				svfs.saveFile(fileDescriptor, dbJob, file, false);
			}
			svfs.dbCommit();
			svw.dbCommit();
			jrh.create(MessageType.SUCCESS, I18n.getText("batch.success.create_job_param"),
					I18n.getText("batch.success.create_job_param"), new JsonObject());
		} catch (Exception e) {

			if (e instanceof SvException) {
				SvException e1 = (SvException) e;
				if (e1.getLabelCode().equals("error.invalid_session")) {
					log4j.error(e1.getFormattedMessage());
					jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
							I18n.getText("error.invalid_session"), new JsonObject());
					return Response.status(401).entity(jrh.getAll().toString()).build();
				}
				log4j.error(e1.getFormattedMessage(), e1);

			} else {
				log4j.error(e.getMessage(), e);
			}

			jrh.create(MessageType.ERROR, I18n.getText("batch.error.create_job_param"),
					I18n.getText("batch.error.create_job_param"), new JsonObject());
		}
		return Response.status(200).entity(jrh.getAll().toString()).build();
	}

	@Path("/runBatchJob/{token}")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/json")
	public Response runBatchJob(@PathParam("token") String token, MultivaluedMap<String, String> formVals,
			@Context HttpServletRequest httpRequest) {
		ResponseHandler jrh = new ResponseHandler();
		String jsonObjString = "";
		Long jobId = null;
		try {
			if (formVals != null)
				for (Entry<String, List<String>> entry : formVals.entrySet()) {
					if (entry.getKey() != null && !entry.getKey().isEmpty()) {
						String key = entry.getKey();
						jsonObjString = key;
					}
				}

			JsonObject jsonData = null;
			Gson gson = new Gson();
			jsonData = gson.fromJson(jsonObjString, JsonObject.class);

			if (jsonData.has("jobId")) {
				jobId = jsonData.get("jobId").getAsLong();
			}
			if (jobId != null) {
				SvarogBatchManager sbm = new SvarogBatchManager();
				sbm.runJob(jobId, token);

				jrh.create(MessageType.SUCCESS, I18n.getText("batch.success.running_job"),
						I18n.getText("batch.success.running_job"), new JsonObject());
			} else {
				jrh.create(MessageType.ERROR, I18n.getText("batch.error.not_found_jobId"),
						I18n.getText("batch.error.not_found_jobId"), new JsonObject());
			}
		} catch (Exception e) {
			if (e instanceof SvException) {
				SvException e1 = (SvException) e;
				if (e1.getLabelCode().equals("error.invalid_session")) {
					log4j.error(e1.getFormattedMessage());
					jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
							I18n.getText("error.invalid_session"), new JsonObject());
					return Response.status(401).entity(jrh.getAll().toString()).build();
				}
			}
			log4j.error(e.getMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.running_job"),
					I18n.getText("batch.error.running_job"), new JsonObject());
		}
		return Response.status(200).entity(jrh.getAll().toString()).build();
	}

	@Path("/selectObjects/{token}")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/json")
	public Response selectObjects(@PathParam("token") String token, MultivaluedMap<String, String> formVals,
			@Context HttpServletRequest httpRequest) {
		ResponseHandler jrh = new ResponseHandler();
		String jsonObjString = "";
		Long jobId = null;
		try {
			if (formVals != null)
				for (Entry<String, List<String>> entry : formVals.entrySet()) {
					if (entry.getKey() != null && !entry.getKey().isEmpty()) {
						String key = entry.getKey();
						jsonObjString = key;
					}
				}

			JsonObject jsonData = null;
			Gson gson = new Gson();
			jsonData = gson.fromJson(jsonObjString, JsonObject.class);

			if (jsonData.has("jobId")) {
				jobId = jsonData.get("jobId").getAsLong();
			}
			if (jobId != null) {
				SvarogBatchManager sbm = new SvarogBatchManager();
				Long numOfSelectedObjects = sbm.selectObjects(jobId, token);
				JsonObject jSon = new JsonObject();
				jSon.addProperty("numSelectedApps", numOfSelectedObjects);

				jrh.create(MessageType.SUCCESS, I18n.getText("batch.success.select_objects"),
						I18n.getText("batch.success.select_objects"), jSon);
			} else {
				jrh.create(MessageType.ERROR, I18n.getText("batch.error.not_found_jobId"),
						I18n.getText("batch.error.not_found_jobId"), new JsonObject());
			}
		} catch (Exception e) {
			if (e instanceof SvException) {
				SvException e1 = (SvException) e;
				if (e1.getLabelCode().equals("error.invalid_session")) {
					log4j.error(e1.getFormattedMessage());
					jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
							I18n.getText("error.invalid_session"), new JsonObject());
					return Response.status(401).entity(jrh.getAll().toString()).build();
				}
			}
			log4j.error(e.getMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.select_objects"),
					I18n.getText("batch.error.select_objects"), new JsonObject());
		}
		return Response.status(200).entity(jrh.getAll().toString()).build();
	}

	@Path("/selectObjects/{token}/trigger")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response selectObjectsTrigger(@PathParam("token") String token, String entity,
			@Context HttpServletRequest httpRequest) {
		ResponseHandler jrh = new ResponseHandler();
		JsonObject requestData = new JsonObject();
		Long jobId = null;
		try {
			try {
				requestData = new Gson().fromJson(entity, JsonObject.class);
			} catch (JsonSyntaxException e) {
				jrh.create(MessageType.ERROR, I18n.getText("error.bad_json"), CC.EMPTY_STRING);
				return Response.status(400).entity(jrh.getAll().toString()).build();
			}
			if (requestData.has("jobId")) {
				jobId = requestData.get("jobId").getAsLong();
			}

			if (jobId == null) {
				jrh.create(MessageType.ERROR, I18n.getText("batch.error.not_found_jobId"), CC.EMPTY_STRING);
				return Response.status(400).entity(jrh.getAll().toString()).build();
			}

			SvarogBatchManager sbm = new SvarogBatchManager();
			sbm.selectObjectsTrigger(jobId, token);
			JsonObject responseJson = new JsonObject();
			responseJson.addProperty("jobId", jobId);

			jrh.create(MessageType.SUCCESS, I18n.getText("batch.success.select_objects_triggered"), CC.EMPTY_STRING,
					responseJson);

			return Response.status(202).entity(jrh.getAll().toString()).build();
		} catch (Exception e) {
			log4j.error(e.getMessage(), e);
			return PerunUtil.handleException(e, "batch.error.select_objects");
		}
	}

	@Path("/stopBatchJob")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/json")
	public Response stopBatchJob(MultivaluedMap<String, String> formVals, @Context HttpServletRequest httpRequest) {
		ResponseHandler jrh = new ResponseHandler();
		String jsonObjString = "";
		Long jobId = null;
		try {
			if (formVals != null)
				for (Entry<String, List<String>> entry : formVals.entrySet()) {
					if (entry.getKey() != null && !entry.getKey().isEmpty()) {
						String key = entry.getKey();
						jsonObjString = key;
					}
				}

			JsonObject jsonData = null;
			Gson gson = new Gson();
			jsonData = gson.fromJson(jsonObjString, JsonObject.class);

			if (jsonData.has("jobId")) {
				jobId = jsonData.get("jobId").getAsLong();
			}
			if (jobId != null) {
				String message = SvarogBatchManager.stopJob(jobId);

				jrh.create(MessageType.SUCCESS, I18n.getText(message), I18n.getText(message), new JsonObject());
			} else {
				jrh.create(MessageType.ERROR, I18n.getText("batch.error.not_found_jobId"),
						I18n.getText("batch.error.not_found_jobId"), new JsonObject());
			}
		} catch (Exception e) {
			if (e instanceof SvException) {
				SvException e1 = (SvException) e;
				if (e1.getLabelCode().equals("error.invalid_session")) {
					log4j.error(e1.getFormattedMessage());
					jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
							I18n.getText("error.invalid_session"), new JsonObject());
					return Response.status(401).entity(jrh.getAll().toString()).build();
				}
			}
			log4j.error(e.getMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.stopping_job"),
					I18n.getText("batch.error.stopping_job"), new JsonObject());
		}
		return Response.status(200).entity(jrh.getAll().toString()).build();
	}

	@Path("/getProcessForm/{token}")
	@GET
	@Produces("application/json")
	public Response getProcessForm(@PathParam("token") String token, @Context HttpServletRequest httpRequest) {
		try (SvReader svr = new SvReader(token);) {
			JsonArray jArray = new JsonArray();
			JsonObject jObj = new JsonObject();
			JsonObject jObj2 = new JsonObject();
			JsonObject jObj3 = new JsonObject();
			/* TODO create from svarog object f.r */
			jObj.addProperty("name", I18n.getText("job_title"));
			jObj.addProperty("placeholder", I18n.getText("batch.process_name"));
			jObj.addProperty("label", I18n.getText("batch.field_name_processe"));
			jObj.addProperty("type", "input");
			jObj.addProperty("id", I18n.getText("batch.field_name_process"));
			jArray.add(jObj);

			jObj2.addProperty("name", I18n.getText("num_processors"));
			jObj2.addProperty("placeholder", I18n.getText("batch.processor_num"));
			jObj2.addProperty("label", I18n.getText("batch.field_processors"));
			jObj2.addProperty("type", "number");
			jObj2.addProperty("id", I18n.getText("batch.field_processors"));
			jArray.add(jObj2);

			jObj3.addProperty("name", I18n.getText("note"));
			jObj3.addProperty("placeholder", I18n.getText("batch.note"));
			jObj3.addProperty("label", I18n.getText("batch.field_note"));
			jObj3.addProperty("type", "textarea");
			jObj3.addProperty("id", I18n.getText("batch.field_field_note"));
			jObj3.addProperty("rows", 4);
			jArray.add(jObj3);

			return Response.status(200).entity(jArray.toString()).build();
		} catch (SvException e) {
			ResponseHandler jrh = new ResponseHandler();
			if (e.getLabelCode().equals("error.invalid_session")) {
				jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
						I18n.getText("error.invalid_session"), new JsonObject());
				return Response.status(401).entity(jrh.getAll().toString()).build();
			} else {
				jrh.create(MessageType.ERROR, I18n.getText("batch.error.getProcessForm"),
						I18n.getText("batch.error.getProcessForm"), new JsonObject());
				return Response.status(200).entity(jrh.getAll().toString()).build();
			}
		}
	}

	@Path("/reRunBatchJob/{token}")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/json")
	public Response reRunBatchJob(@PathParam("token") String token, MultivaluedMap<String, String> formVals,
			@Context HttpServletRequest httpRequest) {
		ResponseHandler jrh = new ResponseHandler();
		String jsonObjString = "";
		Long jobId = null;
		try {
			if (formVals != null)
				for (Entry<String, List<String>> entry : formVals.entrySet()) {
					if (entry.getKey() != null && !entry.getKey().isEmpty()) {
						String key = entry.getKey();
						jsonObjString = key;
					}
				}

			JsonObject jsonData = null;
			Gson gson = new Gson();
			jsonData = gson.fromJson(jsonObjString, JsonObject.class);

			if (jsonData.has("jobId")) {
				jobId = jsonData.get("jobId").getAsLong();
			}
			if (jobId != null) {
				SvarogBatchManager sbm = new SvarogBatchManager();
				sbm.runJob(jobId, true, token);

				jrh.create(MessageType.SUCCESS, I18n.getText("batch.success.running_job"),
						I18n.getText("batch.success.running_job"), new JsonObject());
			} else {
				jrh.create(MessageType.ERROR, I18n.getText("batch.error.not_found_jobId"),
						I18n.getText("batch.error.not_found_jobId"), new JsonObject());
			}
		} catch (Exception e) {
			if (e instanceof SvException) {
				SvException e1 = (SvException) e;
				if (e1.getLabelCode().equals("error.invalid_session")) {
					log4j.error(e1.getFormattedMessage());
					jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
							I18n.getText("error.invalid_session"), new JsonObject());
					return Response.status(401).entity(jrh.getAll().toString()).build();
				}
			}
			log4j.error(e.getMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.running_job"),
					I18n.getText("batch.error.running_job"), new JsonObject());
		}
		return Response.status(200).entity(jrh.getAll().toString()).build();
	}

	@GET
	@Path("/getFile/{token}/{jobId}")
	@Produces("application/zip")
	public Response getFile(@PathParam("token") String token, @PathParam("jobId") Long jobId) {
		String filePath = "";
		ResponseHandler jrh = new ResponseHandler();
		try (SvReader svr = new SvReader(token);) {
			ArrayList<DbDataObject> jobHistory = svr
					.getObjectsHistory(new DbSearchCriterion("OBJECT_ID", DbCompareOperand.EQUAL, jobId),
							SvReader.getTypeIdByName("SVAROG_BATCH_JOB"), 0, 0)
					.getSortedItems("PKID", true);
			DbDataObject job = jobHistory.get(0);

			filePath = (SvConf.getParam("filestore.batch.path") != null ? SvConf.getParam("filestore.batch.path")
					: "sv_batch_fs") + File.separator + String.format("%02d", job.getDtInsert().getMonthOfYear())
					+ job.getDtInsert().getYear() + File.separator + job.getObjectId();
			File file = new File(filePath + ".zip");

			ResponseBuilder responseBuilder = Response.ok((Object) file).status(200);
			responseBuilder.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
			return responseBuilder.build();
		} catch (Exception e) {
			if (e instanceof SvException) {
				SvException e1 = (SvException) e;
				if (e1.getLabelCode().equals("error.invalid_session")) {
					log4j.error(e1.getFormattedMessage());
					jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
							I18n.getText("error.invalid_session"), new JsonObject());
					return Response.status(401).entity(jrh.getAll().toString()).build();
				}
			}
			log4j.error(e.getMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.getFile"), I18n.getText("batch.error.getFile"),
					new JsonObject());
		}
		return Response.status(200).entity(jrh.getAll().toString()).build();
	}

	@Path("/getReports/{token}/{jobId}")
	@GET
	@Produces("text/html;charset=utf-8")
	public Response getReports(@PathParam("token") String token, @PathParam("jobId") Long jobId) {
		try (SvReader svr = new SvReader(token);) {
			DbDataObject job = svr.getObjectById(jobId, svCONST.OBJECT_TYPE_BATCH_JOB, null);
			JsonArray jArray = new JsonArray();
			if (job.getStatus().equals("FINISHED")) {
				DbDataObject dbJobTemplate = svr.getObjectById((Long) job.getVal("TEMPLATE_ID"),
						svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE, null);
				if (dbJobTemplate != null && dbJobTemplate.getVal("REPORTS") != null) {
					jArray = new Gson().fromJson((String) dbJobTemplate.getVal("REPORTS"), JsonArray.class);
				}
			}
			return Response.status(200).entity(jArray.toString()).build();
		} catch (Exception e) {
			ResponseHandler jrh = new ResponseHandler();
			if (e instanceof SvException) {
				SvException e1 = (SvException) e;
				if (e1.getLabelCode().equals("error.invalid_session")) {
					log4j.error(e1.getFormattedMessage());
					jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
							I18n.getText("error.invalid_session"), new JsonObject());
					return Response.status(401).entity(jrh.getAll().toString()).build();
				}
			}
			log4j.error(e.getMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.get_reports"),
					I18n.getText("batch.error.get_reports"), new JsonObject());

			return Response.status(200).entity(jrh.getAll().toString()).build();
		}
	}

	@Path("/getActions/{token}/{jobId}")
	@GET
	@Produces("text/html;charset=utf-8")
	public Response getActions(@PathParam("token") String token, @PathParam("jobId") Long jobId) {
		try (SvReader svr = new SvReader(token);) {
			DbDataObject job = svr.getObjectById(jobId, svCONST.OBJECT_TYPE_BATCH_JOB, null);
			JsonArray jArray = new JsonArray();
			if (job.getStatus().equals("FINISHED")) {
				DbDataObject dbJobTemplate = svr.getObjectById((Long) job.getVal("TEMPLATE_ID"),
						svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE, null);
				if (dbJobTemplate != null && dbJobTemplate.getVal("ACTIONS") != null) {
					jArray = new Gson().fromJson((String) dbJobTemplate.getVal("ACTIONS"), JsonArray.class);
				}
			}
			return Response.status(200).entity(jArray.toString()).build();
		} catch (Exception e) {
			ResponseHandler jrh = new ResponseHandler();

			if (e instanceof SvException) {
				SvException e1 = (SvException) e;
				if (e1.getLabelCode().equals("error.invalid_session")) {
					log4j.error(e1.getFormattedMessage());
					jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
							I18n.getText("error.invalid_session"), new JsonObject());
					return Response.status(401).entity(jrh.getAll().toString()).build();
				}
			}
			log4j.error(e.getMessage(), e);
			jrh.create(MessageType.ERROR, I18n.getText("batch.error.get_actions"),
					I18n.getText("batch.error.get_actions"), new JsonObject());

			return Response.status(200).entity(jrh.getAll().toString()).build();
		}
	}

	@Path("/getBatchJobParam/{token}/{batchJobId}")
	@GET
	@Produces("text/html;charset=utf-8")
	public Response getBatchJobParam(@PathParam("token") String token, @PathParam("batchJobId") Long batchJobId) {
		ResponseHandler jrh = new ResponseHandler();
		JsonArray jArry = new JsonArray();
		try (SvReader svr = new SvReader(token)) {
			DbDataArray params = svr.getObjectsByParentId(batchJobId,
					SvReader.getTypeIdByName("SVAROG_BATCH_JOB_PARAM"), null);
			if (null == params || params.size() == 0) {
				jrh.create(MessageType.ERROR, I18n.getText("batch.not_found_params"),
						I18n.getText("batch.not_found_params"), new JsonObject());
			} else {
				DbDataObject param = params.get(0);
				if (param != null) {
					if (param.getVal("PARAMS_VALUE") != null) {
						jArry = new Gson().fromJson((String) param.getVal("PARAMS_VALUE"), JsonArray.class);
					}
					jrh.create(MessageType.SUCCESS, I18n.getText("batch.success.get_job_params"),
							I18n.getText("batch.success.get_job_params"), jArry);
				} else {
					jrh.create(MessageType.ERROR, I18n.getText("batch.not_found_params"),
							I18n.getText("batch.not_found_params"), new JsonObject());
				}
			}
			return Response.status(200).entity(jArry.toString()).build();
		} catch (SvException e) {
			jrh = new ResponseHandler();
			if (e.getLabelCode().equals("error.invalid_session")) {
				log4j.error(e.getFormattedMessage());
				jrh.create(MessageType.ERROR, I18n.getText("error.invalid_session"),
						I18n.getText("error.invalid_session"), new JsonObject());
				return Response.status(401).entity(jrh.getAll().toString()).build();
			} else {
				log4j.error(e.getFormattedMessage(), e);
				jrh.create(MessageType.ERROR, I18n.getText("batch.error.get_job_params"),
						I18n.getText("batch.error.get_job_params"), new JsonObject());
				return Response.status(200).entity(jrh.getAll().toString()).build();
			}

		}
	}

	@Path("/create-batch-job/{sessionId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createBatchJob(@PathParam("sessionId") String sessionId, String entity,
			@Context HttpServletRequest httpRequest) {
		ResponseHandler jrh = new ResponseHandler();
		JsonObject requestData = new JsonObject();
		String jobName = "";
		String note = "";
		Long numOfThread = null;
		String jobTypeLabelCode = null;
		String jobTemplateLabelCode = null;
		ReentrantLock lock = null;
		String LOCK_KEY = "";
		DbDataObject jobTypeDbo = null;
		DbDataObject jobTemplateDbo = null;
		try (SvReader svr = new SvReader(sessionId); SvWriter svw = new SvWriter(svr)) {
			try {
				requestData = new Gson().fromJson(entity, JsonObject.class);
			} catch (Exception e) {
				throw new SvException("error.bad_json", svr.getInstanceUser());
			}

			if (requestData.has("name")) {
				jobName = requestData.get("name").getAsString();
				LOCK_KEY += jobName;
			}
			if (requestData.has("jobTypeLabelCode")) {
				jobTypeLabelCode = requestData.get("jobTypeLabelCode").getAsString();
			}
			if (requestData.has("note")) {
				note = requestData.get("note").getAsString();
				LOCK_KEY += ("-" + note);
			}
			if (requestData.has("numOfThread")) {
				numOfThread = requestData.get("numOfThread").getAsLong();
			}
			if (requestData.has("jobTemplateLabelCode")) {
				jobTemplateLabelCode = requestData.get("jobTemplateLabelCode").getAsString();
			}

			jobTypeDbo = svr.getObjectByUnqConfId(jobTypeLabelCode, CC.SVAROG_BATCH_JOB_TYPE);
			if (jobTypeDbo == null) {
				throw new SvException("batch.error.job_type_not_found", svr.getInstanceUser());
			}

			jobTemplateDbo = svr.getObjectByUnqConfId(jobTemplateLabelCode, CC.SVAROG_BATCH_JOB_TEMPLATE);
			if (jobTemplateDbo == null) {
				throw new SvException("batch.error.job_template_not_found", svr.getInstanceUser());
			}

			lock = SvLock.getLock(LOCK_KEY, false, 0);
			if (lock != null) {
				DbDataObject dbBatchJob = SvarogBatchManager.createJob(jobName, jobTypeDbo.getObjectId(), note,
						numOfThread, jobTemplateDbo.getObjectId());

				svw.saveObject(dbBatchJob);
				JsonObject resultObj = new JsonObject();
				resultObj.addProperty("jobId", dbBatchJob.getObjectId());

				jrh.create(MessageType.SUCCESS, I18n.getText("batch.success.create_job"), null, resultObj);
			} else {
				jrh.create(MessageType.WARNING, I18n.getText("batch.warning.job_already_created"), null,
						new JsonObject());
			}
			return Response.ok().entity(jrh.getAll().toString()).build();
		} catch (Exception e) {
			return PerunUtil.handleException(e, jrh, "Failed to create batch job");
		} finally {
			if (lock != null) {
				SvLock.releaseLock(LOCK_KEY, lock);
			}
		}
	}
}
