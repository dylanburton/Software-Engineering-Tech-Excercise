package rankings;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;


/**
 * Servlet implementation class MyServletRankingApp
 */
@WebServlet("/MyServletRankingApp")
public class MyServletRankingApp extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static String url = "jdbc:mysql://ec2-18-220-206-97.us-east-2.compute.amazonaws.com:3306/player_rankings";
	static String user = "dylanburton";
	static String password = "Superman!";
	static Connection connection = null;   
    
	/**
     * @see HttpServlet#HttpServlet()
     */
    public MyServletRankingApp() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");

	    
	    //response.getWriter().println("-------- MySQL JDBC Connection Testing ------------<br>");
	    try {
	    	Class.forName("com.mysql.cj.jdbc.Driver"); //old:com.mysql.jdbc.Driver
	    } catch (ClassNotFoundException e) {
	    	System.out.println("Where is your MySQL JDBC Driver?");
	    	e.printStackTrace();
	    	return;
	    }

	    //response.getWriter().println("MySQL JDBC Driver Registered!<br>");
	    connection = null;
	    try {
	    	connection = DriverManager.getConnection(url, user, password);
	    } catch (SQLException e) {
	    	System.out.println("Connection Failed! Check output console");
	    	e.printStackTrace();
	    	return;
	    }
	    if (connection != null) {
	        //response.getWriter().println("You made it, take control your database now!<br>");
	     } else {
	        System.out.println("Failed to make connection!");
	     }

	    /*--Web site String placed here--*/
	    
	    String websiteHTML = "<!DOCTYPE html>\r\n" + 
	    		"<html>\r\n" + 
	    		"\r\n" + 
	    		"<head>\r\n" + 
	    		"<meta charset=\"ISO-8859-1\">\r\n" + 
	    		"<title>Rankings</title>\r\n" + 
	    		"</head>\r\n" + 
	    		"\r\n" + 
	    		"<body>\r\n" + 
	    		"<h1>Tournament Rankings</h1>\r\n" + 
	    		"<h2>Directions</h2>\r\n" + 
	    		"<p>Type in Player tag into player field, and then type in wins entered into field. Then Click the Update Player Record button. If a players tag is new the wins will be placed the number of wins entered. If the player already exists, the number entered will be added to the current number in the leaderboard.</p>\r\n" + 
	    		"<h2>Insert information</h2>\r\n" + 
	    		"<form action=\"MyServletRankingApp\" method=\"post\">" +
	    		
	    		"<input type=\"text\" placeholder=\"Enter Player Tag Here\" id=\"player\" name=\"player\"/>" +
	    		"<input type=\"number\" step=\"1\" placeholder=\"Wins Enter Here\" id=\"wins\" name=\"wins\"/>" + 
	    		"<input type = \"submit\" value=\"Update Player Record\"/>" +
	    		"</form>\r\n" + 
	    		"\r\n" + 
	    		"<h2>Current Rankings:</h2>\r\n";
	    try {
	    	/*-- Inserting players/ Updating player information --*/

		    String playertag = request.getParameter("player");
		    String playerwins = request.getParameter("wins");
		    
		    if(playertag == null) {
		    	playertag = "";
		    }
		    
		    /*-- check if playertag or playerwins is empty. If it isn't add to database --*/
		    if(!playertag.equals("")) {
		    	
		    	/*-- Check if the player exists in database--*/
			    boolean playerExists = false;
			    String checkStringSQL = "Select * from ranks where PlayerTag = \"" + playertag + "\"";
			    PreparedStatement playerCheck = connection.prepareStatement(checkStringSQL);
			    ResultSet numberentries = playerCheck.executeQuery();
			    while(numberentries.next()) {
			    	playerExists = true;
			    	break;
			    }
			    
		    	int actualplayerwins;
		    	if(playerwins.isEmpty()) {
		    		actualplayerwins = 0;
		    	} else {
		    		actualplayerwins = Integer.parseInt(playerwins);
		    	}
		    	
		    	if(!playerExists) {
		    	String addplayerSQL = "insert into ranks value (?, ?)";
		    	
		    	PreparedStatement addStatement = connection.prepareStatement(addplayerSQL);
		    	addStatement.setString(1, playertag);
		    	addStatement.setInt(2, actualplayerwins);
		    	
		    	addStatement.executeUpdate();
		    	} else {
		    		
		    		String grabCurrentWinsSQL = "Select Wins from ranks where PlayerTag = \"" + playertag +"\"";
		    		PreparedStatement updateStatement = connection.prepareStatement(grabCurrentWinsSQL);
		    		ResultSet rese = updateStatement.executeQuery();
		    		int totalplayerwins = 0;
		    		while(rese.next()) {
		    			int currentplayerwins = rese.getInt("Wins");
		    			totalplayerwins += currentplayerwins;
		    		}
			    	totalplayerwins += actualplayerwins;
			    	String updateSQL = "update ranks set Wins = " + totalplayerwins + " where PlayerTag = \"" + playertag + "\"";
			    	updateStatement = connection.prepareStatement(updateSQL);
			    	updateStatement.executeUpdate();
		    	}
		    } else {
		    	websiteHTML += "Enter Player to be Updated or Inserted<br>";
		    }
		    
	    	/*-- Displaying ranks of players --*/
	    String selectSQL = "Select * from ranks order by Wins desc";
	    PreparedStatement preppedStatement = connection.prepareStatement(selectSQL);
	    ResultSet rs = preppedStatement.executeQuery();
	    int count = 1;
	    websiteHTML+= "Rank | Player Tag | Wins <br>";
	    while(rs.next()) {
	    	websiteHTML+= count++ + " | ";
	    	websiteHTML+= rs.getString("PlayerTag") + " | ";
	    	websiteHTML+= " " + rs.getInt("Wins") + "<br>";
	    }
	    
	    websiteHTML+=   "</body>" + "\r\n" + "</html>";
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    }	    
	    
	    response.getWriter().println(websiteHTML);
	    
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
