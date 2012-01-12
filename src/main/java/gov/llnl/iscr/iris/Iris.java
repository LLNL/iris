/**
* Copyright (c) 2011, Lawrence Livermore National Security, LLC. 
* Produced at the Lawrence Livermore National Laboratory. 
* Written by Kevin Lawrence, lawrence22@llnl.gov
* Under the guidance of: 
* David Andrzejewski, andrzejewski1@llnl.gov
* David Buttler, buttler1@llnl.gov 
* CODE-XXXXXX All rights reserved. This file is part of IRIS
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

import gov.llnl.iscr.iris.LDAHandler;
import gov.llnl.iscr.iris.DisMaxQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocumentList;

import com.mongodb.BasicDBObject;
/**
 * 
 * Iris is the driver for the <code>LDAHandler</code>. It demonstrates the intended usage of the 
 * <code>LDAHandler</code> and simplifies the use of the latent topic retrieval component. 
 * It essentially provides a single class solution for bridging the interaction between 
 * pre-learned latent topics and a pre-existing information retrieval system.
 * <p>A typical invocation sequence is:
 * <blockquote><pre>
 * MongoInstance mongo = new MongoInstance("127.0.0.1", "topicModel");
 * LDAModel model = new LDAModel(mongo);
 * LDAHandler lda = new LDAHandler(model);
 * Iris iris = new Iris(lda);
 * </pre></blockquote>
 * Once instantiated, all calls can be made to iris to achieve the default operations.
 * For customizing: call upon the methods of the other classes of the component. 
 * Otherwise, iris is all you need for implementing the latent topic feedback functionality.
 * <p>For example, a complete sequence to retrieve and display topics to console in Java is:
 * <blockquote><pre>
 * iris.setLatentTopics(results);
 * iris.setLatentTopicsNgrams();
 * Map<Integer, List<BasicDBObject>> ngrams = iris.getLatentTopicNgrams();
 * for(Integer topicID : iris.getLatentTopics()){
 * 		System.out.println("Topic: "+topicID);
 * 		System.out.println(iris.getTrigram(ngrams.get(topicID)));
 * 		System.out.println(iris.getBigrams(ngrams.get(topicID)));
 * 		System.out.println(iris.getUnigrams(ngrams.get(topicID)));
 * }
 * </pre></blockquote>
 * Iris may also be used for query expansion. 
 * 
 */

public class Iris {
	LDAHandler lda;
	List<Integer> latentTopics;
	Map<Integer, List<BasicDBObject>> latentTopicNgrams;
	
	/**
	 * returns a list of topic IDs representing the latent topics to be displayed
	 * @return
	 */
	public List<Integer> getLatentTopics() {
		return latentTopics;
	}
	
	/**
	 * returns a key-value map of the latent topic IDs and their respective ngrams to be displayed
	 * @return
	 */
	public Map<Integer, List<BasicDBObject>> getLatentTopicNgrams() {
		return latentTopicNgrams;
	}
	
	/**
	 * returns a key-value map of the latent topic IDs and a list of words to be used for query expansion
	 * @return
	 */
	public Map<Integer, List<String>> getExpansionWords() {
		return lda.getAllExpansionWords();
	}
	
	/**
	 * creates an instance of <code>Iris</code> with given <code>LDAHandler</code>
	 * @param lda the LDAHandler to be used by Iris
	 */
	public Iris(LDAHandler lda){
		this.lda = lda;
	}
	
	/**
	 * sets a compiled list of enriched topics and related topics
	 * @param results the result list from user query containing documents
	 */
	public void setLatentTopics(SolrDocumentList results){
		
		lda.setEnrichedTopicSet(Arrays.asList(results.get(0).get("id"), results.get(1).get("id")));
		latentTopics = new ArrayList<Integer>(lda.getEnrichedTopicSet());
		
		lda.setRelatedTopicSet();
		List<Integer> temp = lda.getRelatedTopicSet();
		for(Integer topicID : temp){
			if(!latentTopics.contains(topicID))
				latentTopics.add(topicID);
		}
		
	}
	
	/**
	 * sets a map of topics and selected ngrams, which includes a trigram, two (2) bigrams and four (4) unigrams
	 */
	public void setLatentTopicsNgrams(){
		if(!latentTopics.isEmpty()){
			latentTopicNgrams = new LinkedHashMap<Integer, List<BasicDBObject>>();
			Iterator<Integer> it = latentTopics.iterator();
			Integer selectedTopic;
			List<BasicDBObject> temp;
			while(it.hasNext()){
				selectedTopic = it.next();
				lda.setNgrams(selectedTopic);
				lda.setUnigrams(selectedTopic);
				temp = new ArrayList<BasicDBObject>(lda.getSelectedNgrams());
				temp.addAll(lda.getSelectedUnigrams());
				latentTopicNgrams.put(selectedTopic, temp);
				
			}
			
		}
	}
	
