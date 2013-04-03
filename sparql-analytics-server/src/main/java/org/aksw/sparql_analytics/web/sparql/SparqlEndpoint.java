package org.aksw.sparql_analytics.web.sparql;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.utils.UriUtils;
import org.aksw.jena_sparql_api.web.SparqlEndpointBase;
import org.aksw.sparql_analytics.core.Backend;
import org.aksw.sparql_analytics.core.QueryExecutionAnalytics;
import org.aksw.sparql_analytics.core.ResultSetAnalyzing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;


@Component
@Path("/sparql")
public class SparqlEndpoint
	extends SparqlEndpointBase
{
	private static final Logger logger = LoggerFactory.getLogger(SparqlEndpoint.class);
	
	
	@Resource(name="sparqlAnalytics.backend")
	private Backend backend = null;
	
	@Resource(name="sparqlAnalytics.defaultServiceUri")
	private String defaultServiceUri = null;
	
	@Resource(name="sparqlAnalytics.allowOverrideDefaultServiceUri")
	private Boolean allowOverrideServiceUri = false;
	
	@Resource(name="sparqlAnalytics.queryNotifier")
	private Object queryNotifier;
	

	public SparqlEndpoint() {
	}
	
	
	public void onQueryExecutionDone(Map<String, Object> data)
	{
		try {
			backend.write(data);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
		
	
	@Override
	public QueryExecution createQueryExecution(final Query query, @Context HttpServletRequest req) {
		
		logger.info("ServiceUri: " + defaultServiceUri);
		logger.info("Allow Override: " + allowOverrideServiceUri);
		
		// TODO I did not think about post requests that do not yield a query string... handle them properly
		
		Multimap<String, String> qs = UriUtils.parseQueryString(req.getQueryString());
		
		// Collect all interesting Metadata and write it into a GSON object
		final Map<String, Object> data = new HashMap<String, Object>();

		String queryString = "" + query; //qs.get("query").iterator().next();
		Collection<String> serviceUris = qs.get("serviceUri");
		String serviceUri;
		if(serviceUris == null || serviceUris.isEmpty()) {
			serviceUri = defaultServiceUri;
		} else {
			serviceUri = serviceUris.iterator().next();
			
			// If overriding is disabled, a given uri must match the default one
			if(!allowOverrideServiceUri && !defaultServiceUri.equals(serviceUri)) {
				serviceUri = null;
			}
		}
		
		if(serviceUri == null) {
			throw new RuntimeException("No SPARQL service URI is configured");
		}
		 
		
		System.out.println("Query: " + query);
		
		data.put("query", query);
		data.put("queryString", queryString);
		data.put("requestTime", new GregorianCalendar());
		data.put("ipAddr", req.getRemoteAddr());
		data.put("serviceUri", serviceUri);
		
		// TODO: Wrap with analytics stuff
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(serviceUri);
		
		//QueryExecutionFactoryAnalytics analytics = new QueryExecutionFactoryAnalytics(tmp, metadata);
		
		QueryExecution qe = qef.createQueryExecution(query);
		
		
		//QueryExecutionEvent qee = new QueryExecutionEvent(qe);
		//qee.addListener(listener);
		
		
		
		final QueryExecutionAnalytics result = new QueryExecutionAnalytics(qe);

		
		result.setOnCloseHandler(new Runnable() {

			@Override
			public void run() {
				ResultSetAnalyzing rs = result.getResultSet(); 
				if(rs == null) {
					return;
				}
				
				data.put("nodeToUsage", rs.getNodeToUsage());
				onQueryExecutionDone(data);				
			}
		});

		synchronized(queryNotifier) {
			queryNotifier.notifyAll();
		}

		
		return result;
	}

}
