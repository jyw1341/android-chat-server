  import java.sql.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class JavaDB {
    Connection conn;

    JavaDB(){
        try{
            String url = "jdbc:mysql://localhost/users";
            String id = "jyw1341";
            String pw = "tjqj!341";
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url,id,pw);
            System.out.println("DB connected");
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    public ArrayList<String> getUsers(String roomId, String mMobileNumber) throws SQLException {
        String sql = "SELECT*FROM chat_room WHERE room_id=?";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1,roomId);

        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<String> users = new ArrayList<>();
        while(resultSet.next()){
            String participants = resultSet.getString("room_people");
            StringTokenizer stringTokenizer = new StringTokenizer(participants,",");
            while (stringTokenizer.hasMoreTokens()){
		String user = stringTokenizer.nextToken();
                if(!user.equals(mMobileNumber)){
                    users.add(user);
                }
            }
        }
        return users;
    }

    public void insertMessage(String room_id,String mobile_number,String user_name,String receivers, String message, String type, String task, String date) throws SQLException {
	String sql = "INSERT INTO chat_message(room_id,mobile_number,user_name,receivers,message,type,task,date) VALUES(?,?,?,?,?,?,?,?)";
	PreparedStatement preparedStatement = conn.prepareStatement(sql);
	preparedStatement.setString(1,room_id);
	preparedStatement.setString(2,mobile_number);
	preparedStatement.setString(3,user_name);
        preparedStatement.setString(4,receivers);
	preparedStatement.setString(5,message);
	preparedStatement.setString(6,type);
	preparedStatement.setString(7,task);
	preparedStatement.setString(8,date);
	
	int result = preparedStatement.executeUpdate();
	System.out.println("message is successfully inserted");
    }
}

