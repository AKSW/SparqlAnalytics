package org.aksw.sparql_analytics.model;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;


interface QueryExecutionListener {
	void onQueryStart(); // Common event for all query forms
	void onQueryEnd();
	
	void onSelectStart();
	void onSelectEnd(); // Actually this just means that a result set has been obtained.
	
	//void onStartResultSet();
	//void onIterateResultSet(Map<Var, Node> binding)
	//void onEndResultSet();
}


class QueryExecutionListenerAdapter
	implements QueryExecutionListener
{
	@Override
	public void onQueryStart() {
	}

	@Override
	public void onQueryEnd() {
	}

	@Override
	public void onSelectStart() {
	}

	@Override
	public void onSelectEnd() {
	}
}


public class QueryExecutionEvent
	extends QueryExecutionDecorator
{
	private List<QueryExecutionListener> listeners = new ArrayList<QueryExecutionListener>();
	
	public synchronized boolean addListener(QueryExecutionListener listener) {
		if(listeners.contains(listener)) {
			return false;
		}
		
		listeners.add(listener);
		return true;
	}
	
	public synchronized void removeListener(QueryExecutionListener listener) {
		throw new RuntimeException("Not implemented yet");
	}

	public void fireOnQueryStart() {
		for(QueryExecutionListener listener : listeners) {
			listener.onQueryStart();
		}
	}

	public void fireOnQueryEnd() {
		for(QueryExecutionListener listener : listeners) {
			listener.onQueryEnd();
		}
	}
	
	public void fireOnSelectStart() {
		for(QueryExecutionListener listener : listeners) {
			listener.onSelectStart();
		}
	}

	public void fireOnSelectEnd() {
		for(QueryExecutionListener listener : listeners) {
			listener.onSelectEnd();
		}
	}
	
	
	public QueryExecutionEvent(QueryExecution qe) {
		super(qe);
	}
	
	@Override
	public ResultSet execSelect() {
		
		fireOnSelectStart();
		
		ResultSet result;
		try {
			result = super.execSelect();
		} finally {
			fireOnSelectEnd();
		}
		
		return result;
	}
}
