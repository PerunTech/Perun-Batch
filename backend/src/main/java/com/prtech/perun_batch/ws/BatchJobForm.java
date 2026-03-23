package com.prtech.perun_batch.ws;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.prtech.perun.PerunUtil;
import com.prtech.svarog.I18n;
import com.prtech.svarog.SvException;
import com.prtech.svarog.SvReader;
import com.prtech.svarog.SvWriter;
import com.prtech.svarog.svCONST;
import com.prtech.svarog_common.DbDataObject;
import com.prtech.svarog_common.ResponseHandler;
import com.prtech.svarog_common.ResponseHandler.MessageType;

@Path("/batch")
public class BatchJobForm {
	static final Gson gson = new Gson();

	/**
	 * Returns the JSON Schema for the batch job search form.
	 *
	 * @param sessionId   the session identifier passed via the {@code sessionId}
	 *                    request header, used to authenticate and resolve the
	 *                    current user's locale
	 * @param httpRequest the underlying HTTP servlet request
	 * @return {@code 200 OK} with a JSON Schema object body, or an error response
	 *         if the session is invalid or an unexpected exception occurs
	 */
	@Path("/search-form")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchForm(@HeaderParam("sessionId") String sessionId, @Context HttpServletRequest httpRequest) {
		try (SvReader svr = new SvReader(sessionId); SvWriter svw = new SvWriter(svr)) {
			String localeId = svr.getUserLocaleId(svr.getInstanceUser());
			JsonObject jsonSchema = new JsonObject();
			jsonSchema.addProperty(CC.TYPE_LC, "object");
			JsonObject properties = new JsonObject();

			JsonObject jobType = new JsonObject();
			jobType.addProperty(CC.TYPE_LC, "string");
			jobType.addProperty(CC.TITLE_LC, I18n.getText(localeId, "perun.batch.job_type"));
			List<String> jobTypes = svr.getObjectsByTypeId(svCONST.OBJECT_TYPE_BATCH_JOB_TYPE, null, 0, 0).getItems()
					.stream().map(dbo -> dbo.getVal("LABEL_CODE")).filter(Objects::nonNull).map(Object::toString)
					.collect(Collectors.toList());
			List<String> jobTypesTranslated = jobTypes.stream().map(e -> I18n.getText(localeId, e))
					.collect(Collectors.toList());
			jobType.add(CC.ENUM_LC, gson.toJsonTree(jobTypes));
			jobType.add(CC.ENUM_NAMES, gson.toJsonTree(jobTypesTranslated));
			properties.add("jobTypeLabelCode", jobType);

			JsonObject jobTemplate = new JsonObject();
			jobTemplate.addProperty(CC.TYPE_LC, "string");
			jobTemplate.addProperty(CC.TITLE_LC, "perun.batch.job_template");
			List<String> jobTemplates = svr.getObjectsByTypeId(svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE, null, 0, 0)
					.getItems().stream().map(dbo -> dbo.getVal("LABEL_CODE")).filter(Objects::nonNull)
					.map(Object::toString).collect(Collectors.toList());
			List<String> jobTemplatesTranslated = jobTemplates.stream().map(e -> I18n.getText(localeId, e))
					.collect(Collectors.toList());
			jobTemplate.add(CC.ENUM_LC, gson.toJsonTree(jobTemplates));
			jobTemplate.add(CC.ENUM_NAMES, gson.toJsonTree(jobTemplatesTranslated));
			properties.add("jobTemplateLabelCode", jobTemplate);

			jsonSchema.add("properties", properties);
			return Response.ok(jsonSchema.toString()).build();
		} catch (Exception e) {
			return PerunUtil.handleException(e, null, "perun.error.generalError");
		}
	}

