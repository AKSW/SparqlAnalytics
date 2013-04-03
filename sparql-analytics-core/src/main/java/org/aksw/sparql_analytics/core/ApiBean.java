package org.aksw.sparql_analytics.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;


public class ApiBean {

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

		System.out.println(sql);
		Connection conn = dataSource.getConnection();
		try {
	
			ResultSet rs = conn.createStatement().executeQuery(sql);
			try {
				while(rs.next()) {
					//String label = rs.getString("label");
					Timestamp ts = rs.getTimestamp("min");
					Integer count = rs.getInt("count");
		
					result.add(new RequestCount(new Date(ts.getTime()), count));
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
