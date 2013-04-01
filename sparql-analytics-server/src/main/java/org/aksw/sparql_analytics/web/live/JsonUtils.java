package org.aksw.sparql_analytics.web.live;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonUtils {

	private static final ObjectMapper jsonMapper = new ObjectMapper();

	public static String toJson(Object object) {
		try {
			return jsonMapper.writeValueAsString(object);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		try {
			return jsonMapper.readValue(json, clazz);
		} catch (Exception e) {
			return null;
		}
	}

}