	/**
	 * returns a <code>String</code> representation of a trigram, if it exist, within the given list of ngrams
	 * @param ngrams a list of ngram objects
	 * @return
	 */
	public String getTrigram(List<BasicDBObject> ngrams){
		BasicDBObject trigram = ngrams.get(0);
		if((Integer)trigram.get("size")==3)
			return trigram.getString("ngram");
		else
			return "(No trigrams found)";
	}
	
	/**
	 * returns true if the given list of ngrams contains a trigram, otherwise false
	 * @param ngrams a list of ngram objects
	 * @return
	 */
	public boolean hasTrigram(List<BasicDBObject> ngrams){
		boolean hasTrigram = false;
		if((Integer)ngrams.get(0).get("size") == 3)
			hasTrigram = true;
		
		return hasTrigram;
	}
	
	/**
	 * returns a single formatted <code>String</code> representation of bigrams within the given list of ngrams
	 * @param ngrams a list of ngram objects
	 * @return
	 */
	public String getBigrams(List<BasicDBObject> ngrams){
		StringBuffer bigrams = new StringBuffer();
		int count = 0;
		for(BasicDBObject bigram : ngrams){
			if((Integer)bigram.get("size") == 2){
				if(count == 0){
					bigrams.append(bigram.get("ngram"));
					++count;
				}
				else if(count == 1){
					bigrams.append(", "+bigram.get("ngram"));
					break;
				}			
			}else
				continue;
		}
		
		return bigrams.toString();
	}
	
	/**
	 * returns a single formatted <code>String</code> representation of unigrams within the given list of ngrams
	 * @param ngrams
	 * @return
	 */
	public String getUnigrams(List<BasicDBObject> ngrams){
		StringBuffer unigrams = new StringBuffer();
		int count = 0;
		for(BasicDBObject unigram : ngrams){
			if(unigram.containsField("word")){
				if(count == 0){
					unigrams.append(unigram.get("word"));
					++count;
				}
				else if(count < 4){
					unigrams.append(", "+unigram.get("word"));
					++count;
				}
				else
					break;
			}				
		}
		return unigrams.toString();
	}
	
	/**
	 * returns the given {@link DisMaxQuery} with appended expansion terms 
	 * retrieved from the given list of topics. It calls on {@link DisMaxQuery#addBoostQuery(Map)}
	 * to perform the expansion using the default boost value.
	 * @param query the DisMaxQuery object to be expanded
	 * @param topics the list of topics used to retrieve the expansion words for boosting
	 * @param sign used to distinguish between positive ('+') and "negative" ('-') boosting
	 * @return
	 */
	public DisMaxQuery expandBoostQuery(DisMaxQuery query, List<String> topics, char sign){
		query.addBoostQuery(buildBoostQueryMap(getListOfTerms(topics), query.getDefaultBoost(), sign));
		return query;
	}
	
	/**
	 * returns the given <code>DisMaxQuery</code> with appended expansion terms
	 * retrieved from the given list of topics. It calls on {@link DisMaxQuery#addBoostQuery(String, Map)}
	 * to perform the expansion using the default boost value.
	 * @param query the DisMaxQuery object to be expanded
	 * @param topics the list of topics used to retrieve the expansion words for boosting
	 * @param field represents the indexed field to boost on
	 * @param sign used to distinguish between positive ('+') and "negative" ('-') boosting
	 * @return
	 */
	public DisMaxQuery expandBoostQuery(DisMaxQuery query, List<String> topics, String field, char sign){
		query.addBoostQuery(field, buildBoostQueryMap(getListOfTerms(topics), query.getDefaultBoost(), sign));
		return query;
	}
	
	/**
	 * returns the given <code>DisMaxQuery</code> with appended expansion terms
	 * retrieved from the given list of topics. It calls on the <code>DisMaxQuery</code> 
	 * <code>method addBoostQuery(Map<String, Float>)</code>
	 * to perform the expansion using the given boost value.
	 * @param query the DisMaxQuery object to be expanded
	 * @param topics the list of topics used to retrieve the expansion words for boosting
	 * @param boost the value used to boost the query terms
	 * @param sign used to distinguish between positive ('+') and "negative" ('-') boosting
	 * @return
	 */
	public DisMaxQuery expandBoostQuery(DisMaxQuery query, List<String> topics, float boost, char sign){
		query.addBoostQuery(buildBoostQueryMap(getListOfTerms(topics), boost, sign));
		return query;
	}
	
