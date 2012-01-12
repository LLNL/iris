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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import gov.llnl.iscr.iris.LDAModel;
/**
 * 
 * This class provides the methods for processing the data retrieved by the {@link LDAModel}. 
 * It contains an LDAModel as one of its members and makes the appropriate calls for data via this object.
 * It then processes the data according to the topic selection procedure for the latent topic feedback system.
 * <p>A typical invocation sequence is:
 * <blockquote><pre>
 * MongoInstance mongo = new MongoInstance("127.0.0.1", "topicModel");
 * LDAModel model = new LDAModel(mongo);
 * LDAHandler lda = new LDAHandler(model);
 * </pre></blockquote>
 * 
 */
public class LDAHandler {
	private final LDAModel model;
	private double topicThreshold = -100.0;
	private List<Integer> enrichedSet = null;
	private List<Integer> relatedSet = null;
	private List<BasicDBObject> selectedNgrams = null;
	private List<BasicDBObject> selectedUnigrams = null;
	private Map<Integer, List<String>> expansionWords = new LinkedHashMap<Integer, List<String>>();
	
	public static enum TopicType {
		ENRICHED, RELATED
	}
	/**
	 * creates an instance of LDAHandler based on the LDAModel provided
	 * @param model
	 */
	public LDAHandler(LDAModel model){
		this.model = model;
	}
	
	/**
	 * sets the threshold value to be used for filtering "junk" topics
	 * to a value obtained by thresholding the topic semantic coherence scores
	 * at the percentile value given.
	 * @param thresholdPercentile
	 * @return
	 */
	public LDAHandler setTopicThreshold(float thresholdPercentile){
		
		DBCursor semcoCur = model.getSemcoValues();
		int semcoCount = semcoCur.count();
		int limit = (int)(thresholdPercentile*semcoCount)-1;
	
		this.topicThreshold = (Double) semcoCur.toArray().get(limit).get("semco");
		return this;
	}
	
	/**
	 * set the threshold value to be used for filtering "junk" topics to the absolute value given 
	 * @param threshold
	 * @return
	 */
	public LDAHandler setTopicThreshold(double threshold){
		this.topicThreshold = threshold;
		return this;
	}
	
	/**
	 * sets the list of enriched topics for the user query using the results list; 
	 * call {@link LDAHandler#getRelatedTopicSet()} on the returned
	 * LDAHandler to retrieve the list.
	 * @param results
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public LDAHandler setEnrichedTopicSet(List<Object> docIDs){
		enrichedSet = new ArrayList<Integer>();
		int iterateCount = 0;
		for(int i=0; i<2; i++){			
			//-|===============================================
			//-|1. Gets associated topics for given document
			//-|2. Sorts topics by probability, descending
			//-|3. Filters topics according to threshold value
			//-|===============================================

			List<DBObject> topics = (List<DBObject>) model.getTopics(docIDs.get(i)).get("topics");
			Collections.sort(topics, new TopicSortByProb());
			List<Integer> temp = filterTopics(topics, topicThreshold, TopicType.ENRICHED);
			
			//-|===============================================
			//-|Iterates temp to extract enriched topics
			//-|===============================================
			int index = 0;
			int numOfTopicAdded = 0;
			while(numOfTopicAdded < 2 && index<temp.size() && enrichedSet.size()<4){
				if(!enrichedSet.contains(temp.get(index))){
					enrichedSet.add(temp.get(index));
					++index;
					++numOfTopicAdded;
				}else{
					++index;
				}
			}
			//If the enriched set does not contain four (4) topics after searching both topic lists
			if(i == 1 && enrichedSet.size() < 4){
				i = -1;
				++iterateCount;
			}
			//If the topic list has been iterated twice: Exit
			if(iterateCount == 2)
				break;
		}
		
		return this;
	}
	
	/**
	 * sets the list of topics that are related to the enriched topic list; 
	 * call {@link LDAHandler#getRelatedTopicSet()} on the returned
	 * LDAHandler to retrieve the list.
	 * @return
	 */
	public LDAHandler setRelatedTopicSet(){
		
		relatedSet = new ArrayList<Integer>();
		if(enrichedSet != null){
			List<DBCursor> curList = model.getRelatedTopics(enrichedSet);
			for(DBCursor cur : curList){
				List<Integer> temp = filterTopics(cur.toArray(), topicThreshold, TopicType.RELATED);
				relatedSet.add(temp.get(0));
				relatedSet.add(temp.get(1));
			}
		}
		else{
			System.err.println("Related Topics Set cannot be populated!");
			System.err.println("Ensure enriched topic set has been established.");
		}
		return this;	
	}
	
