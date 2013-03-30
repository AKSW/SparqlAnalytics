package org.aksw.sparql_analytics.web;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

@Path("/api")
public class Api {

	private DataSource dataSource;
	
	public Api(@Context ServletContext context) {
		this.dataSource = (DataSource)context.getAttribute("dataSource");
		if(dataSource == null) {
			throw new NullPointerException("Data source was not set");
		}
	}

	@GET
	@Path("/min")
	@Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
	public String createMinuteSummary(@Context HttpServletRequest req)
			throws Exception {

		Connection conn = dataSource.getConnection();
		
		
		List<String> labels = new ArrayList<String>();
		List<Integer> data = new ArrayList<Integer>();
		
		try {
			//String startTime = new GregorianCalendar().getTi
			Calendar cal = new GregorianCalendar();
			//cal.add(Calendar.HOUR, -24);
			cal.add(Calendar.HOUR, -1);
			Timestamp startTime = new Timestamp(cal.getTime().getTime());
			
//			String sql
//				= "SELECT TO_CHAR(\"ts_start\", 'DAY:HH24') AS \"label\", MIN(\"ts_start\") As min, COUNT(*) AS \"count\"\n"
//				+ "FROM \"request\"\n"
//				+ "GROUP BY TO_CHAR(\"ts_start\", 'DAY:HH24')\n"
//				+ "HAVING MIN(\"ts_start\") > '" + startTime + "'\n"
//				+ "ORDER BY MIN(\"ts_start\") ASC"
//				;

			String sql
			= "SELECT TO_CHAR(\"ts_start\", 'DAY:HH24:MI') AS \"label\", MIN(\"ts_start\") As min, COUNT(*) AS \"count\"\n"
			+ "FROM \"request\"\n"
			+ "GROUP BY TO_CHAR(\"ts_start\", 'DAY:HH24:MI')\n"
			+ "HAVING MIN(\"ts_start\") > '" + startTime + "'\n"
			+ "ORDER BY MIN(\"ts_start\") ASC"
			;

		
			System.out.println(sql);
			
			ResultSet rs = conn.createStatement().executeQuery(sql);
			try {
				while(rs.next()) {
					String label = rs.getString("label");
					Integer count = rs.getInt("count");
					
					labels.add(label);
					data.add(count);
				}
			}
			finally {
				rs.close();
			}
			
		}
		finally {
			conn.close();
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("labels", labels);
		map.put("data", data);
		
		Gson gson = new Gson();
		String result = gson.toJson(map);
		
		return result;
	}

}
