package org.aksw.sparql_analytics.core;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.jdbc.ColumnsReference;
import org.aksw.commons.util.jdbc.Schema;
import org.aksw.commons.util.strings.StringUtils;

import com.hp.hpl.jena.query.Query;

class AnalyticsResult {
	//private Calendar timestamp;
	//private String queryString;
	
	//private MultiMap<>urlQueryString
	
}

public class Backend {
	
	private DataSource dataSource;
	private Schema schema;
	
	public Backend(DataSource dataSource) throws SQLException {
		this.dataSource = dataSource;

		Connection conn = dataSource.getConnection();
		try {
			this.schema = Schema.create(conn);
		}
		finally {
			conn.close();
		}

	}
	
	public void close() {
		
	}
	
	public void write(Map<String, Object> data) throws SQLException, FileNotFoundException {

		Query query = (Query)data.get("query");
		String queryString = (String)data.get("queryString");
		Calendar requestTime = (Calendar)data.get("requestTime");
		String ipAddr = (String)data.get("ipAddr");

		String serviceUri = (String)data.get("serviceUri");
		
		Map<String, Integer> nodeToUsage = (Map<String, Integer>)data.get("nodeToUsage");
		

		String requestId = StringUtils.md5Hash(System.nanoTime() + ":" + query);

		Connection conn = dataSource.getConnection();
		ColumnsReference requestTable = new ColumnsReference("request", "id", "ts_start", "user_id", "service_id", "query_id", "item_count");

		
		Inserter inserter = new Inserter(requestTable, schema);
		
		try {
			inserter.add(
				requestId,
				requestTime.getTime(),
				ipAddr,
				serviceUri,
				queryString,
				0
			);
			
			inserter.flush(conn);
		}
		finally {
			conn.close();
		}
		
//		ColumnsReference evaluationTable = new ColumnsReference("evaluations", "id", "linkset_id", "user_id", "creation_date", "pos_file_id", "neg_file_id");
//		Inserter inserter = new Inserter(evaluationTable, schema);
//
//		String evalId = evaluation.getId();
//		write(posFileId, evaluation.getPositiveRefsetFile());
//		write(negFileId, evaluation.getNegativeRefsetFile());
//		
//		
//		inserter.add(
//			evalId,
//			linksetId,
//			userId,
//			new Timestamp(evaluation.getTimestamp().getTime().getTime()),
//			posFileId,
//			negFileId
//		);
//		
//		inserter.flush(conn);
	}

}
