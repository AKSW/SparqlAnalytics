package org.aksw.sparql_analytics.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.util.jdbc.ColumnsReference;
import org.aksw.commons.util.jdbc.PrimaryKey;
import org.aksw.commons.util.jdbc.Schema;
import org.aksw.jena_sparql_api.cache.extra.SqlUtils;
import org.apache.commons.lang.StringEscapeUtils;



public class Inserter {
	private ColumnsReference target;
	private Schema schema;
	
	private List<Object> data = new ArrayList<Object>(); 
	
	
	
	public Inserter(ColumnsReference target, Schema schema) {
		this.target = target;
		this.schema = schema;
	}
	
	//private List<List<String>> uniqueColumns;

	/*
	void check() {
		PrimaryKey pk;
		
		if()
		pk.getSource().getColumnNames()
	}
	*/
	
	public void add(Object ... cells) {
		if(cells.length != target.getColumnNames().size()) {
			throw new RuntimeException("Provided cells (" + cells.length + ") does not match number of columns (" + target.getColumnNames().size() + "), Columns:" + target.getColumnNames() + " Data: " + cells);
		}

		for(Object cell : cells) {
			data.add(cell);
		}		
	}
	
	
	public static String escapeSql(Object o) {
		if(o == null) {
			return "NULL";
		} else if(o instanceof Number) {
			return "" + o;
		} else{
			return "'" + StringEscapeUtils.escapeSql("" + o) + "'";
		}
	}
	
	public String composeCheckPart(List<Object> cells, int columnWidth, int[] idMap) {
	
		// TODO Only works with columnWidth 1
		
		int numRows = data.size() / columnWidth;
		String idList = "";
		for(int i = 0; i < numRows; ++i) {

			if(i != 0) {
				idList += ", ";
			}
			
			
			for(int j = 0; j < idMap.length; ++j) {
				int index = i * columnWidth + idMap[j];
				Object cell = data.get(index);
			
				if(j != 0) {
					idList += ", ";
				}
				
				idList += escapeSql("" + cell);
			}

		}

		return idList;
	}
	
	public Set<Integer> getBlacklistedRows(List<Object> cells, int columnWidth, int[] idMap, Set<Object> ids) {
		Set<Integer> result = new HashSet<Integer>();
		
		int numRows = data.size() / columnWidth;
		for(int i = 0; i < numRows; ++i) {

			for(int j = 0; j < idMap.length; ++j) {
				int index = i * columnWidth + idMap[j];
				Object cell = data.get(index);
				
				if(ids.contains(cell)) {
					result.add(i);
				}
			}
			
		}
		
		return result;
	}
	
	public String composeInsertPart(List<Object> cells, int columnWidth, Set<Integer> blacklistedRows) {
		
		// TODO Only works with columnWidth 1
		
		int numRows = data.size() / columnWidth;
		String idList = "";
		for(int i = 0; i < numRows; ++i) {
			
			if(blacklistedRows.contains(i)) {
				continue;
			}
			
			if(!idList.isEmpty()) {
				idList += ", ";
			}
			
			idList += "(";
			
			for(int j = 0; j < columnWidth; ++j) {
				int index = i * columnWidth + j;
				Object cell = data.get(index);

				if(j != 0) {
					idList += ", ";
				}
				
				idList += escapeSql("" + cell);
			}
			
			idList += ")";
		}

		return idList;
	}
	
	
	
	public void flush(Connection conn) throws SQLException {
		PrimaryKey targetPk = schema.getPrimaryKeys().get(target.getTableName());
		
		int columnWidth = target.getColumnNames().size();
		
		Set<Object> duplicateIds = new HashSet<Object>();
		Set<Integer> blacklistedRows = Collections.emptySet();
		
		if(targetPk != null) {
			List<String> idColumns = targetPk.getSource().getColumnNames();			

			
			int d = idColumns.size();
			
			int idMap[] = new int[d];
			
			for(int i = 0; i < d; ++i) {
				String idColumn = idColumns.get(0);
				int j = target.getColumnNames().indexOf(idColumn);
				
				if(j < 0) {
					throw new RuntimeException("Need all primary key columns: Inserted Columns " + target.getColumnNames() + ", Primary Key: " + targetPk.getSource().getColumnNames());
				}
				
				idMap[i] = j;				
			}

			
			
			if(d != 1) {
				throw new RuntimeException("Only singe column primary keys supported - Sorry :(");
			}
			
			
			String idColumn = idColumns.get(0);

			String idList = composeCheckPart(data, columnWidth, idMap); 
			
			{
				// TODO Add a switch so we additionally fetch the values in the DB
				// So we can present the user with the existing values for conflicting entries
				
				if(!idList.isEmpty()) {
					String query = "SELECT " + idColumn + " FROM " + target.getTableName() + " WHERE " + idColumn + " IN (" + idList + ")";
					System.out.println("Dup check: " + query);
					
					List<Object> dupList = SqlUtils.executeList(conn, query, Object.class);
					duplicateIds.addAll(dupList);
				
					System.out.println("Dups are: " + duplicateIds);
				}
			}
			
			// Remove all duplicates from the data

			blacklistedRows = getBlacklistedRows(data, columnWidth, idMap, duplicateIds);
			//data.removeAll(duplicateIds);			
		}
		
		

		
		String valueList = composeInsertPart(data, columnWidth, blacklistedRows); 

		
		{
			if(!valueList.isEmpty()) {
				String query = "INSERT INTO " + target.getTableName() + " VALUES " + valueList;
				System.out.println("Insert: " + query);
				SqlUtils.execute(conn, query, Void.class);
			}
		}
		
		
		data.clear();

		
		
	}
}
