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
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class MongoInstanceTest extends TestCase {

	public void testGetDatabaseNames() {
		MongoInstance mongo1 = new MongoInstance();
		MongoInstance mongo2 = new MongoInstance("127.0.0.1");
		assertEquals(mongo1.getServerAddress(), mongo2.getServerAddress());
		
		assertEquals(Arrays.asList("trecla", "admin", "local"), mongo1.getDatabaseNames());
	}

	public void testGetCollectionNames() {
		MongoInstance mongo = new MongoInstance("127.0.0.1", "trecla");
		Set<String> collections = new HashSet<String>();
		collections.addAll(Arrays.asList("ngram", "phi", "raw", "related", "sample", "semco", "stop", "system.indexes", "theta", "titles"));
		
		assertEquals(collections, mongo.getCollectionNames());
	}

	public void testUseDB() {
		MongoInstance mongo1 = new MongoInstance("127.0.0.1", "trecla");
		MongoInstance mongo2 = new MongoInstance("127.0.0.1");
		
		mongo2.useDB("trecla");
		assertEquals(mongo1.getDB(), mongo2.getDB());
		
		assertEquals(mongo1.getCollectionNames(), mongo2.getCollectionNames());
	}

	public void testUseCollection() {
		MongoInstance mongo1 = new MongoInstance("127.0.0.1", "trecla");
		MongoInstance mongo2 = new MongoInstance("127.0.0.1", 27017, "trecla");
		
		mongo1.useCollection("ngram");
		mongo2.useCollection("phi");
		assertNotSame(mongo1.getCollection(), mongo2.getCollection());
		
		mongo2.useCollection("ngram");
		assertSame(mongo1.getCollection(), mongo2.getCollection());
	}

}
