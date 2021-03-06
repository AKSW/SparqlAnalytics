package org.aksw.sparql_analytics.core;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

public class QueryExecutionAnalytics
	extends QueryExecutionDecorator
{
	private Runnable onCloseHandler = null;

	public QueryExecutionAnalytics(QueryExecution decoratee) {
		super(decoratee);
	}

	/**
	 *  Set a handler when the result set closes
	 *  
	 *  Must be done before calling any of the exec... methods
	 *  
	 * @param onCloseHandler
	 */
	public void setOnCloseHandler(Runnable onCloseHandler) {
		this.onCloseHandler = onCloseHandler;
	}
	
	/**
	 * A query execution can only be used once, so making the result set a property
	 * is probably sound, although it still feels like a hack.
	 * Some proper event pattern should be used here. 
	 */
	private ResultSetAnalyzing wrapper;
			
	@Override
	public ResultSetAnalyzing execSelect() {
		ResultSet rs = super.execSelect();

		wrapper = new ResultSetAnalyzing(rs, onCloseHandler);
		
		return wrapper;
	}
	
	public ResultSetAnalyzing getResultSet() {
		return wrapper;
	}
}