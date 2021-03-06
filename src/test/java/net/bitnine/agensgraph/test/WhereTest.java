package net.bitnine.agensgraph.test;

import junit.framework.TestCase;
import net.bitnine.agensgraph.graph.Vertex;
import net.bitnine.agensgraph.graph.property.JsonArray;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class WhereTest extends TestCase {

    private Connection con;
    private Statement st;

    public void setUp() throws Exception {
        con = TestUtil.openDB();
        con.setAutoCommit(true);
        st = con.createStatement();
        try {
            dropSchema();
        }
        catch (Exception ignored) {}
        st.execute("create vlabel person");
        create();
    }

    private void create() throws Exception {
        st.execute("CREATE (:person '{ \"name\": \"Emil\", \"from\": \"Sweden\", \"klout\": 99 }')");
    }

    private void dropSchema() throws Exception {
        st.execute("drop vlabel person");
    }

    public void tearDown() throws Exception {
        dropSchema();
        st.close();
        TestUtil.closeDB(con);
    }

    public void testWhere() throws Exception {
        ResultSet rs = st.executeQuery("MATCH (ee:person) WHERE (ee).name = to_jsonb('Emil'::text) return ee");
        while (rs.next()) {
            Vertex n = (Vertex)rs.getObject("ee");
            assertEquals(99, (int)n.getProperty().getInt("klout"));
        }
        rs = st.executeQuery("MATCH (ee:person) WHERE (ee).klout = to_jsonb(99::int) return ee");
        while (rs.next()) {
            Vertex n = (Vertex)rs.getObject("ee");
            assertEquals(99, (int)n.getProperty().getInt("klout"));
        }
        rs = st.executeQuery("MATCH (ee:person) WHERE (ee).from = to_jsonb('Korea'::text) return ee");
        assertFalse(rs.next());
        rs.close();
    }
}
