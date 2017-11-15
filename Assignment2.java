import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Arrays;
// If you are looking for Java data structures, these are highly useful.
// Remember that an important part of your mark is for doing as much in SQL (not Java) as you can.
// Solutions that use only or mostly Java will not receive a high mark.
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
//import java.util.Set;
//import java.util.HashSet;
public class Assignment2 extends JDBCSubmission {
    
    public Assignment2() throws ClassNotFoundException {

        Class.forName("org.postgresql.Driver");
    }

    @Override
    public boolean connectDB(String url, String username, String password) {	
	try {
		connection = DriverManager.getConnection(url, username, password);
		return true;
	} 
	catch (SQLException e) {
		System.err.println("SQL Exception." + "<Message>:" + e.getMessage());
		return false;		
	}
	
    }

    @Override
    public boolean disconnectDB() {
	try {
		connection.close();
		return true;
	}
	catch (SQLException e) {
		System.err.println("SQL Exception." + "<Message>:" + e.getMessage());
		return false;
	}
    }

    @Override
    public ElectionCabinetResult electionSequence(String countryName) {
	ElectionCabinetResult result = new ElectionCabinetResult(new ArrayList<Integer> (), new ArrayList<Integer> ());
	try {	
		String clearTables = "DROP VIEW IF EXISTS intermediate CASCADE";	
		PreparedStatement dropState = connection.prepareStatement(clearTables);
		dropState.execute();

		String countryQuery = "SELECT id FROM country WHERE name = ?";
		PreparedStatement countrystatement = connection.prepareStatement(countryQuery);
		countrystatement.setString(1, countryName);
		ResultSet countryRes = countrystatement.executeQuery();
		countryRes.next();
		int countryId = countryRes.getInt("id");
		

		String ElectionQuery = "CREATE VIEW intermediate AS SELECT id, e_date, e_type AS type FROM election WHERE country_id = " + Integer.toString(countryId) + " ORDER BY e_date DESC";
		PreparedStatement ElecState = connection.prepareStatement(ElectionQuery);		
		ElecState.execute();
		
		String searchelQ = "SELECT * FROM intermediate";
		PreparedStatement sestate = connection.prepareStatement(searchelQ);
		ResultSet seRes = sestate.executeQuery();
		
		ArrayList<Integer> electionIds = new ArrayList<Integer>();
		while (seRes.next()) {
			int NeId = seRes.getInt("id");
			electionIds.add(NeId);
		}
		


		for (int i=0; i<electionIds.size(); i++) {
			int currId = electionIds.get(i);
			String updateQuery = "SELECT id FROM cabinet WHERE election_id = ? ORDER BY start_date";
			PreparedStatement updateState = connection.prepareStatement(updateQuery);
			updateState.setInt(1, currId);
			ResultSet updateRes = updateState.executeQuery();
			while (updateRes.next()) {
				int cabId = updateRes.getInt("id");
				result.elections.add(currId);
				result.cabinets.add(cabId);
			}
			
		}
		return result;

		
	}
	catch (SQLException se)
	{
		System.err.println("SQL EXCEPTION: <MESSAGE:> " + se.getMessage());
		return null;
	}

    }


    @Override
    public List<Integer> findSimilarPoliticians(Integer politicianId, Float threshold) {
    	// Implement this method!
    	List<Integer> similarPresidents = new ArrayList<Integer>();
    	Connection conn = this.connection;
    	PreparedStatement pStatement;
        ResultSet rs;
        String queryString;
    	
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (ClassNotFoundException e) {
            System.out.println("Failed to find the JDBC driver");
        }
        
        try {
        	/* Query1: Table with a single tuple, containing the
        	 * id, description, and comment of the input politician 
        	 */
        	
            queryString = "SELECT id, description, comment " +
            		"FROM politician_president " + 
            		"WHERE id = ?";
           
            pStatement = conn.prepareStatement(queryString);
            pStatement.setInt(1, politicianId);
            rs = pStatement.executeQuery();
            rs.next();
            
        	String presidentInput = rs.getString("description") + 
        			" " + rs.getString("comment");
            
        	//TESTING
//        	System.out.println(presidentInput);
//        	System.out.println("\n");
            
            // Query 2: Table of tuples consisting of all politician IDs, 
        	// description, and comments in the politician_president relation 
        	// who are not the input president's ID
        	
            queryString = "SELECT id, description, comment " +
            		"FROM politician_president " + 
            		"WHERE id != " + Integer.toString(politicianId);
            pStatement = conn.prepareStatement(queryString);
            rs = pStatement.executeQuery();
            
            
            /* NOTE: I could've done a Cartesian product between the above two queries, 
             * and then calculated the Jaccard similarity between each relvant 
             * set of attributes in the tuple, but I felt that this would increase
             * the code complexity, while  not necessarily decreasing 
             * the run time of the program. So instead, here's a while loop.
             */ 
             
            // Iterate through politicians and calculate their Jaccard similarity
            // to politicianID's description and comment
            while(rs.next()) {
            	int newID = rs.getInt("id");
            	String newInput = rs.getString("description") + 
            			" " + rs.getString("comment");
            	float jSimilarity = (float)similarity(presidentInput, newInput);
            	
            	//TESTING
//            	System.out.println(jSimilarity);
//            	System.out.println("\n");
                 
                 
            	if(jSimilarity >= threshold){
            		similarPresidents.add(newID);
            	}
            }
        }
        
       
        catch (SQLException se) {
            System.err.println("SQL Exception." +
                    "<Message>: " + se.getMessage());
        }
        
        return similarPresidents;
    }

//    public static void main(String[] args) {
//        // You can put testing code in here. It will not affect our autotester.
//    	try {
//	    Assignment2 test = new Assignment2();
//	    boolean t = test.connectDB("jdbc:postgresql://localhost:5432/csc343h-morgensh?currentSchema=parlgov", "morgensh", "");
//	    System.out.println(t);
//	    
//	    List<Integer> similarPresidents = test.findSimilarPoliticians(148, (float)0.0);
//	    Integer lenSP = similarPresidents.size();
//	    Integer i =  0;
//	    
//	    while(i < lenSP) {
//	    	System.out.println(similarPresidents.get(i));
//	    	i += 1;
//	    }
//	    
//	    
//	    boolean t1 = test.disconnectDB();
//	    System.out.println(t);
//    	}
//    	
//		catch (ClassNotFoundException e) {
//		    System.out.println("Failed to find JDBC driver");
//		}
//   }

    public static void main(String[] args) {
        // You can put testing code in here. It will not affect our autotester.
    	try {
	    Assignment2 test = new Assignment2();
	    boolean t = test.connectDB("jdbc:postgresql://localhost:5432/csc343h-alisye55?currentSchema=parlgov", "alisye55", "");
	    test.electionSequence("Germany");
	    boolean t1 = test.disconnectDB();
	}
    	
	catch (ClassNotFoundException e) {
	    System.out.println("Failed to find JDBC driver");
	}
    }

}

