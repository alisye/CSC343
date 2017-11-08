import java.sql.*;
import java.util.List;

// If you are looking for Java data structures, these are highly useful.
// Remember that an important part of your mark is for doing as much in SQL (not Java) as you can.
// Solutions that use only or mostly Java will not receive a high mark.
//import java.util.ArrayList;
//import java.util.Map;
//import java.util.HashMap;
//import java.util.Set;
//import java.util.HashSet;
public class Assignment2 extends JDBCSubmission {
    Connection conn;
    public Assignment2() throws ClassNotFoundException {

        Class.forName("org.postgresql.Driver");
    }

    @Override
    public boolean connectDB(String url, String username, String password) {	
	try {
		conn = DriverManager.getConnection(url, username, password);
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
		conn.close();
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
        return null;
    }

    @Override
    public List<Integer> findSimilarPoliticians(Integer politicianName, Float threshold) {
        // Implement this method!
        return null;
    }

    public static void main(String[] args) {
        // You can put testing code in here. It will not affect our autotester.
        System.out.println("Hello");
    }

}

