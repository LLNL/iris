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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import junit.framework.TestCase;

public class LDAHandlerTest extends TestCase {
	LDAModel model;
	LDAHandler handler;
	protected void setUp(){
		model =  new LDAModel(new MongoInstance("127.0.0.1", "trecla"));
		handler = new LDAHandler(model);
		
	}
	public void testSetTopicThresholdFloat() {
		DBCursor cur = model.getSemcoValues();
		assertEquals((Double)cur.toArray().get(124).get("semco"), handler.setTopicThreshold(0.25F).getTopicThreshold());

	}

	public void testSetTopicThresholdDouble() {
		handler.setTopicThreshold(-50.0);
		assertEquals(-50.0, handler.getTopicThreshold());
	}
	
	
	public void testSetEnrichedTopicSet() {
		//Using default threshold for test: -100.0
		Object docid1 = "LA092590-0030";
		Object docid2 = "LA022190-0160";
		handler.setEnrichedTopicSet(Arrays.asList(docid1, docid2));
		
		assertEquals(Arrays.asList(134, 474, 391, 81), handler.getEnrichedTopicSet());
	}
	
	public void testSetRelatedTopicSet() {
		//Using default threshold for test: -100.0
		Object docid1 = "LA092590-0030";
		Object docid2 = "LA022190-0160";
		handler.setEnrichedTopicSet(Arrays.asList(docid1, docid2));
		
		handler.setRelatedTopicSet();
		assertEquals(Arrays.asList(126, 247, 450, 15, 298, 154, 436, 68), handler.getRelatedTopicSet());
	}

	public void testSetNgrams() {
		Object selectedTopic = 134;
		handler.setNgrams(selectedTopic);
		
		BasicDBObject ngram1 = new BasicDBObject("size", 2).append("score", 121.1848328909918);
		ngram1.append("ngram", "public opinion");
		BasicDBObject ngram2 = new BasicDBObject("size", 2).append("score", 118.03008258436529);
		ngram2.append("ngram", "conflict interest");
		
		assertEquals(Arrays.asList(ngram1, ngram2), handler.getSelectedNgrams());
		
	}

	public void testSetUnigrams() {
		Object selectedTopic = 134;
		BasicDBObject unigram1 = new BasicDBObject("word", "issue").append("prob", 3572.0155588668767);
		BasicDBObject unigram2 = new BasicDBObject("word", "policy").append("prob", 3147.0155588668767);
		BasicDBObject unigram3 = new BasicDBObject("word", "public").append("prob", 1792.0155588668767);
		BasicDBObject unigram4 = new BasicDBObject("word", "issues").append("prob", 1634.0155588668767);
		handler.setUnigrams(selectedTopic);
		assertEquals(Arrays.asList(unigram1, unigram2, unigram3, unigram4), handler.getSelectedUnigrams());
	}

}
