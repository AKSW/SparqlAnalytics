package org.aksw.sparql_analytics.temp;


import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class Test {
	public static void main(String[] args)
		throws Exception
	{
		// Create a query execution over DBpedia
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
		
		// Add delay in order to be nice to the remote server (delay in milli seconds)
		qef = new QueryExecutionFactoryDelay(qef, 7000);

		// Set up a cache
		// Cache entries are valid for 1 day
		long timeToLive = 24l * 60l * 60l * 1000l; 
		
		// This creates a 'cache' folder, with a database file named 'sparql.db'
		// Technical note: the cacheBackend's purpose is to only deal with streams,
		// whereas the frontend interfaces with higher level classes - i.e. ResultSet and Model
		CacheCoreEx cacheBackend = CacheCoreH2.create("sparql", timeToLive, true);
		CacheEx cacheFrontend = new CacheExImpl(cacheBackend);		
		qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);

		// Add pagination
		qef = new QueryExecutionFactoryPaginated(qef, 900);
		
		QueryExecution qe = qef.createQueryExecution("Select ?s { ?s a <http://dbpedia.org/ontology/City> } Limit 5000");
		
		ResultSet rs = qe.execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
	}
}