	/**
	 * returns the given <code>DisMaxQuery</code> with appended expansion terms
	 * retrieved from the given list of topics. It calls on the <code>DisMaxQuery</code> 
	 * method <code>addBoostQuery(String, Map<String, Float>)</code>
	 * to perform the expansion using the given boost value.
	 * @param query the DisMaxQuery object to be expanded
	 * @param topics the list of topics used to retrieve the expansion words for boosting
	 * @param field represents the indexed field to boost on
	 * @param boost the value used to boost the query terms
	 * @param sign used to distinguish between positive ('+') and "negative" ('-') boosting
	 * @return
	 */
	public DisMaxQuery expandBoostQuery(DisMaxQuery query, List<String> topics, String field, float boost, char sign){
		query.addBoostQuery(field, buildBoostQueryMap(getListOfTerms(topics), boost, sign));
		return query;
	}
	
	/**
	 * returns the given {@link DisMaxQuery} with a new set of expansion terms 
	 * retrieved from the given list of topics. It calls on {@link DisMaxQuery#setBoostQuery(Map)}
	 * to perform the expansion using the default boost value.
	 * @param query the DisMaxQuery object to be expanded
	 * @param topics the list of topics used to retrieve the expansion words for boosting
	 * @return
	 */
	public DisMaxQuery resetBoostQuery(DisMaxQuery query, List<String> topics){
		query.setBoostQuery(buildBoostQueryMap(getListOfTerms(topics), query.getDefaultBoost(), '+'));
		return query;
	}
	
	/**
	 * returns the given <code>DisMaxQuery</code> with a new set of expansion terms
	 * retrieved from the given list of topics. It calls on {@link DisMaxQuery#setBoostQuery(String, Map)}
	 * to perform the expansion using the default boost value.
	 * @param query the DisMaxQuery object to be expanded
	 * @param topics the list of topics used to retrieve the expansion words for boosting
	 * @param field represents the indexed field to boost on
	 * @return
	 */
	public DisMaxQuery resetBoostQuery(DisMaxQuery query, List<String> topics, String field){
		query.setBoostQuery(field, buildBoostQueryMap(getListOfTerms(topics), query.getDefaultBoost(), '+'));
		return query;
	}
	
	/**
	 * returns the given <code>DisMaxQuery</code> with a new set of expansion terms
	 * retrieved from the given list of topics. It calls on the <code>DisMaxQuery</code> 
	 * method <code>setBoostQuery(Map<String, Float>)</code>
	 * to perform the expansion using the given boost value.
	 * @param query the DisMaxQuery object to be expanded
	 * @param topics the list of topics used to retrieve the expansion words for boosting
	 * @param boost the value used to boost the query terms
	 * @return
	 */
	public DisMaxQuery resetBoostQuery(DisMaxQuery query, List<String> topics, float boost){
		query.setBoostQuery(buildBoostQueryMap(getListOfTerms(topics), boost, '+'));
		return query;
	}
	
	/**
	 * returns the given <code>DisMaxQuery</code> with a new set of expansion terms
	 * retrieved from the given list of topics. It calls on the <code>DisMaxQuery</code> 
	 * method <code>setBoostQuery(String, Map<String, Float>)</code>
	 * to perform the expansion using the given boost value.
	 * @param query the DisMaxQuery object to be expanded
	 * @param topics the list of topics used to retrieve the expansion words for boosting
	 * @param field represents the indexed field to boost on
	 * @param boost the value used to boost the query terms
	 * @return
	 */
	public DisMaxQuery resetBoostQuery(DisMaxQuery query, List<String> topics, String field, float boost){
		query.setBoostQuery(field, buildBoostQueryMap(getListOfTerms(topics), boost, '+'));
		return query;
	}
	
	//-|===============================================
	//-|Private helper methods used in query expansion
	//-|===============================================
	/**
	 * builds and returns a map that represents the values of a boost query (bq) parameter for a Disjunction Max query.
	 * It contains the mapping of query terms and associated boost values.
	 * @param terms represents the query terms to boost
	 * @param boost the value used to boost the query terms
	 * @param sign used to distinguish between positive ('+') and "negative" ('-') boosting
	 * @return
	 * @see {@link http://wiki.apache.org/solr/DisMaxQParserPlugin}
	 */
	private Map<String, Float> buildBoostQueryMap(Iterator<String> terms, float boost, char sign){
		Map<String,Float> boostQueries = new LinkedHashMap<String, Float>();
		switch(sign){
		case '-':
			while(terms.hasNext()){
				boostQueries.put("-"+terms.next(), boost);
			}
		case '+':
		default:
			while(terms.hasNext()){
				boostQueries.put(terms.next(), boost);
			}
			break;
		}
		return boostQueries;
	}
	
	/**
	 * returns an iterator list of words to be used for query expansion for the given list of topics
	 * @param topics represents the list of topics chosen (topic IDs are accepted as <code>String</code>
	 * @return
	 */
	private Iterator<String> getListOfTerms(List<String> topics){
		List<String> bqTerms = new ArrayList<String>();
		for(String topicID : topics){
			bqTerms.addAll(lda.getTopicExpansionWords(Integer.parseInt(topicID)));
		}
		Iterator<String> it = bqTerms.iterator();
		return it;
	}
}
