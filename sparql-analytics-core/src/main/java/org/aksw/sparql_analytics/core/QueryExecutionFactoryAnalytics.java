package org.aksw.sparql_analytics.core;

import java.util.Map;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryWrapper;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;


public class QueryExecutionFactoryAnalytics
	extends QueryExecutionFactoryWrapper
{
	private Map<String, Object> metadata;

	
	
	public QueryExecutionFactoryAnalytics(QueryExecutionFactory decoratee, Map<String, Object> metadata) {
		super(decoratee);
		
		this.metadata = metadata;
	}

	@Override
	protected QueryExecutionStreaming wrap(QueryExecutionStreaming qe) {
		QueryExecutionStreaming result = new QueryExecutionAnalytics(qe);
		return result;
	}
}