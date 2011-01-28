package com.quub.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quub.Globals;

public class DBConnectionTest {

	//private static final boolean testing = true;
	private static Random r = new Random();

	static int count = 0;

	private static String _databaseDomain = null;
	static QuubDBConnectionPool odbcp = null;

	private static Globals _globals = null;
	public static Globals getGlobals() {
		return _globals;
	}
	
	public static void setGlobals(Globals g) {
		_globals = g;
	}

	private static transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(DBConnectionTest.class);
		}
		return log;
	}

	public static String getDatabaseDomain() {
		if(_databaseDomain == null){
			_databaseDomain = getGlobals().getDefaultDatabaseDomain();
		}
		return _databaseDomain;
	}

	public void set_databaseDomain(String domain) {
		_databaseDomain = domain;
	}

	@BeforeClass
	public static void setUp() throws Exception {
		try{
			setGlobals(Globals.getGlobals());
			odbcp = new QuubDBConnectionPool(getGlobals(), getDatabaseDomain(), "database Domain", "testuser", "testuserPassword",null,0);
		}
		catch(Exception e){
			fail("Couldn't make a pool");
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if(odbcp != null){
			odbcp.shutdown();
		}
	}


	private void statementBattery(Statement s, String tableName) {
		if (s != null) {
			int count = -1;

			try {
				s.executeUpdate("DROP TABLE IF EXISTS " + tableName);
			} catch (SQLException e) {
				e.printStackTrace();
				fail("SQL Exception");
			}

			try {
				s.executeUpdate("CREATE TABLE " + tableName + " ("
						+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
						+ "PRIMARY KEY (id),"
						+ "name CHAR(40), category CHAR(40))");
			} catch (SQLException e) {
				e.printStackTrace();
				fail("SQL Exception");
			}

			try {
				count = s.executeUpdate("INSERT INTO " + tableName
						+ " (name, category)" + " VALUES"
						+ "('snake', 'reptile')," + "('frog', 'amphibian'),"
						+ "('tuna', 'fish')," + "('racoon', 'mammal')");
			} catch (SQLException e) {
				e.printStackTrace();
				fail("SQL Exception");
			}

			assertTrue(count == 4);

			count = 0;
			try {
				ResultSet rs = s.executeQuery("SELECT * FROM " + tableName
						+ " WHERE name = 'tuna';");
				while (rs.next()) {
					count++;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				fail("SQL Exception");
			}

			try {
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
				fail("SQL Exception");
			}
			assertTrue(count == 1);
		}
	}

	public class Tester implements Runnable {

		private QuubDBConnectionPool pool = null;
		DBConnection c = null;

		Tester(QuubDBConnectionPool pool) {
			this.pool = pool;
		}

		DBConnection getDBConnection() {
			return c;
		}

		@Override
		public void run() {

			synchronized (this) {
				try {
					long delay = r.nextInt(100) + 1;/* We don't want to delay 0! */
					wait(delay); /* Pause so everything isn't synchronized */
				} catch (InterruptedException e1) {
				}
			}

			c = pool.getConnection();

			Statement s;
			try {
				s = (Statement) c.createStatement();
				statementBattery(s, "testtable" + (count++));
			} catch (SQLException e) {
				getLog().error(e.toString());
				fail("Shouldn't fail");
			}
			finally{
				try {
					c.close();
				} catch (SQLException e) {
					getLog().error(e.toString());
					fail("Shouldn't fail");
				}
			}
		}
	}

	@Test
	public void testDBConnect() {
		long tests = QuubDBConnectionPool.getTotalRequests();

		List<Thread> list = new ArrayList<Thread>();

		int number = 100;
		int count = 0;

		getLog().info( "Creating and testing " + number + " connection operations. "+tests+" connections have been made already");

		for (int j = 0; j < 10; j++) {

			for (int i = 0; i < number / 10; i++) {
				Thread t = new Thread(new Tester(odbcp));
				count++;
				t.setName("Test thread:" + i);
				t.setDaemon(false);
				list.add(t);
				t.start();
			}

			for (Thread t : list) {
				System.out.print(".");
				try {
					t.join();
				} catch (InterruptedException e) {
					getLog().error(e.toString());
					fail("This shouldn't happen");
				}
			}

			System.out.println(""+count);

			list.clear();
		}

		assertEquals(number, (QuubDBConnectionPool.getTotalRequests() - tests));

	}
	
	
	@Test
	public void testWarmUpAndHotStandby() {
		int number = 3;
		QuubDBConnectionPool testodbcp = new QuubDBConnectionPool(getGlobals(), getDatabaseDomain(), "database Domain", "testuser", "testuserPassword",number,2*number);


		assertEquals(Integer.valueOf(2*number),testodbcp.getPoolSize());

		if(testodbcp != null){
			testodbcp.shutdown();
		}
		
		testodbcp = new QuubDBConnectionPool(getGlobals(), getDatabaseDomain(), "database Domain", "testuser", "testuserPassword",2*number,number);

		assertEquals(Integer.valueOf(2*number),testodbcp.getPoolSize());

		if(testodbcp != null){
			testodbcp.shutdown();
		}
		
		testodbcp = new QuubDBConnectionPool(getGlobals(), getDatabaseDomain(), "database Domain", "testuser", "testuserPassword",number,number);

		assertEquals(Integer.valueOf(number),testodbcp.getPoolSize());

		if(testodbcp != null){
			testodbcp.shutdown();
		}
		
		testodbcp = new QuubDBConnectionPool(getGlobals(), getDatabaseDomain(), "database Domain", "testuser", "testuserPassword",0,0);

		assertEquals(Integer.valueOf(0),testodbcp.getPoolSize());

		if(testodbcp != null){
			testodbcp.shutdown();
		}
	}

	
	@Test
	public void testReaping() {

		long tests = QuubDBConnectionPool.getTotalRequests();

		Tester x = new Tester(odbcp);
		Thread t = new Thread(x);
		t.setName("Test thread");
		t.setDaemon(false);
		t.start();

		try {
			t.join();
		} catch (InterruptedException e1) {
		}

		/*
		 * Lease the wrapper and close the underlying connection so that the
		 * reaper has something to reap
		 */
		try {
			assertTrue(x.getDBConnection().lease());
			x.getDBConnection().getConnection().close();
			x.getDBConnection().setConnection(null);
		} catch (SQLException e) {
			getLog().error(e.toString());
			fail("This should fail");
		}

		assertEquals(1,QuubDBConnectionPool.getTotalRequests() - tests);

		try {
			odbcp.shutdown();
		} catch (Exception e) {
			fail("This shouldn't throw an exception");
		}
	}

}