	/**
	 * sets the ngrams for the given topic; 
	 * call {@link LDAHandler#getSelectedNgrams()} on the returned
	 * LDAHandler to retrieve the list.
	 * @param selectedTopic
	 * @return
	 */
	public LDAHandler setNgrams(Object selectedTopic){
		//-|==================================================
		//-|1. Instantiate multicomparator for sorting ngrams
		//-|	by size and score
		//-|2. Get ngrams from model and sort them
		//-|==================================================
		List<Comparator<BasicDBObject>> comps = new ArrayList<Comparator<BasicDBObject>>();
		comps.add(new NgramSortBySize()); 
		comps.add(new NgramSortByScore());
		MultiComparator<BasicDBObject> multiComp = new MultiComparator<BasicDBObject>(comps);
		
		@SuppressWarnings("unchecked")
		List<BasicDBObject> topicNgrams = (List<BasicDBObject>) model.getNgrams(selectedTopic).get("ngrams");
		Collections.sort(topicNgrams, multiComp);
		List<BasicDBObject> allNgrams = topicNgrams;
		
		
		//-|=====================================
		//-|Extract top trigram and
		//-|top two (2) bigrams from sort ngrams
		//-|=====================================
		boolean tri = true;
		int biCount = 0;
		selectedNgrams = new ArrayList<BasicDBObject>();		
		for(int i=0; i<allNgrams.size(); i++){
			if(tri && (Integer)allNgrams.get(i).get("size") == 3){
				tri = false;
				selectedNgrams.add(allNgrams.get(i));
			}else if(biCount<2){
				if((Integer)allNgrams.get(i).get("size") == 2){
					selectedNgrams.add(allNgrams.get(i));
					++biCount;
				}
			}else break;
					
		}
		return this;
	}
	
	/**
	 * sets the unigrams for the given topic; 
	 * call {@link LDAHandler#getSelectedUnigrams()} on the returned
	 * LDAHandler to retrieve the list.
	 * @param selectedTopic
	 * @return
	 */
	public LDAHandler setUnigrams(Object selectedTopic){
		//-|===================================================
		//-|1. Get unigrams (probable words) for selected topic
		//-|2. Sort unigrams by probability
		//-|===================================================
		@SuppressWarnings("unchecked")
		List<DBObject> topicUnigrams = (List<DBObject>) model.getUnigrams(selectedTopic).get("words");
		Collections.sort(topicUnigrams, new TopicSortByProb());
		List<DBObject> allUnigrams = topicUnigrams;
		
		//-|==============================
		//-|Extract the best unigrams for
		//-|display and query expansion
		//-|==============================
		List<String> words = new ArrayList<String>(); //Store words for expansion
		DBObject obj;
		selectedUnigrams = new ArrayList<BasicDBObject>();
		for(int i=0; i<4; i++){
			obj = allUnigrams.get(i);
			selectedUnigrams.add((BasicDBObject)obj);
			words.add(obj.get("word").toString());
		}
		try{
			obj = allUnigrams.get(4);
			words.add(obj.get("word").toString());
		}catch(Exception e){
			System.err.println("Could NOT add the last (5th) term to the query expansion word list:");
			System.err.println("Only four (4) words will be used for query expansion.");
		}
		expansionWords.put((Integer)selectedTopic, words);
		return this;
	}
	
	/**
	 * returns the list of enriched topics
	 * @return
	 */
	public List<Integer> getEnrichedTopicSet(){
		return enrichedSet;
	}
	
	/**
	 * returns the a map of topics and associated expansion words
	 * @return
	 */
	public Map<Integer, List<String>> getAllExpansionWords(){
		return expansionWords;
	}
	
	/**
	 * returns a list of expansion words for the given topic
	 * @param topicID
	 * @return
	 */
	public List<String> getTopicExpansionWords(Integer topicID){
		return expansionWords.get(topicID);
	}
	
