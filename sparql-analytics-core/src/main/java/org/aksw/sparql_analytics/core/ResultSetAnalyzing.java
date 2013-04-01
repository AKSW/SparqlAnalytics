package org.aksw.sparql_analytics.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.core.ResultSetClosable;
import org.apache.jena.atlas.lib.MapUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


public class ResultSetAnalyzing
	extends ResultSetClosable
{

	private Map<Node, Integer> nodeToUsage = new HashMap<Node, Integer>();

	// TODO Maybe switch to Guava's EventBus
	// Or maybe even better: create a ResultSetListener class
	private Runnable onClose;
	//public final PropertyChangeSupport isDone = new PropertyChangeSupport(this);
	
	public ResultSetAnalyzing(ResultSet decoratee, Runnable onClose) {
		super(decoratee);
		
		this.onClose = onClose;
	}


	@Override
	public QuerySolution next() {
		QuerySolution result = super.next();
		
		Map<Var, Node> map = createMapFromQuerySolution(result);
		analyze(map, nodeToUsage);
		
		return result;
	}

	@Override
	public QuerySolution nextSolution() {
		QuerySolution result = super.nextSolution();		

		Map<Var, Node> map = createMapFromQuerySolution(result);
		analyze(map, nodeToUsage);
		
		return result;
	}
	
	@Override
	public Binding nextBinding() {
		Binding result = super.nextBinding();

		Map<Var, Node> map = createMapFromBinding(result);
		analyze(map, nodeToUsage);

		return result;
	}
	
	
	public static Map<Var, Node> createMapFromBinding(Binding binding) {
		Map<Var, Node> result = new HashMap<Var, Node>();

		Iterator<Var> it = binding.vars();
		
		while(it.hasNext()) {
			Var var = it.next();
			
			Node node = binding.get(var);
			result.put(var, node);
		}

		return result;
	}
	
	public static Map<Var, Node> createMapFromQuerySolution(QuerySolution qs) {
		Map<Var, Node> result = new HashMap<Var, Node>();
		Iterator<String> it = qs.varNames();
		while(it.hasNext()) {
			String varName = it.next();
			Var var = Var.alloc(varName);

			RDFNode rdfNode = qs.get(varName);
			
			Node node = rdfNode == null ? null : rdfNode.asNode();
			
			result.put(var, node);
		}
		return result;
	}
	
	public static void analyze(Map<Var, Node> map, Map<Node, Integer> nodeToUsage) {
		System.out.println("Binding: " + map);
		for(Entry<Var, Node> entry : map.entrySet()) {
			//Var var = entry.getKey();			
			Node node = entry.getValue();
					
			MapUtils.increment(nodeToUsage, node);
		}
	}
	
	public Map<Node, Integer> getNodeToUsage() {
		return nodeToUsage;
	}
	
	@Override
	public void close() {
		if(onClose != null) {
			onClose.run();
		}
	}
}
