/**
* Copyright (c) 2011, Lawrence Livermore National Security, LLC. 
* Produced at the Lawrence Livermore National Laboratory. 
* Written by Kevin Lawrence, lawrence22@llnl.gov
* Under the guidance of: 
* David Andrzejewski, andrzejewski1@llnl.gov
* David Buttler, buttler1@llnl.gov 
* LLNL-CODE-521811 All rights reserved. This file is part of IRIS
*
* This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
* License (as published by the Free Software Foundation) version 2, dated June 1991. This program is distributed in the
* hope that it will be useful, but WITHOUT ANY WARRANTY; without even the IMPLIED WARRANTY OF MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE. See the terms and conditions of the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
* Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA For full text see license.txt
*
*
*/
package gov.llnl.iscr.iris;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;

/**
 * 
 * This class extends SolrQuery and provides get/set/add methods for fields
 * specifically used in the Dismax request handler. To configure Solr to parse
 * Dismax queries, use the default search handler and set field "deftype" to 
 * "dismax" or "edismax" for Solr 3.1 and above. 
 * 
 * This class is designed for easy integration of Iris with Apache Lucene (Solr) system.
 *
 */
public class DisMaxQuery extends SolrQuery {
	
	private static final long serialVersionUID = 1L;
	private static final String defType = "defType";
	private static final String disMax = "dismax";
	private float defaultBoost = 1.0F;
	
	private Map<String, Float> queryFields = new LinkedHashMap<String, Float>();
	private Map<String, Map<String, Float>> boostQuery = new LinkedHashMap<String, Map<String, Float>>();
	
	public DisMaxQuery(){
		super("");
		this.set(defType,disMax);
	}
	
	public DisMaxQuery(String qstr){
		this();
		this.setQuery(qstr);
	}
	//-|================================
	//-|Set Query Fields methods
	//-|================================
	/**
	 * sets the <code>DisMaxQuery</code> param qf (query field) to the field given (default boost)
	 * @param field String representing field name used to set the query field
	 * @return
	 */
	public DisMaxQuery setQueryField(String field){
		if(field == null || field.isEmpty())
			return this;
		
		setQF(field, defaultBoost);
		return this;
	}
	
	/**
	 * sets the <code>DisMaxQuery</code> param qf (query field) to the field given with the specified boost value
	 * @param field String representing field name used to set the query field
	 * @param boost boost value to associate with the given query field
	 * @return
	 */
	public DisMaxQuery setQueryField(String field, float boost){
		if(field == null || field.isEmpty())
			return this;
		
		setQF(field, boost);
		return this;
	}
	
	/**
	 * private helper method used to set the dismax param qf and store the query fields
	 * @param field String representing field name used to set the query field
	 * @param boost boost value to associate with the given query field
	 */
	private void setQF(String field, float boost){
		queryFields = new LinkedHashMap<String, Float>();
		queryFields.put(field, boost);
		
		this.set(DisMaxParams.QF, field+"^"+boost);
	}
	
	/**
	 * sets the<code>DisMaxQuery</code> param qf (query field) to the fields given (default boost)
	 * @param fields an array of strings representing the field names used to set the query fields
	 * @return
	 */
	public DisMaxQuery setQueryFields(String ... fields){
		if(fields == null || fields.length == 0)
			return this;
		
		queryFields = new LinkedHashMap<String, Float>();
		StringBuffer qfVal = new StringBuffer();
		for(int i=0; i<fields.length; i++){
			queryFields.put(fields[i], defaultBoost);
			qfVal.append(fields[i]+"^"+defaultBoost+" ");
		}
		
		this.set(DisMaxParams.QF, qfVal.toString().trim());
		return this;
	}
	
	/**
	 * sets the <code>DisMaxQuery</code> param qf (query field) according to the given map of fields and associated boost values
	 * @param boostedFields map of fields and associated boost values
	 * @return
	 */
	public DisMaxQuery setQueryFields(Map<String, Float> boostedFields){
		if(boostedFields.isEmpty())
			return this;
		
		queryFields = boostedFields;
		
		StringBuffer qfVal = new StringBuffer();
		Iterator<Entry<String, Float>> entrySet = boostedFields.entrySet().iterator();
		Entry<String, Float> entry;
		while(entrySet.hasNext()){
			entry = entrySet.next();
			qfVal.append(entry.getKey()+"^"+entry.getValue()+" ");
		}
		this.set(DisMaxParams.QF, qfVal.toString().trim());
		return this;
	}
	
