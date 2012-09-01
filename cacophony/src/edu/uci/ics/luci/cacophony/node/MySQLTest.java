package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.Instances;

import com.quub.Globals;
import com.quub.GlobalsTest;
import com.quub.database.DBConnection;
import com.quub.database.QuubDBConnectionPool;

public class MySQLTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Globals.setGlobals(new GlobalsTest());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private QuubDBConnectionPool odbcp = null;
	private MySQL m = null;

	@Before
	public void setUp() throws Exception {
		try{
			odbcp = new QuubDBConnectionPool(Globals.getGlobals().getDatabaseDomain(), "testDatabase", "testuser", "testuserPassword",null,0);
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
								"historyQuery:\"SELECT cast(id as signed) AS ID, name AS NAME from "+tableName+" limit 1000\"}";

			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(options);
			} catch (JSONException e) {
				fail(e.toString());
			}
			m = new MySQL();
			m.init(jsonObject);
			Instances i = m.loadCNodeHistory();
			m = null;
			assertTrue(i.numInstances() > 0);

			
			
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