	/**
	 * Returns the UI Schema for the batch job search form, for use with
	 * {@code react-jsonschema-form}.
	 *
	 * @param sessionId   the session identifier passed via the {@code sessionId}
	 *                    request header, used to authenticate the current user
	 * @param httpRequest the underlying HTTP servlet request
	 * @return {@code 200 OK} with a flat JSON UI Schema object body, or an error
	 *         response if the session is invalid or an unexpected exception occurs
	 */
	@Path("/search-form-ui-schema")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchFormUiSchema(@HeaderParam("sessionId") String sessionId,
			@Context HttpServletRequest httpRequest) {
		try (SvReader svr = new SvReader(sessionId)) {
			JsonObject uiSchema = new JsonObject();

			JsonObject jobTypeUi = new JsonObject();
			jobTypeUi.addProperty("ui:widget", "DependencyDropdown");
			jobTypeUi.addProperty("order", 0);
			uiSchema.add("jobTypeLabelCode", jobTypeUi);

			JsonObject jobTemplateUi = new JsonObject();
			jobTemplateUi.addProperty("ui:widget", "hidden");
			jobTemplateUi.addProperty("dependentOnField", "jobTypeLabelCode");
			jobTemplateUi.addProperty("order", 1);
			uiSchema.add("jobTemplateLabelCode", jobTemplateUi);

			return Response.ok(uiSchema.toString()).build();
		} catch (Exception e) {
			return PerunUtil.handleException(e, null, "perun.error.generalError");
		}
	}

	/**
	 * Returns the available batch job templates for a given batch job type. Looks
	 * up the batch job object matching the provided {@code jobTypeLabelCode}, then
	 * retrieves all batch job templates whose parent is that batch job object.
	 *
	 * @param sessionId        the session identifier passed via the
	 *                         {@code sessionId} request header, used to
	 *                         authenticate and resolve the current user's locale
	 * @param httpRequest      the underlying HTTP servlet request
	 * @param jobTypeLabelCode the label code of the selected batch job type, passed
	 *                         as a path parameter
	 * @return {@code 200 OK} with a JSON object containing the job templates, or an
	 *         error response if the session is invalid, the job type is not found,
	 *         or an unexpected exception occurs
	 */
	@Path("/job-templates/{jobTypeLabelCode}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJobTemplatesByType(@HeaderParam("sessionId") String sessionId,
			@PathParam("jobTypeLabelCode") String jobTypeLabelCode, @Context HttpServletRequest httpRequest) {
		ResponseHandler jrh = new ResponseHandler();
		try (SvReader svr = new SvReader(sessionId)) {
			String localeId = svr.getUserLocaleId(svr.getInstanceUser());

			DbDataObject jobTypeObject = svr.getObjectsByTypeId(svCONST.OBJECT_TYPE_BATCH_JOB_TYPE, null, 0, 0)
					.getItems().stream().filter(dbo -> jobTypeLabelCode.equals(dbo.getVal("LABEL_CODE"))).findFirst()
					.orElse(null);

			if (jobTypeObject == null) {
				throw new SvException("batch.error.job_type_not_found", svr.getInstanceUser());
			}

			long jobTypeObjectId = jobTypeObject.getObjectId();

			List<JsonObject> jobTemplates = svr
					.getObjectsByParentId(jobTypeObjectId, svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE, null, 0, 0)
					.getItems().stream().map(dbo -> {
						JsonObject obj = new JsonObject();
						obj.addProperty("CODE_VALUE", dbo.getAsString(CC.LABEL_CODE));
						obj.addProperty("LBL_TRANSL", I18n.getText(localeId, dbo.getAsString(CC.LABEL_CODE)));
						return obj;
					}).collect(Collectors.toList());

			JsonObject result = new JsonObject();
			result.add(CC.ITEMS_LC, gson.toJsonTree(jobTemplates));
			jrh.create(MessageType.SUCCESS, "data.read", null, result);

			return Response.ok(jrh.getAll().toString()).build();
		} catch (Exception e) {
			return PerunUtil.handleException(e, null, "perun.error.generalError");
		}
	}

