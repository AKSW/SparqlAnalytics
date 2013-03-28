package org.aksw.sparql_analytics.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.aksw.commons.sparql.api.http.QueryExecutionFactoryHttp;
import org.aksw.sparql_analytics.core.Backend;
import org.aksw.sparql_analytics.core.QueryExecutionAnalytics;
import org.aksw.sparql_analytics.core.ResultSetAnalyzing;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.Query;



@Path("/sparql")
public class SparqlEndpoint
	extends SparqlEndpointBase
{

	private Backend backend;
	private String defaultServiceUri;
	private boolean allowOverrideServiceUri = false;
	
	public SparqlEndpoint(@Context ServletContext context) {
		//super((QueryExecutionFactory)context.getAttribute("queryExecutionFactory"));
	
		this.backend = (Backend)context.getAttribute("backend");
		if(backend == null) {
			throw new NullPointerException("Backend was not set");
		}
		
		this.defaultServiceUri = (String)context.getAttribute("defaultServiceUri");
		this.allowOverrideServiceUri = (Boolean)context.getAttribute("allowOverrideServiceUri");
	}

	
	/**
	 * TODO Add to utils
	 * 
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static Multimap<String, String> parseQueryString(String queryString) {
		try {
			return parseQueryStringEx(queryString);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Multimap<String, String> parseQueryStringEx(String queryString)
			throws UnsupportedEncodingException
	{
        Multimap<String, String> result = ArrayListMultimap.create();
	    
        for (String param : queryString.split("&")) {
	        String pair[] = param.split("=");
	        String key = URLDecoder.decode(pair[0], "UTF-8");
	        String value = "";
	        if (pair.length > 1) {
	            value = URLDecoder.decode(pair[1], "UTF-8");
	        }
	        result.put(new String(key), new String(value));
	    }
	    
        return result;	
	}
	
	/*
	public static Multimap<String, String> parseQueryString(String url) {
        try {
            Multimap<String, String> ret = ArrayListMultimap.create();
            URLEncodedUtils.
            for (NameValuePair param : URLEncodedUtils.parse(new URI(url), "UTF-8")) {
                ret.put(param.getName(), param.getValue());
            }
            return ret;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    */
	
	public void onQueryExecutionDone(Map<String, Object> data)
	{
		try {
			backend.write(data);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	@Override
	public QueryExecutionStreaming createQueryExecution(final Query query, @Context HttpServletRequest req) {
		
		
		Multimap<String, String> qs = parseQueryString(req.getQueryString());
		
		// Collect all interesting Metadata and write it into a GSON object
		final Map<String, Object> data = new HashMap<String, Object>();

		String queryString = qs.get("query").iterator().next();
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
		
		QueryExecutionStreaming qe = qef.createQueryExecution(query);
		
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
		
		return result;
	}

}
