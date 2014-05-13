import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.net.InetAddress;

public class ServerStart{
	static final int PORT = 50000;
	static final int PORT_OFF = 88;
	static int x = 1;
	public static ArrayList<Socket> clientList = new ArrayList<>();
	public static ArrayList<String> nameList = new ArrayList<>();
	public static ArrayList<String> busyList = new ArrayList<>();
	public static void main(String args[]){
		Socket client = null;
		ServerSocket server = null;
                try{
			server = new ServerSocket(PORT + PORT_OFF);
			System.out.println("Server started.");
		}
		catch(IOException e){
			System.out.println(e);
		}
		while(true){
			try{
				client = server.accept();
				System.out.println("Connection made with Client " + x);
				x++;
				SRunnable s = new SRunnable(client);
				Thread splitoff = new Thread(s);
				splitoff.start();
			}
			catch(IOException e){
				System.out.println(e);
			}
	        }
    	}
}

class SRunnable implements Runnable{  
	BufferedReader input = null;
	PrintWriter printWriter = null;
	Socket socket = null;
	int trackForPort = ServerStart.x;

	public SRunnable(Socket socket){
		this.socket=socket;
	}

/*run() is started when start() is called on the thread.
  Different indenting style is used here to increase readability.
  This code checks for all commands sent to the server from a client.
*/
	public void run() {
	  try{
	    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    printWriter = new PrintWriter(socket.getOutputStream());
    	  }
	  catch(IOException e){
	    System.out.println(e);
	  }
   	  try{	
	    String chat = input.readLine();
	    while(true){
//------------------------------Beginning of if control block: all input has "(" within it
              if(chat.contains("(")){
	        String[] temp = chat.split("\\(");
		String former = temp[0];
		String latter = temp[1];
//------------------------------GOONLINE
		if(former.equals("name") && latter.charAt(latter.length() - 1) == ')'){
		  latter = latter.substring(0, latter.length() - 1);
		    if(ServerStart.nameList.contains(latter)){
		      printWriter.println("No");
		      printWriter.flush();
		      ServerStart.x--;
		      Thread.currentThread().interrupt();
		      return;
		    }
		  printWriter.println("Yes");
		  printWriter.flush();
		  System.out.println(latter + " has logged on.");
		  ServerStart.nameList.add(latter);
		  ServerStart.clientList.add(socket);
		  System.out.println("Users online: ");
		  for(int i=0; i<ServerStart.nameList.size(); i++){
		    System.out.println(ServerStart.nameList.get(i));
		    System.out.println(ServerStart.clientList.get(i).toString());
		  }
		  System.out.println("---------------------");
		  chat = input.readLine();
		}
		else if(former.equals("goOnline")){
 		  printWriter.println("You are already online.");
		  printWriter.flush();
		  chat = input.readLine();
		}
//------------------------------GOOFFLINE
                else if(former.equals("goOffline") && latter.charAt(latter.length() - 1) == ')'){
		  latter = latter.substring(0, latter.length() - 1);
		  if(!ServerStart.clientList.contains(socket)){
		    printWriter.println("Server error: You are not logged on.");	
		    printWriter.flush();
	            chat = input.readLine();
		    continue;
		  }
		  else{
                    int j = 0;
                    for(int i=0; i<ServerStart.clientList.size(); i++){
                      if(socket.equals(ServerStart.clientList.get(i))){
		        j=i; 
	              }
                    }
                    if(!ServerStart.nameList.get(j).equals(latter)){
                      printWriter.println("Server error: You are logged on under " + ServerStart.nameList.get(j) + ".");
		      printWriter.flush();
	              chat = input.readLine();
                      continue;
		    }
		  }
		  for(int i=0; i<ServerStart.nameList.size(); i++){
		    if(latter.equals(ServerStart.nameList.get(i))){
		      if(ServerStart.clientList.get(i).equals(socket)){
	                ServerStart.nameList.remove(i);
			ServerStart.clientList.remove(i); 
		      }
		    }
                  }
		  System.out.println(latter + " has logged off.");
		  ServerStart.x--;
		  printWriter.flush();
		  System.out.println("Users online: ");
		  for(int i=0; i<ServerStart.nameList.size(); i++){
		    System.out.println(ServerStart.nameList.get(i));
		    System.out.println(ServerStart.clientList.get(i).toString());
		  }
		  System.out.println("---------------------");
		  printWriter.println("You are now logged off.");
		  printWriter.flush();
		  BufferedReader input = null;
		  PrintWriter printWriter = null;
		  Thread.currentThread().interrupt();
		  return;
                }
//------------------------------REQUESTCHATWITH
                else if(former.equals("requestChatWith") && latter.charAt(latter.length() - 1) == ')'){
		  latter = latter.substring(0, latter.length() - 1);
		  int j = 0;
		  int k = 0;
		  if(!ServerStart.clientList.contains(socket)){
		    printWriter.println("Server error: You are not logged on.");	
		    printWriter.flush();
	            chat = input.readLine();
		    continue;
		  }
		  else if(ServerStart.busyList.contains(latter)){
		    printWriter.println("Server error: The user " + latter + " is currently talking to someone else.");
		    printWriter.flush();
		    chat = input.readLine();
		    continue;
		  }
		  else if(!ServerStart.nameList.contains(latter)){
		    printWriter.println("Server error: The user " + latter + " is not logged on.");	
		    printWriter.flush();
	            chat = input.readLine();
		    continue;
		  }
		  else{
                    for(int i=0; i<ServerStart.clientList.size(); i++){
                      if(socket.equals(ServerStart.clientList.get(i))){
		        k=i; 
	              }
                    }
                    if(ServerStart.nameList.get(k).equals(latter)){
                      printWriter.println("Server error: You cannot chat with yourself.");
		      printWriter.flush();
	              chat = input.readLine();
                      continue;
		    }
		  }
		  for(int i=0; i<ServerStart.nameList.size(); i++){
		    if(latter.equals(ServerStart.nameList.get(i))){
		      j=i; 
		    }
 	          }
                  Socket receiver = ServerStart.clientList.get(j); 
		  BufferedReader br1 = null;
		  BufferedReader br = null;
		  PrintWriter pw1 = null;
		  PrintWriter pw = null;
		  try{
	            br1 = new BufferedReader(new InputStreamReader(receiver.getInputStream()));
	            pw1 = new PrintWriter(receiver.getOutputStream());
		  }
                  catch(IOException e){
		    System.out.println(e);
		  }
		  ServerSocket serverConvo = null;
		  Socket clientConvo = null;
		  int currentPort = ServerStart.PORT + ServerStart.PORT_OFF + (129 * trackForPort);
                  try{
		    serverConvo = new ServerSocket(currentPort);
		  }
		  catch(IOException e){
		    System.out.println(trackForPort);
	          }
		  pw1.println("Would you like to chat with " + ServerStart.nameList.get(k) + "? Port: " + currentPort + " Address: " + socket.getInetAddress().toString() + " ");
		  pw1.flush();
                  try{
		    clientConvo = serverConvo.accept();
	            br = new BufferedReader(new InputStreamReader(clientConvo.getInputStream()));
	            pw = new PrintWriter(clientConvo.getOutputStream());
		  }
		  catch(IOException e){
		    System.out.println(e);
		  }
		  String output;
		  output = br.readLine();
		  while(!output.equals("Y") && !output.equals("N")){
		    output = br.readLine();
		  }
		  if(output.equals("N")){
		    serverConvo.close();
		    clientConvo.close();
		    pw.close();
		    br.close();
		    printWriter.println("Recipient declined.");
		    printWriter.flush(); 
		    chat = input.readLine();
		    continue;
		  }
		  else if(output.equals("Y")){
		    serverConvo.close();
		    clientConvo.close();
		    pw.close();
		    br.close();
		    printWriter.println("ACCEPT " + currentPort + " " + ServerStart.nameList.get(j));
		    ServerStart.busyList.add(ServerStart.nameList.get(j));
		    ServerStart.busyList.add(ServerStart.nameList.get(k));
		    printWriter.flush();
		    chat = input.readLine();
		    continue;
		  }
                } 
//------------------------------Server and client are both done talking
                else if(former.equals("REMOVE") && latter.charAt(latter.length() - 1) == ')'){
		  latter = latter.substring(0, latter.length() - 1);
		  String[] tempName = latter.split(" ");
		  String firstName = tempName[0];
		  String secondName = tempName[1];
	          if(ServerStart.busyList.contains(firstName) && ServerStart.busyList.contains(secondName)){
		    ServerStart.busyList.remove(firstName);
		    ServerStart.busyList.remove(secondName);
		  } 	
		  System.out.println("Number of users talking: " + ServerStart.busyList.size());
		  chat = input.readLine();
		}
//------------------------------Input contains a "(" but is not a command
	        else{
		  printWriter.println("Server Error: Not a command.  Please refer to the help manual.");
		  printWriter.flush();
		  chat = input.readLine();
	        }
//------------------------------
              }//end of if statement block: if control reaches past here, then input has no "(" and is not valid
              else{
	        printWriter.println("Server Error: Not a command.  Please refer to the help manual.");
		printWriter.flush();
		chat = input.readLine();
	      }
	    }//The end of the while loop   
          } //The end of the try block
	  catch(IOException e){
            System.out.println(e);
	  }
        }//End of run()
}//End of class
