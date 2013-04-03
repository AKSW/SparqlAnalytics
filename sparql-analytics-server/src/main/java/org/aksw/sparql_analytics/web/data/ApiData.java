package org.aksw.sparql_analytics.web.data;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.aksw.sparql_analytics.core.ApiBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.core.InjectParam;

@Component
@Path("/data")
public class ApiData
{		
	public ApiData() {
	}

	@Resource(name="sparqlAnalytics.api")
	private ApiBean apiBean;

	
	@GET
	@Path("/min")
	@Produces({MediaType.APPLICATION_JSON})
	public String createMinuteSummary(@Context HttpServletRequest req)
			throws Exception {
	
		if(this.apiBean == null) {
			throw new RuntimeException("ApiBean not set.");
		}
		
		return "{}";
	}

	

//	@GET
//	@Path("/min")
//	@Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
//	public String createMinuteSummary(@Context HttpServletRequest req)
//			throws Exception {
//
//		if(dataSource == null) {
//			throw new RuntimeException("DataSource not set.");
//		}
//		
//		Connection conn = dataSource.getConnection();
//		
//		
//		List<String> labels = new ArrayList<String>();
//		List<Integer> data = new ArrayList<Integer>();
//		
//		try {
//			//String startTime = new GregorianCalendar().getTi
//			Calendar cal = new GregorianCalendar();
//			//cal.add(Calendar.HOUR, -24);
//			cal.add(Calendar.HOUR, -1);
//			Timestamp startTime = new Timestamp(cal.getTime().getTime());
//			
////			String sql
////				= "SELECT TO_CHAR(\"ts_start\", 'DAY:HH24') AS \"label\", MIN(\"ts_start\") As min, COUNT(*) AS \"count\"\n"
////				+ "FROM \"request\"\n"
////				+ "GROUP BY TO_CHAR(\"ts_start\", 'DAY:HH24')\n"
////				+ "HAVING MIN(\"ts_start\") > '" + startTime + "'\n"
////				+ "ORDER BY MIN(\"ts_start\") ASC"
////				;
//
////			String sql
////			= "SELECT TO_CHAR(\"ts_start\", 'DAY:HH24:MI') AS \"label\", MIN(\"ts_start\") As min, COUNT(*) AS \"count\"\n"
////			+ "FROM \"request\"\n"
////			+ "GROUP BY TO_CHAR(\"ts_start\", 'DAY:HH24:MI')\n"
////			+ "HAVING MIN(\"ts_start\") > '" + startTime + "'\n"
////			+ "ORDER BY MIN(\"ts_start\") ASC"
////			;
//
//
//		
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("labels", labels);
//		map.put("data", data);
//		
//		Gson gson = new Gson();
//		String result = gson.toJson(map);
//		
//		return result;
//	}

}
