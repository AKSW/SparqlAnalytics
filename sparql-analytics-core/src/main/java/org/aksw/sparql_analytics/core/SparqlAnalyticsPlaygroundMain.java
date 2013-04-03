package org.aksw.sparql_analytics.core;

import java.sql.SQLException;
import java.util.List;

import org.postgresql.ds.PGSimpleDataSource;

public class SparqlAnalyticsPlaygroundMain {
	public static void main(String[] args) throws SQLException, InterruptedException {
		
		PGSimpleDataSource dataSource = new PGSimpleDataSource();

		dataSource.setDatabaseName("sparql_analytics");
		dataSource.setServerName("localhost");
		dataSource.setUser("postgres");
		dataSource.setPassword("postgres");

		//Backend backend = new Backend(dataSource);
		ApiBean api = new ApiBean(dataSource);
		
		UsageCounterIncremental usageCounter = new UsageCounterIncremental(api);

		System.out.println("startTimestamp: " + usageCounter.getStartTimestamp());
		System.out.println("timeWindow: " + usageCounter.getTimeWindow());
		
		for(int i = 0; i < 10; ++i) {
			List<RequestCount> usageCounts = usageCounter.next();
			Thread.sleep(2000);
			System.out.println(usageCounts);
		}
		
	}
}
