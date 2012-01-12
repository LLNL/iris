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

import gov.llnl.iscr.iris.MongoInstance;

import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.SolrDocument;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
/**
 * 
 * This class provides connections to the various collections that
 * make up the LDA topic model. It provides limited retrieval methods
 * for accessing the data in the model.
 * <p> Typical invocations sequence are:
 * <blockquote><pre>
 * MongoInstance mongo1 = new MongoInstance("127.0.0.1");
 * mongo.useDB("topicModel");
 * LDAModel model1 = new LDAModel(mongo1);
 * 
 * MongoInstance mongo2 = new MongoInstance("127.0.0.1", "topicModel");
 * LDAModel model2 = new LDAModel(mongo2);
 * 
 * LDAModel model3 = new LDAModel(new MongoInstance("127.0.0.1", 27017, "topicModel"));
 * </pre></blockquote>
 * 
 * This class retrieves data used by {@link LDAHandler} to select topics and associated ngrams given a query result list.
 *
 */
public class LDAModel {
	private final DBCollection phi;
	private final DBCollection theta;
	private final DBCollection semco;
	private final DBCollection ngram;
	private final DBCollection related;
	
	/**
	 * creates an instance of LDAModel and initialize the required collections used by the model
	 * @param mongoInstance object that provides the connection to the necessary database
	 */
	public LDAModel(MongoInstance mongoInstance){
		if(mongoInstance.getDB().collectionExists("phi"))
			phi = mongoInstance.useCollection("phi");
		else{
			System.out.println("phi set to NULL!\n Check database for required LDA model collections.");
			phi=null;
		}
		
		if(mongoInstance.getDB().collectionExists("theta"))
			theta = mongoInstance.useCollection("theta");
		else{
			System.out.println("theta set to NULL!\n Check database for required LDA model collections.");
			theta=null;
		}

		if(mongoInstance.getDB().collectionExists("semco"))
			semco = mongoInstance.useCollection("semco");
		else{
			System.out.println("semco set to NULL!\n Check database for required LDA model collections.");
			semco=null;
		}
		
		if(mongoInstance.getDB().collectionExists("ngram"))
			ngram = mongoInstance.useCollection("ngram");
		else{
			System.out.println("ngram set to NULL!\n Check database for required LDA model collections.");
			ngram=null;
		}
		
		if(mongoInstance.getDB().collectionExists("related"))
			related = mongoInstance.useCollection("related");
		else{
			System.out.println("ngram set to NULL!\n Check database for required LDA model collections.");
			related=null;
		}
	}
	
	/**
	 * returns the semantic coherence scores (only) for all topics in the model 
	 * @return
	 */
	public DBCursor getSemcoValues(){
		BasicDBObject query = new BasicDBObject();
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0);
		query2.put("words", 0);
		query2.put("topic", 0);
		