	/**
	 * returns a list of related topics
	 * @return
	 */
	public List<Integer> getRelatedTopicSet(){
		return relatedSet;
	}
	
	/**
	 * returns a list of ngrams
	 * @return
	 */
	public List<BasicDBObject> getSelectedNgrams(){
		return selectedNgrams;
	}
	
	/**
	 * returns a list of unigrams
	 * @return
	 */
	public List<BasicDBObject> getSelectedUnigrams(){
		return selectedUnigrams;
	}
	
	/**
	 * returns the topic threshold value
	 * @return
	 */
	public double getTopicThreshold(){
		return topicThreshold;
	}
	
	/**
	 * returns the model associated with the LDAHandler
	 * @return
	 */
	public LDAModel getModel(){
		return model;
	}
	
	/**
	 * filter the list of given topics by removing those that are less than the provided threshold
	 * @param topics the list of topic objects which consist of a key-value map (topic: id, prob: value)
	 * @param threshold value use to remove topics that fall short
	 * @param topicType enum value to use for distinguishing what type of topics are to be filtered (ENRICHED or RELATED)
	 * @return
	 */
	public List<Integer> filterTopics(List<DBObject> topics, double threshold, TopicType topicType){
		List<Integer> semcoArgs = new ArrayList<Integer>(); //Store topic IDs to retrieve the semco values
		
		//Getting topic ID's from topics list
		Iterator<DBObject> topicsIter = topics.iterator();
		switch(topicType){
			case ENRICHED: 
				while(topicsIter.hasNext()){
					semcoArgs.add((Integer)topicsIter.next().get("topic"));
				}
			case RELATED:
				while(topicsIter.hasNext()){
					semcoArgs.add((Integer)topicsIter.next().get("cotopic"));				
				}
		}
		//-|==========================================================
		//-|Retrieve the list of topicIDs in given list of topics 
		//-|	less than threshold (junk topics)
		//-|Remove junk topics from given list and return filter list
		//-|==========================================================
		DBCursor semcoCur = model.getTopicsLessThan(semcoArgs, threshold);
		while(semcoCur.hasNext()){
			DBObject obj = semcoCur.next();
			semcoArgs.remove(obj.get("topic"));
		}
		
		return semcoArgs;
		
	}
	//-|=============================================================
	//-|Private classes used for sorting data retrieved from model
	//-|=============================================================
	/**
	 * Comparator use to sort topic objects by probability
	 */
	private static class TopicSortByProb implements Comparator<DBObject>, Serializable{

		private static final long serialVersionUID = 1L;

		public int compare(DBObject o1, DBObject o2) {   	
	    	Double d1 = (Double)o1.get("prob");
	    	Double d2 = (Double)o2.get("prob");
	    	
	    	return (d1.compareTo(d2))*-1;
	    }
	}
	
	/**
	 * Comparator use to sort ngram objects by score 
	 */
	private static class NgramSortByScore implements Comparator<BasicDBObject>, Serializable{
		
		private static final long serialVersionUID = 2L;

		public int compare(BasicDBObject o1, BasicDBObject o2) {
	    	Double d1 = o1.getDouble("score");
	    	Double d2 = o2.getDouble("score");
	    	
	    	return (d1.compareTo(d2))*-1;
	    }
	}
	
	/**
	 * Comparator use to sort ngram objects by size
	 */
	private static class NgramSortBySize implements Comparator<BasicDBObject>, Serializable{
		
		private static final long serialVersionUID = 3L;
		
		public int compare(BasicDBObject o1, BasicDBObject o2) {
	    	Integer i1 = o1.getInt("size");
	    	Integer i2 = o2.getInt("size");
	    	
	    	return (i1.compareTo(i2))*-1;
	    	 
	    }
	}
	
	/**
	 * Generic comparator list use to enable sorting using multiple comparators 
	 */
	private static class MultiComparator<T> implements Comparator<T>, Serializable {
	    
		private static final long serialVersionUID = 4L;
		
		private List<Comparator<T>> comparators;

	    public MultiComparator(List<Comparator<T>> comparators) {
	        this.comparators = comparators;
	    }

	    public int compare(T o1, T o2) {
	        for (Comparator<T> comparator : comparators) {
	            int comparison = comparator.compare(o1, o2);
	            if (comparison != 0) return comparison;
	        }
	        return 0;
	    }
	}
}
