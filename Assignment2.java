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
		
		int NeId;
		Date NeDate;
		String Netype;
		ArrayList<Integer> electionIds = new ArrayList<Integer>();
		ArrayList<String> electionTypes = new ArrayList<String>();
		while (seRes.next()) {
			NeId = seRes.getInt("id");
			electionIds.add(NeId);
			NeDate = seRes.getDate("e_date");
			Netype = seRes.getString("type");
			electionTypes.add(Netype);
		}
		
		HashMap<Integer, Date> nextElectionDates = new HashMap<Integer, Date>();
		for (int i=0; i<electionIds.size(); i++) {
			int currId = electionIds.get(i);
			Date currDate;
			String getDateQ = "SELECT e_date FROM intermediate WHERE id = " + Integer.toString(currId);
			PreparedStatement currState = connection.prepareStatement(getDateQ);
			ResultSet currRes = currState.executeQuery();
			currRes.next();
			currDate = currRes.getDate("e_date");

			String potentialNextQ = "SELECT e_date " +
					        " FROM intermediate" +
						" WHERE type =" + 
						" '" + electionTypes.get(i) + "'" +
						" and e_date > ? ORDER BY e_date";
			
			PreparedStatement potentialNextstate = connection.prepareStatement(potentialNextQ);
			potentialNextstate.setDate(1, currDate);
			ResultSet potentialRes = potentialNextstate.executeQuery();
			if (potentialRes.next()) {
				Date nextDate = potentialRes.getDate("e_date");
				nextElectionDates.put(currId, nextDate);
			}
			else {
				nextElectionDates.put(currId, null);
			}

		}

		for (int i=0; i<electionIds.size(); i++) {
			int currId2 = electionIds.get(i);
			Date currDate2;
			String getDateQ = "SELECT e_date FROM intermediate WHERE id = " + Integer.toString(currId2);
			PreparedStatement currState = connection.prepareStatement(getDateQ);
			ResultSet currRes = currState.executeQuery();
			currRes.next();
			currDate2 = currRes.getDate("e_date");

			Date nextDate = nextElectionDates.get(currId2);
			
			if (nextDate != null) {
				String updateQuery = "SELECT id FROM cabinet WHERE start_date > ? and start_date < ? ORDER BY start_date";
				PreparedStatement updateState = connection.prepareStatement(updateQuery);
				updateState.setDate(1, currDate2);
				updateState.setDate(2, nextDate);
				ResultSet updateRes = updateState.executeQuery();
				if (!updateRes.next()) {
					result.elections.add(100000);
				}
				updateRes.beforeFirst();
				while (updateRes.next()) {
					int cabId = updateRes.getInt("id");
					result.elections.add(currId2);
					result.cabinets.add(cabId);
				}
			}
			else {
				String updateLatestQuery = "SELECT id FROM cabinet WHERE start_date > ? ORDER BY start_date";
				PreparedStatement updateLatestState = connection.prepareStatement(updateLatestQuery);
				updateLatestState.setDate(1, currDate2);
				ResultSet latestResult = updateLatestState.executeQuery();
				while (latestResult.next()) {
					int cabLateId = latestResult.getInt("id");
					result.elections.add(currId2);
					result.cabinets.add(cabLateId);
				}
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
        	//get info relevant to politicianID
            queryString = "SELECT id, description, comment " +
            		"FROM politician_president " + 
            		"WHERE id = " + Integer.toString(politicianId);
            
            pStatement = conn.prepareStatement(queryString);
            rs = pStatement.executeQuery();
            rs.next();
            
        	String presidentInput = rs.getString("description") + 
        			" " + rs.getString("comment");
            
        	//TESTING
//        	System.out.println(presidentInput);
//        	System.out.println("\n");
            
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

