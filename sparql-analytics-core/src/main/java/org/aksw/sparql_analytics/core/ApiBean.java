package org.aksw.sparql_analytics.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ApiBean {
	private static final Logger logger = LoggerFactory.getLogger(ApiBean.class);
	
	private DataSource dataSource;
	
	public ApiBean(DataSource dataSource) throws SQLException {
		this.dataSource = dataSource;
	}

	
	public List<RequestCount> createSummaryHistogram(long startTime)
			throws Exception
	{
		Timestamp timestamp = new Timestamp(startTime);
		
		
		List<RequestCount> result = new ArrayList<RequestCount>();

		String sql
		= "SELECT\n"
		+ "    TO_CHAR(\"ts_start\", 'DAY:HH24:MI') AS \"label\",\n"
		+ "    MIN(\"ts_start\") As min,\n"
		+ "    COUNT(*) AS \"count\"\n"
		+ "FROM\n"
		+ "    (SELECT * FROM request WHERE ts_start > '" + timestamp + "') As a\n"
		+ "GROUP BY TO_CHAR(\"ts_start\", 'DAY:HH24:MI')\n"
		+ "ORDER BY MIN(\"ts_start\") ASC"
		;

		logger.debug("Timestamp for database lookup: " + timestamp);
		System.out.println(sql);
		Connection conn = dataSource.getConnection();
		try {
	
			ResultSet rs = conn.createStatement().executeQuery(sql);
			try {
				while(rs.next()) {
					//String label = rs.getString("label");
					Timestamp ts = rs.getTimestamp("min");
					Integer count = rs.getInt("count");
		
					//Long t = ts.getTime();
					DateTime dt = new DateTime(ts).withSecondOfMinute(0).withMillisOfSecond(0);
					
					result.add(new RequestCount(dt.getMillis(), count));
				}
			}
			finally {
				rs.close();
			}
		}
		finally {
			conn.close();
		}
	
		return result;
	}
}
