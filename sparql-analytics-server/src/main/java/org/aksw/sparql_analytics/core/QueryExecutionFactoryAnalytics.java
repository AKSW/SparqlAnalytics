package org.aksw.sparql_analytics.core;

import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryWrapper;

import com.hp.hpl.jena.query.QueryExecution;


public class QueryExecutionFactoryAnalytics
	extends QueryExecutionFactoryWrapper
{
	private Map<String, Object> metadata;

	
	
	public QueryExecutionFactoryAnalytics(QueryExecutionFactory decoratee, Map<String, Object> metadata) {
		super(decoratee);
		
		this.metadata = metadata;
	}

	@Override
	protected QueryExecution wrap(QueryExecution qe) {
		QueryExecution result = new QueryExecutionAnalytics(qe);
		return result;
	}
}