	@Path("/create-batch-job-form")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response createBatchJobForm(@HeaderParam("sessionId") String sessionId,
			@Context HttpServletRequest httpRequest) {
		try (SvReader svr = new SvReader(sessionId)) {
			String localeId = svr.getUserLocaleId(svr.getInstanceUser());
			JsonObject jsonSchema = new JsonObject();
			jsonSchema.addProperty(CC.TYPE_LC, "object");

			JsonObject properties = new JsonObject();
			JsonArray required = new JsonArray();

			JsonObject name = new JsonObject();
			name.addProperty(CC.TYPE_LC, "string");
			name.addProperty(CC.TITLE_LC, I18n.getText(localeId, "perun.batch.job_name"));
			properties.add("name", name);
			required.add("name");

			JsonObject jobType = new JsonObject();
			jobType.addProperty(CC.TYPE_LC, "string");
			jobType.addProperty(CC.TITLE_LC, I18n.getText(localeId, "perun.batch.job_type"));
			List<String> jobTypes = svr.getObjectsByTypeId(svCONST.OBJECT_TYPE_BATCH_JOB_TYPE, null, 0, 0).getItems()
					.stream().map(dbo -> dbo.getAsString(CC.LABEL_CODE)).filter(Objects::nonNull)
					.collect(Collectors.toList());
			List<String> jobTypesTranslated = jobTypes.stream().map(e -> I18n.getText(localeId, e))
					.collect(Collectors.toList());
			jobType.add(CC.ENUM_LC, gson.toJsonTree(jobTypes));
			jobType.add(CC.ENUM_NAMES, gson.toJsonTree(jobTypesTranslated));
			properties.add("jobTypeLabelCode", jobType);
			required.add("jobTypeLabelCode");

			JsonObject jobTemplate = new JsonObject();
			jobTemplate.addProperty(CC.TYPE_LC, "string");
			jobTemplate.addProperty(CC.TITLE_LC, I18n.getText(localeId, "perun.batch.job_template"));
			List<String> jobTemplates = svr.getObjectsByTypeId(svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE, null, 0, 0)
					.getItems().stream().map(dbo -> dbo.getAsString(CC.LABEL_CODE)).filter(Objects::nonNull)
					.collect(Collectors.toList());
			List<String> jobTemplatesTranslated = jobTemplates.stream().map(e -> I18n.getText(localeId, e))
					.collect(Collectors.toList());
			jobTemplate.add(CC.ENUM_LC, gson.toJsonTree(jobTemplates));
			jobTemplate.add(CC.ENUM_NAMES, gson.toJsonTree(jobTemplatesTranslated));
			properties.add("jobTemplateLabelCode", jobTemplate);
			required.add("jobTemplateLabelCode");

			JsonObject note = new JsonObject();
			note.addProperty(CC.TYPE_LC, "string");
			note.addProperty(CC.TITLE_LC, I18n.getText(localeId, "perun.batch.note"));
			properties.add("note", note);

			JsonObject numOfThread = new JsonObject();
			numOfThread.addProperty(CC.TYPE_LC, "integer");
			numOfThread.addProperty(CC.TITLE_LC, I18n.getText(localeId, "perun.batch.num_of_thread"));
			numOfThread.addProperty("minimum", 1);
			properties.add("numOfThread", numOfThread);

			jsonSchema.add("properties", properties);
			jsonSchema.add("required", required);

			return Response.ok(jsonSchema.toString()).build();
		} catch (Exception e) {
			return PerunUtil.handleException(e, null, "perun.error.generalError");
		}
	}