		BasicDBObject orderBySemco = new BasicDBObject("semco", 1);
		DBCursor semcoCur = semco.find(query, query2).sort(orderBySemco); //Sort by semco value ascending
		return semcoCur;
		
	}
	
	/**
	 * returns the semantic coherence scores for the given topics
	 * @param topicIDs a list of ID values for topics
	 * @return
	 */
	public DBCursor getSemcoValues(List<Integer> topicIDs){
		BasicDBObject query = new BasicDBObject();
		query.put("topic", new BasicDBObject("$in",topicIDs));
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0);
		query2.put("words", 0);
		
		BasicDBObject orderBySemco = new BasicDBObject("semco", 1);
		DBCursor semcoCur = semco.find(query, query2).sort(orderBySemco); //Sort by semco value ascending
		return semcoCur;
		
	}
	/**
	 * returns an iterator object that contains a list of key-value mappings of all topics and associated semantic coherence scores
	 * @return
	 */
	public DBCursor getTopicSemcoValues(){
		BasicDBObject query = new BasicDBObject();
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0);
		query2.put("words", 0);
		
		BasicDBObject orderBySemco = new BasicDBObject("semco", -1);
		DBCursor semcoCur = semco.find(query, query2).sort(orderBySemco); //Sort by semco value ascending
		return semcoCur;
		
	}
	/**
	 * returns all topics associated with given document in a {@link DBObject} that contains key-value maps of topics and probabilities.
	 * @param doc a single document taken from query results
	 * @return
	 */
	//@SuppressWarnings("unchecked")
	public DBObject getTopics(Object docid){
		BasicDBObject query = new BasicDBObject();
		query.put("document", docid);
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0);
		query2.put("topics", 1);
		DBObject topicsObj = theta.findOne(query, query2);
		
		return topicsObj;
	}
	
	/**
	 * returns a list of iterator objects containing key-value maps of the related topics and probabilities for each topic in the given list 
	 * @param enrichedSet
	 * @return
	 */
	public List<DBCursor> getRelatedTopics(List<Integer> enrichedSet){
		BasicDBObject orderByCovar = new BasicDBObject("covar", -1);
		BasicDBObject query = new BasicDBObject("_id", 0);
		DBCursor cur;
		List<DBCursor> relatedTopicsCursors = new ArrayList<DBCursor>();
		for(Integer topicID : enrichedSet){
			cur = related.find(new BasicDBObject("topic", topicID), query).sort(orderByCovar);
			relatedTopicsCursors.add(cur.copy());
		}
		
		return relatedTopicsCursors;
	}
	
	/**
	 * returns all the ngrams for the given topic
	 * @param selectedTopic
	 * @return
	 */
	public DBObject getNgrams(Object selectedTopic){
		BasicDBObject query = new BasicDBObject();
		query.put("topic", selectedTopic);
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0);
		query2.put("topic", 0);
		query2.put("ngrams.count", 0);
		DBObject ngramObj = ngram.findOne(query, query2);
		
		return ngramObj;
	}
	
	/**
	 * returns all the unigrams for the given topic
	 * @param selectedTopic
	 * @return
	 */
	public DBObject getUnigrams(Object selectedTopic){
		BasicDBObject query = new BasicDBObject();
		query.put("topic", selectedTopic);
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0); 
		query2.put("topic", 0);
		DBObject phiObj = phi.findOne(query, query2);
		
		return phiObj;
	}
	
	/**
	 * returns the topics within the given list of topic IDs less than the given threshold argument
	 * @param semcoArgs
	 * @param threshold
	 * @return
	 */
	public DBCursor getTopicsLessThan(List<Integer> semcoArgs, double threshold){
		//Building queries for retrieving topic semco values less than threshold
		BasicDBObject query = new BasicDBObject();
		query.put("topic", new BasicDBObject("$in",semcoArgs));
		query.append("semco", new BasicDBObject("$lt",threshold));
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0); 
		query2.put("words", 0);
				
		return semco.find(query, query2);
	}
	
	/**
	 * returns the topics within the given list of topic IDs greater than the given threshold argument
	 * @param semcoArgs
	 * @param threshold
	 * @return
	 */
	public DBCursor getTopicsGreaterThan(List<Integer> semcoArgs, double threshold){
		//Building queries for retrieving topic semco values greater than threshold
		BasicDBObject query = new BasicDBObject();
		query.put("topic", new BasicDBObject("$in",semcoArgs));
		query.append("semco", new BasicDBObject("$gt",threshold));
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0); 
		query2.put("words", 0);
				
		return semco.find(query, query2);
	}
	/**
	 * returns the topics less than the given threshold argument that are associated with the given document
	 * @param doc
	 * @param threshold
	 * @return
	 */
	public DBCursor getTopicsLessThan(SolrDocument doc, double threshold){
		@SuppressWarnings("unchecked")
		List<DBObject> topics = (List<DBObject>) this.getTopics(doc).get("topics");
		
		List<Integer> semcoArgs = new ArrayList<Integer>();
		for(DBObject topic : topics){
			semcoArgs.add((Integer)topic.get("topic"));
		}
		
		//Building queries for retrieving topic semco values less than threshold
		BasicDBObject query = new BasicDBObject();
		query.put("topic", new BasicDBObject("$in",semcoArgs));
		query.append("semco", new BasicDBObject("$lt",threshold));
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0); 
		query2.put("words", 0);
		
		return semco.find(query, query2);		
	}
	/**
	 * returns the topics greater than the given threshold argument that are associated with the given document
	 * @param doc
	 * @param threshold
	 * @return
	 */
	public DBCursor getTopicsGreaterThan(SolrDocument doc, double threshold){
		@SuppressWarnings("unchecked")
		List<DBObject> topics = (List<DBObject>) this.getTopics(doc).get("topics");
		
		List<Integer> semcoArgs = new ArrayList<Integer>();
		for(DBObject topic : topics){
			semcoArgs.add((Integer)topic.get("topic"));
		}
		
		//Building queries for retrieving topic semco values less than threshold
		BasicDBObject query = new BasicDBObject();
		query.put("topic", new BasicDBObject("$in",semcoArgs));
		query.append("semco", new BasicDBObject("$gt",threshold));
		BasicDBObject query2 = new BasicDBObject();
		query2.put("_id", 0); 
		query2.put("words", 0);
		
		return semco.find(query, query2);		
	}
	
}
