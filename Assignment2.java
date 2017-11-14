import java.sql.*;
import java.util.List;
import java.util.ArrayList;

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
		//get countryid first
		String countryIDquery = "SELECT id FROM country WHERE name = ?";
		PreparedStatement ps = connection.prepareStatement(countryIDquery);
		ps.setString(1, countryName);
		ResultSet rs = ps.executeQuery();
		rs.next();
		int countryId = rs.getInt("id");


		String elections = "SELECT id, e_date FROM election WHERE country_id = ? ORDER BY EXTRACT(YEAR FROM e_date) DESC";
		PreparedStatement ps1 = connection.prepareStatement(elections);
		ps1.setInt(1, countryId);
		ResultSet rs1 = ps1.executeQuery();
		
		while(rs1.next()) {
			int nextele = rs1.getInt("id");
			result.elections.add(nextele);
		}

		for (int electionIds : result.elections) {
			String typequery = "SELECT e_type, previous_parliament_election_id, previous_ep_election_id FROM election WHERE id = " + electionIds;
			PreparedStatement ps2 = connection.prepareStatement(typequery);
			ResultSet typetable = ps2.executeQuery();
			typetable.next();
			String type = typetable.getString("e_type");
			
			String next_type;
			if (type.equals("European Parliament")) {	
				next_type = "previous_ep_election_id";
			} else {
				next_type = "previous_parliament_election_id";
			}
		

			String findnext = "SELECT id FROM election WHERE " + next_type + " = " + Integer.toString(electionIds);
			PreparedStatement ps3 = connection.prepareStatement(findnext);
			ResultSet nextEL = ps3.executeQuery();
		
			boolean next_exists = nextEL.next();
			String dateQuery = "SELECT e_date FROM election WHERE id = " + Integer.toString(electionIds);
			PreparedStatement preparedate = connection.prepareStatement(dateQuery);
			ResultSet dateres = preparedate.executeQuery();
			dateres.next();
			Date current_electionDate = dateres.getDate("e_date");
			Date next_electionDate;
				
			if (next_exists) {
				int nextelection = nextEL.getInt("id");
				String nextDateQ = "SELECT e_date FROM election WHERE id = " + Integer.toString(nextelection);
				PreparedStatement preparenextdate = connection.prepareStatement(nextDateQ);
				ResultSet nextres = preparenextdate.executeQuery();
				nextres.next();
				next_electionDate = nextres.getDate("e_date");
				String findCabs = "SELECT id FROM cabinet WHERE start_date > ? and start_date < ?";
				PreparedStatement ps4 = connection.prepareStatement(findCabs);	
				ps4.setDate(1, current_electionDate);
				ps4.setDate(2, next_electionDate);
				ResultSet validcabs1 = ps4.executeQuery();
				while (validcabs1.next()){
					int cabs = validcabs1.getInt("id");
					result.cabinets.add(cabs);
				}

			} else {
				String findfirstCabs = "SELECT id FROM cabinet WHERE start_date > ?";
				PreparedStatement ps5 = connection.prepareStatement(findfirstCabs);
				ps5.setDate(1, current_electionDate);
				ResultSet validcabs2 = ps5.executeQuery();
				while (validcabs2.next()) {
					int cabs = validcabs2.getInt("id");
					result.cabinets.add(cabs);
				}
				
			}
		}
		
		System.out.println(result.toString());
		return result;
	}
	catch (SQLException se)
	{
		System.err.println("SQL EXCEPTION: <MESSAGE:> " + se.getMessage());
		return null;
	}
    }

    @Override
    public List<Integer> findSimilarPoliticians(Integer politicianName, Float threshold) {
        
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
            rs.next();
            
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
>>>>>>> refs/remotes/origin/master
    }

    public static void main(String[] args) {
        // You can put testing code in here. It will not affect our autotester.
    	try {
	    Assignment2 test = new Assignment2();
	    boolean t = test.connectDB("jdbc:postgresql://localhost:5432/csc343h-alisye55?currentSchema=parlgov", "alisye55", "");
	    System.out.println(t);
	    test.electionSequence("Germany");
	    boolean t1 = test.disconnectDB();
	    System.out.println(t1);
	}
    	
	catch (ClassNotFoundException e) {
	    System.out.println("Failed to find JDBC driver");
	}
    }

}