	//-|================================
	//-|Set Boost Query methods
	//-|================================
	/**
	 * sets the <code>DisMaxQuery</code> param bq (boost query) to the query term given (default boost, default field)
	 * @param bq a raw query string that will be included with the user's query to influence results
	 * @return
	 */
	public DisMaxQuery setBoostQuery(String bq){
		if(bq == null || bq.isEmpty())
			return this;
		
		setBQ("", Arrays.asList(bq), defaultBoost);
		return this;
	}
	
	/**
	 * sets the <code>DisMaxQuery</code> param bq (boost query) to the query term given with the specified boost value (default field)
	 * @param bq a raw query string that will be included with the user's query to influence the results
	 * @param boost boost value to associate with the given query term
	 * @return
	 */
	public DisMaxQuery setBoostQuery(String bq, float boost){
		if(bq == null || bq.isEmpty())
			return this;
		
		setBQ("", Arrays.asList(bq), boost);
		return this;
	} 
	
	/**
	 * sets the <code>DisMaxQuery</code> param bq (boost query) to the query term given that is boosted on the specified field (default boost)
	 * @param field name of the field that the query term will be boosted on
	 * @param bq a raw query string that will be included with the user's query to influence the results
	 * @return
	 */
	public DisMaxQuery setBoostQuery(String field, String bq){
		if(bq == null || bq.isEmpty())
			return this;
		
		setBQ(field, Arrays.asList(bq), defaultBoost);
		return this;
	}
	
	/**
	 * sets the <code>DisMaxQuery</code> param bq (boost query) to the query term given that is boosted on the specified field using the given boost value
	 * @param field name of the field that the query term will be boosted on
	 * @param bq a raw query string that will be included with the user's query to influence the results
	 * @param boost boost value to associate with the given query term
	 * @return
	 */
	public DisMaxQuery setBoostQuery(String field, String bq, float boost){
		if(bq == null || bq.isEmpty())
			return this;
		
		setBQ(field, Arrays.asList(bq), boost);
	
		return this;
	}
	
	/**
	 * private helper method used to set the boost query param and store the boost queries
	 * @param field name of the field that the query term will be boosted on
	 * @param bqs raw query strings that will be included with the user's query to influence the results
	 * @param boost boost value to associate with the given query terms
	 */
	private void setBQ(String field, List<String> bqs, float boost){
		boostQuery = new LinkedHashMap<String, Map<String, Float>>();
		Map<String,Float> bqTerm = new LinkedHashMap<String, Float>();
		
		StringBuffer bqVals = new StringBuffer();
		for(String bq : bqs){
			bqTerm.put(bq, boost);
			bqVals.append(bq+"^"+boost+" ");
		}
		boostQuery.put(field, bqTerm);
		
		if(field.isEmpty())
			this.set(DisMaxParams.BQ, bqVals.toString().trim());
		else
			this.set(DisMaxParams.BQ, field+":"+bqVals.toString().trim());
	}
	
	/**
	 * sets the <code>DisMaxQuery</code> param bq (boost query) to the query terms given (default boost, default field)
	 * @param bqs raw query strings that will be included with the user's query to influence the results
	 * @return
	 */
	public DisMaxQuery setBoostQuery(List<String> bqs){
		if(bqs.equals(null) || bqs.size() == 0)
			return this;
		
		boostQuery = new LinkedHashMap<String, Map<String, Float>>();

		setBQ("", bqs, defaultBoost);
		return this;
	}
	
	/**
	 * sets the <code>DisMaxQuery</code> param bq (boost query) according to the given map of query terms and associated boost values (default field)
	 * @param boostedQueries map of query terms and associated boost values
	 * @return
	 */
	public DisMaxQuery setBoostQuery(Map<String,Float> boostedQueries){
		if(boostedQueries.isEmpty())
			return this;
		
		setBQ("", boostedQueries);
		return this;
	}
	
