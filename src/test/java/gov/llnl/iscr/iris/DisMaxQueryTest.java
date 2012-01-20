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

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class DisMaxQueryTest extends TestCase {
	DisMaxQuery query;
	
	protected void setUp(){
		query =  new DisMaxQuery("environmental policy");
		query.setQueryField("text");
		query.setBoostQuery("impact");
	}
	
	public void testAddBoostQueryString() {
		query.addBoostQuery("government");
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0&bq=impact^1.0 government^1.0";
		
		assertEquals(qstr.toString(), parseQuery());
	}

	public void testAddBoostQueryStringFloat() {
		query.addBoostQuery("united", 0.5F);
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0&bq=impact^1.0 united^0.5";
		
		assertEquals(qstr.toString(), parseQuery());
	}

	public void testAddBoostQueryStringString() {
		query.addBoostQuery("title", "pollution");
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0&bq=impact^1.0 title:pollution^1.0";
		
		assertEquals(qstr.toString(), parseQuery());
	}

	public void testAddBoostQueryStringStringFloat() {
		query.addBoostQuery("title", "pollution", 5.0F);
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0&bq=impact^1.0 title:pollution^5.0";
		
		assertEquals(qstr.toString(), parseQuery());
	}

	public void testAddBoostQueryListOfString() {
		query.addBoostQuery(Arrays.asList("united", "states", "damage"));
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0&bq=impact^1.0 united^1.0 states^1.0 damage^1.0";
		
		assertEquals(qstr.toString(), parseQuery());
	}
	
	public void testAddBoostQueryStringListOfString() {
		query.addBoostQuery("text", Arrays.asList("united", "states", "damage"));
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0&bq=impact^1.0 text:united^1.0 states^1.0 damage^1.0";
		
		assertEquals(qstr.toString(), parseQuery());
	}
	
	public void testAddBoostQueryMapOfStringFloat() {
		Map<String, Float> boostedQueries = new LinkedHashMap<String, Float>();
		boostedQueries.put("impact", 2.5F);
		boostedQueries.put("administration", 2.5F);
		
		query.addBoostQuery(boostedQueries);
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0&bq=impact^2.5 administration^2.5";
		
		assertEquals(qstr.toString(), parseQuery());
	}

	public void testAddBoostQueryStringMapOfStringFloat() {
		Map<String, Float> boostedQueries = new LinkedHashMap<String, Float>();
		boostedQueries.put("John", 0.5F);
		boostedQueries.put("Doe", 0.5F);
		
		query.addBoostQuery("author", boostedQueries);
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0&bq=impact^1.0 author:John^0.5 Doe^0.5";
		
		assertEquals(qstr.toString(), parseQuery());
	}

	public void testAddQueryFieldString() {
		query.addQueryField("title");
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0 title^1.0&bq=impact^1.0";
		
		assertEquals(qstr.toString(), parseQuery());
	}

	public void testAddQueryFieldStringFloat() {
		query.addQueryField("text", 5.0F);
		query.addQueryField("title", 1.5F);
		String qstr = "q=environmental policy&defType=dismax&qf=text^5.0 title^1.5&bq=impact^1.0";
		
		assertEquals(qstr.toString(), parseQuery());
	}

	public void testAddQueryFieldsListOfString() {
		query.addQueryFields(Arrays.asList("title", "author"));
		String qstr = "q=environmental policy&defType=dismax&qf=text^1.0 title^1.0 author^1.0&bq=impact^1.0";
		
		assertEquals(qstr.toString(), parseQuery());
	}

	public void testAddQueryFieldsMapOfStringFloat() {
		Map<String, Float> queryFields = new LinkedHashMap<String, Float>();
		queryFields.put("text", 20.0F);
		queryFields.put("title", 10.0F);
		queryFields.put("abstract", 5.0F);
		
		query.addQueryFields(queryFields);
		String qstr = "q=environmental policy&defType=dismax&qf=text^20.0 title^10.0 abstract^5.0&bq=impact^1.0";
		
		assertEquals(qstr.toString(), parseQuery());
	}
	
	private String parseQuery(){
		String dismaxquery = new String();
		try {
			dismaxquery = URLDecoder.decode(query.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return dismaxquery;
	}

}
