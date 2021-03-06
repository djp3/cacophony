package edu.uci.ics.luci.cacophony.directory.nodelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;
import edu.uci.ics.luci.utility.database.DBConnection;
import edu.uci.ics.luci.utility.database.LUCIDBConnectionPool;

public class MySQLTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		while(Globals.getGlobals() != null){
			try{
				Thread.sleep(1000);
			}
			catch(InterruptedException e){
			}
		}
		Globals.setGlobals(new GlobalsTest());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Globals.getGlobals().setQuitting(true);
		Globals.setGlobals(null);
	}

	private LUCIDBConnectionPool odbcp = null;
	private MySQL m = null;

	@Before
	public void setUp() throws Exception {
		try{
			odbcp = new LUCIDBConnectionPool(Globals.getGlobals().getDatabaseDomain(), "testDatabase", "testuser", "testuserPassword",null,0);
		}
		catch(Exception e){
			fail("Couldn't make a pool\n"+e);
		}
	}
	
	@After
	public void tearDown() throws Exception {
		if(odbcp != null){
			odbcp.shutdown();
		}
	}

	@Test
	public void testInit() {
		
		/* Create a table to work with */
		DBConnection c = odbcp.getConnection();
		Statement s = null;
		try {
			final String tableName = "testData";
			
			s = (Statement) c.createStatement();
			
			try {
				s.executeUpdate("DROP TABLE IF EXISTS "+tableName);
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
				fail("SQL Exception");
			}
			
			int count = 0;
			
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

			String options = "{ server_address:\"localhost\","+
								"user:\"testuser\","+
								"password:\"testuserPassword\","+
								"database:\"testDatabase\","+
								"listViewQuery:\"SELECT id AS ID, name AS NAME , 1 AS CALL_COUNT from "+tableName+" limit 1000\","+
								"mapViewQuery:\"SELECT id AS ID, 1 AS X, 2 AS Y, 1 AS MAP_WEIGHT from "+tableName+" limit 1000\","+
								"configurationQuery:\"SELECT configuration AS CONFIGURATION from configurations\"}";

			JSONObject jsonObject = null;
			try {
				jsonObject = (JSONObject) JSONValue.parse(options);
			} catch (ClassCastException e) {
				fail(e.toString());
			}
			m = new MySQL();
			m.init(jsonObject);
			List<MetaCNode> map = m.loadNodeList();
			m = null;
			assertEquals(4,map.size());
			
			
			/*Clean up*/
			try {
				s.executeUpdate("DROP TABLE IF EXISTS "+tableName);
			} catch (SQLException e) {
				e.printStackTrace();
				fail("SQL Exception");
			}
			
		} catch (SQLException e) {
			fail("Shouldn't fail");
		}
		finally{
			try {
				if(s!= null){
					s.close();
				}
			} catch (SQLException e) {
				fail("Shouldn't fail");
			}
			try {
				if(c != null){
					c.close();
				}
			} catch (SQLException e) {
				fail("Shouldn't fail");
			}
		}
		
	}

}
