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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;
import com.mongodb.ServerAddress;
/**
 * 
 * @author Kevin R. Lawrence
 * 
 * Provides a database connection to Mongodb.
 * Download MongoDB at: http://www.mongodb.org/downloads
 * 
 * Set up instructions can be found at:
 * http://www.mongodb.org/display/DOCS/Quickstart
 * 
 * After installation, a data directory must be created.
 * This location is used by MongoDB to store data by default:
 * C:\> mkdir \data
 * C:\> mkdir \data\db
 * 
 * To connect to the server, run mongod.exe for startup.
 * You may specify the database location using option --dbpath
 * eg. C:\mongodb\bin>mongod --dbpath ../../data/db/topicModel
 *
 * You can now use MongoInstance to connect to MongoDB server:
 * 
 * <blockquote><pre>
 * //All connects to local database running on default port
 * MongoInstance mongo1 = new MongoInstance();
 * MongoInstance mongo2 = new MongoInstance("127.0.0.1");
 * MongoInstance mongo3 = new MongoInstance("127.0.0.1", "topicModel");//specifies database name
 * MongoInstance mongo4 = new MongoInstance("127.0.0.1", 27017);
 * MongoInstance mongo5 = new MongoInstance("127.0.0.1", 27017, "topicModel");
 * </pre></blockquote>
 * 
 * MongoInstance wraps a single instance of Mongo and uses
 * Mongo.Holder as a static place to hold that instance. 
 * 
 * MongoInstance also wraps a DB and DBCollection instance.
 * You may switch both databases and collections once connected to server.
 * 
 * Note: Mongo class may be used instead of MongoInstance, if desired.
 * See: http://www.mongodb.org/display/DOCS/Java+Tutorial#JavaTutorial-MakingAConnection, for instructions.
 */
public class MongoInstance{
	private final String URIprefix = "mongodb://";
	private static Mongo mongo = null;
	private static final Mongo.Holder holder = new Mongo.Holder();
	private DB db = null;
	private DBCollection coll = null;
	
	/**
	 * creates a MongoInstance instance and
	 * calls {@link MongoInstance#getMongo(String)} with localhost address to instantiate a
	 * Mongo instance based on a (single) mongo node (localhost, default port)
	 */
	public MongoInstance(){
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			MongoInstance.getMongo(URIprefix+localHost.getHostAddress());
		} catch (UnknownHostException e) {
			System.err.println("localhost cannot be resolved: "+e);
		}
		
	}
	/**
	 * creates a MongoInstance instance and
	 * calls {@link MongoInstance#getMongo(String)} with given host address to instantiate a
	 * Mongo instance based on a (single) mongo node (default port)
	 * @param host server to connect to
	 */
	public MongoInstance(String host){
		MongoInstance.getMongo(URIprefix+host);
	}
	
	/**
	 * creates a MongoInstance instance,
	 * calls {@link MongoInstance#getMongo(String)} with given host address used to instantiate a
	 * Mongo instance based on a (single) mongo node (default port), and  
	 * automatically connects to the given database name
	 * @param host server to connect to
	 * @param dbname the database name that specifies location for retrieving collections
	 */
	public MongoInstance(String host, String dbname){
		db = MongoInstance.getMongo(URIprefix+host).getDB(dbname);
	}
	
	/**
	 * creates a MongoInstance instance and
	 * calls {@link MongoInstance#getMongo(String)} with given host address and port number used to instantiate a
	 * Mongo instance based on a (single) mongo node
	 * @param host server to connect to
	 * @param port the port on which the database is running
	 */
	public MongoInstance(String host, int port){
		String hostAddress;
		try {
			hostAddress = (new ServerAddress(host, port)).toString();
			MongoInstance.getMongo(URIprefix+hostAddress);
		} catch (UnknownHostException e) {
			System.err.println("Host cannot be resolved: "+e);
		}
		
	}
	
	/**
	 * creates a MongoInstance instance,
	 * calls {@link MongoInstance#getMongo(String)} with given host address and port number used to instantiate a
	 * Mongo instance based on a (single) mongo node, and
	 * automatically connects to the given database name
	 * @param host server to connect to
	 * @param port the port on which the database is running
	 * @param dbname the database name that specifies location for retrieving collections
	 */
	public MongoInstance(String host, int port, String dbname){
		String hostAddress;
		try {
			hostAddress = (new ServerAddress(host, port)).toString();
			db = MongoInstance.getMongo(URIprefix+hostAddress).getDB(dbname);
		} catch (UnknownHostException e) {
			System.err.println("Could NOT connect to Mongo: check hostname provided");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * employs a singleton approach that creates a Mongo described by a URI;
	 * returns a mongo object
	 * @param host server to connect to
	 * @return 
	 */
	private static Mongo getMongo(String host){
		 if ( mongo == null ) { 
			 try { 
				 MongoURI mongoURI = new MongoURI(host);
				 mongo = holder.connect(mongoURI);
			 } catch ( UnknownHostException e ) { 
				 System.err.println("Database host cannot be resolved: "+ e); 
			 } catch (MongoException e){
				 System.err.println("A problem occured connecting to server: "+ e);
			 }
		  } 
		return mongo;
	}
	/**
	 * gets a list of all database names present on the server
	 * @return
	 */
	public List<String> getDatabaseNames(){
		return mongo.getDatabaseNames();
	}
	
	/**
	 * gets a list of all collection names present on the database in use
	 * @return
	 * @throws NullPointerException if database to use has not been specified
	 */
	public Set<String> getCollectionNames() throws NullPointerException{
		if(db != null)
			return db.getCollectionNames();
		else{
			System.err.println("Database not set!\nSet database using method useDB(String dbname)");
			throw new NullPointerException();
		}
	}
	
	/**
	 * sets the database to use on server and returns MongoInstance object
	 * @param dbname
	 * @return
	 */
	public MongoInstance useDB(String dbname){
		if(mongo != null)
			db = mongo.getDB(dbname);
		
		return this;
	}
	
	/**
	 * sets the collection to use in database and returns that DBCollection object
	 * @param name
	 * @return
	 */
	public DBCollection useCollection(String name){
		if(db != null)
			coll = db.getCollection(name);
		else{
			System.err.println("Database not set!\nSet database using method useDB(String dbname)");
			throw new NullPointerException();
		}
		
		return coll;
	}
	/**
	 * returns the database object in use
	 * @return
	 */
	public DB getDB(){
		return db;
	}
	
	/**
	 * returns the DBCollection object in use
	 * @return
	 */
	public DBCollection getCollection(){
		return coll;
	}
	/**
	 * closes the underlying connector for mongo server, which in turn closes all open connections.
	 * Once called, this mongo instance caa no longer be used
	 */
	public void closeMongo(){
		mongo.close();
	}
	
	public ServerAddress getServerAddress(){
		return mongo.getAddress();
	}

}