	/**
	 * sets the <code>DisMaxQuery</code> param bq (boost query) according to the given map of query terms and associated boost values. 
	 * Query terms are boosted on the given field.
	 * @param field name of the field that the query term will be boosted on
	 * @param boostedQueries map of query terms and associated boost values
	 * @return
	 */
	public DisMaxQuery setBoostQuery(String field, Map<String,Float> boostedQueries){
		if(boostedQueries.isEmpty())
			return this;
		
		setBQ(field, boostedQueries);
		return this;
	}
	
	/**
	 * private helper method used to set the boost query param and store the boost queries
	 * @param field name of the field that the query term will be boosted on
	 * @param boostedQueries map of query terms and associated boost values
	 */
	private void setBQ(String field, Map<String,Float> boostedQueries){
		boostQuery = new LinkedHashMap<String, Map<String, Float>>();
		boostQuery.put(field, boostedQueries);
		
		StringBuffer bqVals = new StringBuffer();
		Iterator<Entry<String, Float>> entrySet = boostedQueries.entrySet().iterator();
		Entry<String, Float> entry;
		while(entrySet.hasNext()){
			entry = entrySet.next();
			bqVals.append(entry.getKey()+"^"+entry.getValue()+" ");
		}
		
		if(field.isEmpty())
			this.set(DisMaxParams.BQ, bqVals.toString().trim());
		else
			this.set(DisMaxParams.BQ, field+":"+bqVals.toString().trim());
	}
	//-|================================
	//-|Add Boost Query methods
	//-|================================
	/**
	 * appends the query term given to the <code>DisMaxQuery</code> param bq (default boost, default field)
	 * @param bq a raw query string that will be included with the user's query to influence results
	 * @return
	 */
	public DisMaxQuery addBoostQuery(String bq){
		if(bq == null || bq.isEmpty())
			return this;
	
		this.modifyBoostQuery(addBQ("", Arrays.asList(bq), defaultBoost));
		return this;
	}
	
	/**
	 * appends the query term given with specified boost value to the <code>DisMaxQuery</code> param bq (default field)
	 * @param bq a raw query string that will be included with the user's query to influence results
	 * @param boost boost value to associate with the given query term
	 * @return
	 */
	public DisMaxQuery addBoostQuery(String bq, float boost){
		if(bq == null || bq.isEmpty())
			return this;
		
		this.modifyBoostQuery(addBQ("", Arrays.asList(bq), boost));
		return this;
	}
	
	/**
	 * private helper method that appends query terms and boost values to the specified field
	 * @param field
	 * @param bqs raw query strings that will be included with the user's query to influence results
	 * @param boost boost value to associate with the given query terms
	 * @return
	 */
	private Map<String, Map<String, Float>> addBQ(String field, List<String> bqs, float boost){
		Map<String,Float> newBq = new LinkedHashMap<String,Float>();
		if(boostQuery.containsKey(field)){
			newBq = boostQuery.get(field);
		}else{
			if(field.isEmpty()){
				field = getLastField();
				newBq = boostQuery.get(field);
			}
		}
		
		for(String q : bqs){
			newBq.put(q, boost);
		}
		
		boostQuery.put(field, newBq);
		return boostQuery;
	}
	
	/**
	 * private helper method used to retrieve the last field in the boost query param
	 * @return
	 */
	private String getLastField(){
		Object[] temp = boostQuery.keySet().toArray();
		int index = ((temp.length)-1);
		return temp[index].toString();
	}
	
	/**
	 * appends the query field and term given to the <code>DisMaxQuery</code> param bq (default boost)
	 * @param field name of the field that the query term will be boosted on
	 * @param bq a raw query string that will be included with the user's query to influence results
	 * @return
	 */
	public DisMaxQuery addBoostQuery(String field, String bq){
		if(bq == null || bq.isEmpty())
			return this;
		
		this.modifyBoostQuery(addBQ(field, Arrays.asList(bq), defaultBoost));
		return this;
	}
	
	/**
	 * appends the query field and terms given to the <code>DisMaxQuery</code> param bq (default boost)
	 * @param field name of the field that the query term will be boosted on
	 * @param bqs raw query strings that will be included with the user's query to influence results
	 * @return
	 */
	public DisMaxQuery addBoostQuery(String field, List<String> bqs){
		if(bqs == null || bqs.isEmpty())
			return this;
		
		this.modifyBoostQuery(addBQ(field, bqs, defaultBoost));
		return this;
	}
	
