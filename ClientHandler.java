import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.sql.SQLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {


    public static HashMap<String, ClientHandler> clientHandlers = new HashMap<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUserId;
    private JavaDB javaDB;
    
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	    this.javaDB = new JavaDB();
            this.clientUserId = bufferedReader.readLine();
	   
	    if(!clientHandlers.containsKey(clientUserId)){
            	clientHandlers.put(clientUserId,this);
	    }

	    System.out.println(clientUserId+" has connected/"+clientHandlers.size());

        } catch (IOException e) {
	    System.out.println("생성자");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (!socket.isClosed()) {
           try {
                messageFromClient = bufferedReader.readLine();
		  if(messageFromClient==null){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                } else {
                    broadcastMessage(messageFromClient);
                }
            } catch (IOException e) {
		System.out.println("run2");
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

   
    public void broadcastMessage(String messageToSend) {
	     JSONObject jsonObject = null;
        try {
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(messageToSend);
        } catch (ParseException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

	   String formatDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
           jsonObject.put("date",formatDate);

	   String roomId = (String) jsonObject.get("room_id");
	   String mobile_number = (String) jsonObject.get("mobile_number");
	   String user_name = (String) jsonObject.get("user_name");
           String message = (String) jsonObject.get("message");
	   String type = (String) jsonObject.get("type");
           String task = (String) jsonObject.get("task");


        try {
            ArrayList<String> users = javaDB.getUsers(roomId,clientUserId);
	     for (int i = 0; i<users.size(); i++){
                if (clientHandlers.containsKey(users.get(i))){
		    System.out.println("Receiver : "+users.get(i)+" : "+jsonObject.toJSONString());
                    clientHandlers.get(users.get(i)).bufferedWriter.write(jsonObject.toJSONString());
                    clientHandlers.get(users.get(i)).bufferedWriter.newLine();
                    clientHandlers.get(users.get(i)).bufferedWriter.flush();
                }
            }
	    javaDB.insertMessage(roomId,mobile_number,user_name,users.toString(),message,type,task,formatDate);

        } catch (SQLException | IOException throwables) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(clientUserId);
        System.out.println(clientUserId + " has disconnected ");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
		bufferedReader = null;
		System.out.println("bufferedReader closed");
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
		bufferedWriter = null;
		System.out.println("bufferedWriter closed");

            }
            if (socket != null) {
                socket.close();
		socket = null;
		System.out.println("Socket closed");
            }
	    clientUserId = null;
        } catch (IOException e) {
            System.out.println("error");
        }
    }
}
