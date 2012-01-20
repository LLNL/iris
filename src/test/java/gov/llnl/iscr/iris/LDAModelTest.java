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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import junit.framework.TestCase;

public class LDAModelTest extends TestCase {

	public void testGetSemcoValuesListOfInteger() {
		
		LDAModel model =  new LDAModel(new MongoInstance("127.0.0.1", "trecla"));
		List<BasicDBObject> topicSemco = new ArrayList<BasicDBObject>();
		topicSemco.add(new BasicDBObject("semco", -110.82702588899582).append("topic", 1));
		topicSemco.add(new BasicDBObject("semco", -102.10882594585706).append("topic", 2));
		topicSemco.add(new BasicDBObject("semco", -86.46024146121823).append("topic", 0));
		
		DBCursor cur = model.getSemcoValues(Arrays.asList(0, 1, 2));
		
		for(int index = 0; index<3; index++){
			if(cur.hasNext())
				assertEquals(topicSemco.get(index), cur.next());
			else
				fail("Method under test returns less elements than expected");
		}
		if(cur.hasNext())
			fail("Method under test returns less elements than expected");
	}

	public void testGetTopics() {
		LDAModel model =  new LDAModel(new MongoInstance("127.0.0.1", "trecla"));

		List<BasicDBObject> topicProb = new ArrayList<BasicDBObject>();
		topicProb.add(new BasicDBObject("topic", 425).append("prob", 0.15789473684210525));
		topicProb.add(new BasicDBObject("topic", 488).append("prob", 0.15789473684210525));
		topicProb.add(new BasicDBObject("topic", 382).append("prob", 0.05263157894736842));
		topicProb.add(new BasicDBObject("topic", 184).append("prob", 0.3684210526315789));
		topicProb.add(new BasicDBObject("topic", 53).append("prob", 0.15789473684210525));
		topicProb.add(new BasicDBObject("topic", 194).append("prob", 0.10526315789473684));
		
		BasicDBObject topics = new BasicDBObject("topics", topicProb);
		
		assertEquals(topics, model.getTopics("LA021490-0001"));
	}
}