	/**
	 * appends the query field and term given with specified boost value to the <code>DisMaxQuery</code> param bq
	 * @param field name of the field that the query term will be boosted on
	 * @param bq a raw query string that will be included with the user's query to influence results
	 * @param boost boost value to associate with the given query terms
	 * @return
	 */
	public DisMaxQuery addBoostQuery(String field, String bq, float boost){
		if(bq == null || bq.isEmpty())
			return this;
		
		this.modifyBoostQuery(addBQ(field, Arrays.asList(bq), boost));
		return this;
	}
	
	/**
	 * appends the query terms given to the <code>DisMaxQuery</code> param bq (default boost, default field)
	 * @param bqs raw query strings that will be included with the user's query to influence results
	 * @return
	 */
	public DisMaxQuery addBoostQuery(List<String> bqs){
		if(bqs == null || bqs.size() == 0)
			return this;
		
		this.modifyBoostQuery(addBQ("", bqs, defaultBoost));
		return this;
	}
	
	/**
	 * appends the query terms given with the associated boost values provided to the <code>DisMaxQuery</code> param bq (default field)
	 * @param boostedQueries map of query terms and associated boost values
	 * @return
	 */
	public DisMaxQuery addBoostQuery(Map<String,Float> boostedQueries){
		if(boostedQueries.isEmpty())
			return this;
		
		Map<String,Float> newBq;
		String key = "";
		if(!boostQuery.isEmpty()){
			if(!boostQuery.containsKey("")){
				key = getLastField();
			}	
			newBq = boostQuery.get(key);
			newBq.putAll(boostedQueries);
			boostQuery.put(key, newBq);
			
		}
		else{
			boostQuery.put(key, boostedQueries);
		}
		
		this.modifyBoostQuery(boostQuery);
		return this;
	}

	/**
	 * appends the query field and terms given with the associated boost values provided to the <code>DisMaxQuery</code> param bq
	 * @param field name of the field that the query term will be boosted on
	 * @param boostedQueries map of query terms and associated boost values
	 * @return
	 */
	public DisMaxQuery addBoostQuery(String field, Map<String,Float> boostedQueries){
		if(boostedQueries.isEmpty())
			return this;
		
		Map<String,Float> newBq = new LinkedHashMap<String,Float>();
		if(boostQuery.containsKey(field)){
			newBq = boostQuery.get(field);
		}
		
		newBq.putAll(boostedQueries);
		boostQuery.put(field, newBq);
		
		this.modifyBoostQuery(boostQuery);
		return this;
	}
	
	//-|================================
	//-|Add Query Fields methods
	//-|================================
	/**
	 * appends the given query field to the <code>DisMaxQuery</code> param qf (default boost)
	 * @param field String representing field name used to append to the existing query fields 
	 * @return
	 */
	public DisMaxQuery addQueryField(String field){
		queryFields.put(field, defaultBoost);
		this.modifyQueryFields(queryFields);
		return this;
	}
	
	/**
	 * appends the given query field with specified boost value to the <code>DisMaxQuery</code> param qf
	 * @param field String representing field name used to append to the existing query fields
	 * @param boost boost value to associate with the given query field
	 * @return
	 */
	public DisMaxQuery addQueryField(String field, float boost){
		queryFields.put(field, boost);
		this.modifyQueryFields(queryFields);
		return this;
	}
	
	/**
	 * appends the given query fields to the <code>DisMaxQuery</code> param qf (default boost)
	 * @param fields a list of strings representing field names used to append to the existing query fields
	 * @return
	 */
	public DisMaxQuery addQueryFields(List<String> fields){
		if(fields == null || fields.size() == 0)
			return this;
					
		for(String field : fields){
			queryFields.put(field, defaultBoost);	
		}
		this.modifyQueryFields(queryFields);
		return this;
	}
	
