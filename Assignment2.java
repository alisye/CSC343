import java.sql.*;
import java.util.List;
import java.util.ArrayList;

// If you are looking for Java data structures, these are highly useful.
// Remember that an important part of your mark is for doing as much in SQL (not Java) as you can.
// Solutions that use only or mostly Java will not receive a high mark.
//import java.util.ArrayList;
//import java.util.Map;
//import java.util.HashMap;
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
        // Implement this method!
//	ElectionCabinetResult result = new ElectionCabinetResult(new ArrayList<Integer> (), new ArrayList<Integer> ());
	return null;
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
        	//get info relevant to politicianID
            queryString = "SELECT id, description, comment " +
            		"FROM politician_president " + 
            		"WHERE id = " + Integer.toString(politicianId);
            
            pStatement = conn.prepareStatement(queryString);
            rs = pStatement.executeQuery();
            
        	String presidentInput = rs.getString("description") + 
        			" " + rs.getString("comment");
            
        	//TESTING
        	System.out.println(presidentInput);
        	System.out.println("\n");
            
            //get info for all the other policiticans 
            queryString = "SELECT id, description, comment " +
            		"FROM politician_president " + 
            		"WHERE id != " + Integer.toString(politicianId);
            pStatement = conn.prepareStatement(queryString);
            rs = pStatement.executeQuery();
            
            // iterate through politicians and calculate their Jaccard similarity
            // to politicianID's description and comment
            while(rs.next()) {
            	int newID = rs.getInt("id");
            	String newInput = rs.getString("description") + 
            			" " + rs.getString("comment");
            	float jSimilarity = (float)similarity(presidentInput, newInput);
            	
            	//TESTING
            	System.out.println(jSimilarity);
            	System.out.println("\n");
                 
                 
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

    public static void main(String[] args) {
        // You can put testing code in here. It will not affect our autotester.
    	try {
	    Assignment2 test = new Assignment2();
	    boolean t = test.connectDB("jdbc:postgresql://localhost:5432/csc343h-morgensh", "morgensh", "");
	    System.out.println(t);
	    
	    findSimilarPoliticians(9, 0.1);
	    
	    boolean t1 = test.disconnectDB();
	    System.out.println(t);
	}
    	
	catch (ClassNotFoundException e) {
	    System.out.println("Failed to find JDBC driver");
	}
    }

}

