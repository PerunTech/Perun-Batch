package com.prtech.perun_batch.ws;

import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.prtech.svarog_common.DbDataArray;

final class HelperMethods {
	private HelperMethods() {
	}

	public static JsonArray prefixKeys(String prefix, JsonArray arr) {
		JsonArray resultArray = new JsonArray();
		arr.forEach(elem -> {
			if (elem.isJsonObject()) {
				JsonObject obj = new JsonObject();
				elem.getAsJsonObject().entrySet().stream().forEach(e -> obj.add(prefix + e.getKey(), e.getValue()));
				resultArray.add(obj);
			}
		});
		return resultArray;
	}

	public static JsonArray prepareDataArray(DbDataArray dataArray) {
		JsonArray result = new JsonArray();
		dataArray.getItems().stream().forEach(dbo -> {
			result.add(HelperMethods.toUpperCaseKeys(dbo.toSimpleJson()));
		});
		return result;
	}

	public static JsonObject toUpperCaseKeys(JsonObject original) {
		JsonObject result = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : original.entrySet()) {
			result.add(entry.getKey().toUpperCase(), entry.getValue());
		}
		return result;
	}
}
