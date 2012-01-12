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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import com.mongodb.BasicDBObject;

import junit.framework.TestCase;

public class IrisTest extends TestCase {
	Iris iris;
	
	protected void setUp(){
		LDAModel model =  new LDAModel(new MongoInstance("127.0.0.1", "trecla"));
		LDAHandler handler = new LDAHandler(model);
		iris = new Iris(handler);
		
		DisMaxQuery query = new DisMaxQuery("environmental policy");
	
		iris.setLatentTopics(getResults(query));
		iris.setLatentTopicsNgrams();
	}
	
	public void testSetLatentTopics() {
		assertEquals(Arrays.asList(134, 474, 391, 81, 126, 247, 450, 15, 298, 154, 436, 68), iris.getLatentTopics());
	}

	
	@SuppressWarnings("unchecked")
	public void testSetLatentTopicsNgrams() {
		Map<Integer, List<BasicDBObject>> topicNgrams = new LinkedHashMap<Integer, List<BasicDBObject>>();

		InputStream topicNgramsFile;
		try {			
			topicNgramsFile = new FileInputStream("topicNgramsTestFile.txt");
			ObjectInput oi = new ObjectInputStream(topicNgramsFile);
			Object temp = oi.readObject();
			topicNgrams = (Map<Integer, List<BasicDBObject>>) temp;
			oi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		assertEquals(topicNgrams, iris.getLatentTopicNgrams());
	}
	
	public SolrDocumentList getResults(DisMaxQuery query){
		SolrServer server = null;
		QueryResponse response = null;
		try {
			server = new CommonsHttpSolrServer("http://localhost:8983/solr/");
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return response.getResults();
	}

}