	/**
	 * appends the key-value pairs within the given map to the <code>DisMaxQuery</code> param qf
	 * @param boostedFields map of fields and associated boost values
	 * @return
	 */
	public DisMaxQuery addQueryFields(Map<String, Float> boostedFields){
		queryFields.putAll(boostedFields);
		this.modifyQueryFields(queryFields);
		return this;
	}
	
	//-|====================================
	//-|Private helper for modifying params
	//-|====================================
	private DisMaxQuery modifyBoostQuery(Map<String, Map<String,Float>> boostQueries){
		StringBuffer bqVals = new StringBuffer();
		
		Iterator<Entry<String, Map<String, Float>>> entrySet = boostQueries.entrySet().iterator();
		Entry<String, Map<String, Float>> entry;
		
		Iterator<Entry<String, Float>> innerEntrySet;
		Entry<String, Float> innerEntry;
		
		//Constructing the boost boost query string for ModifiableSolrParams set method
		String key;
		while(entrySet.hasNext()){
			entry = entrySet.next();
			key = entry.getKey();
			if(!key.isEmpty())
				bqVals.append(key+":");
		
			innerEntrySet = entry.getValue().entrySet().iterator();
			while(innerEntrySet.hasNext()){
				innerEntry = innerEntrySet.next();
				bqVals.append(innerEntry.getKey()+"^"+innerEntry.getValue()+" ");
			}
			
		}
		this.set(DisMaxParams.BQ, bqVals.toString().trim());
		return this;
	}
	
	private DisMaxQuery modifyQueryFields(Map<String, Float> qf){
		if(qf.isEmpty())
			return this;
		
		StringBuffer qfVal = new StringBuffer();
		Iterator<Entry<String, Float>> entrySet = qf.entrySet().iterator();
		Entry<String, Float> entry;
		while(entrySet.hasNext()){
			entry = entrySet.next();
			qfVal.append(entry.getKey()+"^"+entry.getValue()+" ");
		}
		
		this.set(DisMaxParams.QF, qfVal.toString().trim());
		return this;
	}
	
	//-|============================
	//-|Get methods for DisMaxQuery
	//-|============================
	/**
	 * gets the default boost value associate with the <code>DisMaxQuery</code>
	 * @return
	 */
	public float getDefaultBoost() {
		return defaultBoost;
	}

	/**
	 * set the default boost value for the <code>DisMaxQuery</code>
	 * @param defaultBoost
	 */
	public void setDefaultBoost(float defaultBoost) {
		this.defaultBoost = defaultBoost;
	}
	
	/**
	 * returns a map of the query fields that the <code>DisMaxQuery</code> consist of
	 * @return
	 */
	public Map<String, Float> getQueryFields() {
		return queryFields;
	}

	/**
	 * returns a map of the boost queries that the <code>DisMaxQuery</code> consist of
	 * @return
	 */
	public Map<String, Map<String, Float>> getBoostQuery() {
		return boostQuery;
	}
	
	//-|====================================
	//-|Set methods for basic common params
	//-|====================================
	/**
	 * sets the highlights param for the <code>DisMaxQuery</code>
	 * @param num the number of snippets to return
	 * @param field the field name to apply the highlights to
	 * @return
	 */
	public DisMaxQuery setHighlights(int num, String field){
		this.setHighlight(true);
		this.setParam("hl.fl", field);
		this.setHighlightSnippets(num);
		return this;
	}
	
	/**
	 * sets the highlights param for the <code>DisMaxQuery</code>
	 * @param num the number of snippets to return
	 * @param fields the field names to apply the highlights to
	 * @return
	 */
	public DisMaxQuery setHighlights(int num, String... fields){
		this.setHighlight(true);
		this.setParam("hl.fl", fields);
		this.setHighlightSnippets(num);
		return this;
	}
	
	//-|============================================================
	//-|DisMaxParams class; supported as local or as request params
	//-|============================================================
	/**
	 *Static class that includes string representations for all of the supported params by DisMaxQuery
	 *
	 */
	public static class DisMaxParams{
		static final String ALTQ = "q.alt";
		static final String QF = "qf";
		static final String MM = "mm";
		static final String PF = "pf";
		static final String PS = "ps";
		static final String QS = "qs";
		static final String TIE = "tie";
		static final String BQ = "bq";
		static final String BF = "bf";
	}
	
}