	@Path("/create-batch-job-form-ui-schema")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response createBatchJobFormUiSchema(@HeaderParam("sessionId") String sessionId,
			@Context HttpServletRequest httpRequest) {
		try (SvReader svr = new SvReader(sessionId)) {
			JsonObject uiSchema = new JsonObject();

			JsonObject nameUi = new JsonObject();
			uiSchema.add("name", nameUi);

			JsonObject jobTypeUi = new JsonObject();
			jobTypeUi.addProperty("ui:widget", "DependencyDropdown");
			jobTypeUi.addProperty("order", 0);
			uiSchema.add("jobTypeLabelCode", jobTypeUi);

			JsonObject jobTemplateUi = new JsonObject();
			jobTemplateUi.addProperty("ui:widget", "hidden");
			jobTemplateUi.addProperty("dependentOnField", "jobTypeLabelCode");
			jobTemplateUi.addProperty("order", 1);
			uiSchema.add("jobTemplateLabelCode", jobTemplateUi);

			JsonObject numOfThreadUi = new JsonObject();
			uiSchema.add("numOfThread", numOfThreadUi);

			JsonObject noteUi = new JsonObject();
			noteUi.addProperty("ui:widget", "textarea");
			uiSchema.add("note", noteUi);

			return Response.ok(uiSchema.toString()).build();
		} catch (Exception e) {
			return PerunUtil.handleException(e, null, "perun.error.generalError");
		}
	}

	@Path("/batch-job-params-form/{jobObjId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response batchJobParamsForm(@HeaderParam("sessionId") String sessionId,
			@PathParam("jobObjId") Long jobObjectId, @Context HttpServletRequest httpRequest) {
		try (SvReader svr = new SvReader(sessionId)) {
			String localeId = svr.getUserLocaleId(svr.getInstanceUser());
			JsonObject jsonSchema = new JsonObject();

			DbDataObject jobDbo = svr.getObjectById(jobObjectId, svCONST.OBJECT_TYPE_BATCH_JOB, null);
			if (jobDbo == null) {
				throw new SvException("batch.error.job_not_found", svr.getInstanceUser());
			}

			Long templateObjId = (Long) jobDbo.getVal(CC.TEMPLATE_ID);
			DbDataObject jobTemplateDbo = svr.getObjectById(templateObjId, svCONST.OBJECT_TYPE_BATCH_JOB_TEMPLATE,
					null);
			if (jobTemplateDbo == null) {
				throw new SvException("batch.error.job_template_not_found", svr.getInstanceUser());
			}

			if (jobTemplateDbo.getVal(CC.PARAMS) == null) {
				return Response.ok(jsonSchema.toString()).build();
			}

			JsonArray jobParams = gson.fromJson(jobTemplateDbo.getAsString(CC.PARAMS), JsonArray.class);

			jsonSchema.addProperty(CC.TYPE_LC, "object");
			JsonObject properties = new JsonObject();
			JsonArray required = new JsonArray();

			for (JsonElement paramEl : jobParams) {
				JsonObject param = paramEl.getAsJsonObject();

				String paramId = param.get("id").getAsString();
				String paramName = param.get("name").getAsString();
				String paramType = param.get("type").getAsString();
				boolean mandatory = Boolean.parseBoolean(param.get("mandatory").getAsString());
				JsonElement defaultValue = param.get("value");

				JsonObject fieldSchema = new JsonObject();
				fieldSchema.addProperty(CC.TITLE_LC, I18n.getText(localeId, paramName));

				switch (paramType) {
				case "NUMERIC":
					fieldSchema.addProperty(CC.TYPE_LC, "number");
					if (defaultValue != null && !defaultValue.isJsonNull()) {
						fieldSchema.addProperty("default", defaultValue.getAsNumber());
					}
					break;
				case "NVARCHAR":
				default:
					fieldSchema.addProperty(CC.TYPE_LC, "string");
					if (defaultValue != null && !defaultValue.isJsonNull()) {
						fieldSchema.addProperty("default", defaultValue.getAsString());
					}
					break;
				}

				properties.add(paramId, fieldSchema);

				if (mandatory) {
					required.add(paramId);
				}
			}

			jsonSchema.add("properties", properties);
			if (required.size() > 0) {
				jsonSchema.add("required", required);
			}

			return Response.ok(jsonSchema.toString()).build();
		} catch (Exception e) {
			return PerunUtil.handleException(e, null, "perun.error.generalError");
		}
	}
}
