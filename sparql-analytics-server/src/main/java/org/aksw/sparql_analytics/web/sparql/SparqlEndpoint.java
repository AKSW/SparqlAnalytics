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
import org.springframework.stereotype.Component;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;


@Component
@Path("/sparql")
public class SparqlEndpoint
	extends SparqlEndpointBase
{
//	/*
//	 * Workaround for not getting injection working - Hopefully we get rid of that soon
//	 */
//	private ApplicationContext ctx = null;
//
//	@Override
//	public void setApplicationContext(ApplicationContext ctx)
//			throws BeansException {
//		this.ctx = ctx;
//		
//		this.backend = (Backend)ctx.getBean("sparqlAnalytics.backend");
//		this.defaultServiceUri = (String)ctx.getBean("sparqlAnalytics.defaultServiceUri");
//		this.allowOverrideServiceUri = (Boolean)ctx.getBean("sparqlAnalytics.allowOverrideDefaultServiceUri");
//	}

	
	@Resource(name="sparqlAnalytics.backend")
	private Backend backend = null;
	
	@Resource(name="sparqlAnalytics.defaultServiceUri")
	private String defaultServiceUri = null;
	
	@Resource(name="sparqlAnalytics.allowOverrideDefaultServiceUri")
	private Boolean allowOverrideServiceUri = false;
	
	public SparqlEndpoint() {

	}
	
//	public SparqlEndpoint(@Context ServletContext context) {
//		//super((QueryExecutionFactory)context.getAttribute("queryExecutionFactory"));
//	
//		this.backend = (Backend)context.getAttribute("backend");
//		if(backend == null) {
//			throw new NullPointerException("Backend was not set");
//		}
//		
//		this.defaultServiceUri = (String)context.getAttribute("defaultServiceUri");
//		this.allowOverrideServiceUri = (Boolean)context.getAttribute("allowOverrideServiceUri");
//	}
	
	public void setBackend(Backend backend) {
		this.backend = backend;
	}
	
	public Backend getBackend() {
		return backend;
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
	public QueryExecution createQueryExecution(final Query query, @Context HttpServletRequest req) {
		
		
		Multimap<String, String> qs = UriUtils.parseQueryString(req.getQueryString());
		
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
		
		QueryExecution qe = qef.createQueryExecution(query);
		
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
