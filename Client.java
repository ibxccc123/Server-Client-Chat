import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;

public class Client{
	static final int PORT = 50000;
	static final int PORT_OFF = 88;
	public static void main(String args[]) throws IOException{
		Socket socket = null;
		BufferedReader clientReader = null;
		BufferedReader serverReader = null;	
		PrintWriter serverWriter = null;
		String input = null;
		String output = null;
		String address = null;
		String ownName = "";
		String chatName = "";
		clientReader = new BufferedReader(new InputStreamReader(System.in));
		int x = 0;
		while(x == 0){
		  System.out.print("Client> ");
		  input = clientReader.readLine();
		  if(input.contains("(")){
		    String[] temp = input.split("\\(");
		    String former = temp[0];
		    String latter = temp[1];
		    if(former.equals("goOnline") && latter.charAt(latter.length() - 1) == ')'){
		      String[] arguments = latter.split("\\,");
		      address = arguments[0];
                      String name = arguments[1];
		      name = name.substring(0, name.length() - 1);
		      try{
		        socket = new Socket(address,PORT+PORT_OFF);
			serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		 	serverWriter = new PrintWriter(socket.getOutputStream());
		      }	
		      catch(IOException e){
		        System.out.println("No server exists at the address.");
			continue;
		      }
		      serverWriter.println("name(" + name + ")");
		      serverWriter.flush();
		      output = serverReader.readLine();
		      if(output.equals("No")){
			System.out.println("This name is already in use at the server.");
		        socket.close();
			serverReader = null;
			serverWriter = null;
			continue;
		      }
		      else{
		        System.out.println("Welcome aboard, " + name + ".");
			System.out.print("Client> ");
			x = 1;
			ownName = name;
		      }
		  }
		  else{
		    System.out.println("Please log onto a server first.");
		    continue;	
		  }
		  }
		else{
		  System.out.println("This is not a valid command.  Please log on first to a server.");
		  continue;
		}
              //End of while loop for server-less clients	
	      while(true){
	        while(true){
		  if(!clientReader.ready()){
	            if(!serverReader.ready()){
	              continue;
  	 	    }
                    else{//Server has sent a message
	              output = serverReader.readLine();
		      System.out.println("\n" + output);
		      String chatAddress = "";
		      String chatPort = "";
		      for(int i=28; i<output.length(); i++){
		        if(output.charAt(i) != '?'){
		          chatName += output.charAt(i);
		        }
			else{
			  break;
			}
		      }
		      String strTemp = "Port: ";
		      int intTemp = output.indexOf(strTemp); 
                      intTemp += 6;
		      char addressCharAt = '\u0000';
		      while(addressCharAt != ' '){
			addressCharAt = output.charAt(intTemp);
			chatPort += addressCharAt;
		        intTemp++;
		      }
		      strTemp = "Address: ";
		      intTemp = output.indexOf(strTemp);
		      intTemp += 9;
		      addressCharAt = '\u0000';
                      while(addressCharAt != ' '){
		        addressCharAt = output.charAt(intTemp);
		        chatAddress += addressCharAt;
			intTemp++;
		      } 
		      chatAddress = chatAddress.trim();
		      chatAddress = chatAddress.substring(1);
		      chatPort = chatPort.trim();
                      System.out.print("[Y/N]");
                      System.out.print("\nClient> ");
		      Socket chatClient = null;
		      PrintWriter sWriter = null;
		      try{//Make thread sleep to wait for server to go up
			Thread.sleep(2500);
		      }
		      catch(InterruptedException e){
		        System.out.println(e);
		      }
		      try{
		        chatClient = new Socket(address,Integer.parseInt(chatPort));
		 	sWriter = new PrintWriter(chatClient.getOutputStream());
	 	      }
		      catch(IOException e){
		        System.out.println(e);
		      }
		      input = clientReader.readLine();
		      while(!input.equals("N") && !input.equals("Y")){
		        System.out.println("Please enter Y or N.");
		        System.out.print("Client> ");
			input = clientReader.readLine();
		      }
	              if(input.equals("N")){
 		        System.out.println("N");
			System.out.print("Client> ");
			sWriter.println("N");
			sWriter.flush();
		 	chatClient.close();
			sWriter.close();
			chatName = "";
			continue;	
		      }
		      else if(input.equals("Y")){//Client becomes the client/receiver in conversation
			sWriter.println("Y");
			sWriter.flush();
		        chatClient.close();
			sWriter.close();
			chatClient = null;
			BufferedReader sReader = null;
			sWriter = null;
			try{//Make thread sleep to wait for server to go up
			  Thread.sleep(2500);
			}
			catch(InterruptedException e){
			  System.out.println(e);
			}
			try{
		          chatClient = new Socket(chatAddress,Integer.parseInt(chatPort));
			  sReader = new BufferedReader(new InputStreamReader(chatClient.getInputStream()));
		 	  sWriter = new PrintWriter(chatClient.getOutputStream());
			}
			catch(IOException e){
			  System.out.println(e);
			  break;
			}
		        System.out.println("Connected! Type quitChat() in order to exit the conversation.");
			System.out.print("Client> ");
			while(true){
			  if(!clientReader.ready()){
			    if(!sReader.ready()){
			      continue;
			    }
			    else{
			      output = sReader.readLine();
			      System.out.println("\n" + chatName + ": " + output);
			      if(output.equals("quitChat()")){
			        break;
			      }
		              System.out.print("Client> ");
			      continue;
			    }
			  }
			  else{
			    input = clientReader.readLine();
			    sWriter.println(input);
			    sWriter.flush();
			    System.out.println(ownName + ": " + input);
			    if(input.equals("quitChat()")){
			      break;
			    }
			    System.out.print("Client> ");
			    continue;
			  }
			}//End of while block for conversation	
			sWriter.close();
			sReader.close();
			chatClient.close();	
			chatName = "";
		  	System.out.println("Ending conversation...");
		  	System.out.print("Client> ");
		  	continue;
		      }//End of conversation block
                    }//End of block where server has sent a message
                  }//End of if block where user was not sent a message
                  else{//Client has typed a message
		    input = clientReader.readLine();
                    break;
                  }
                }//End of inner while loop that checks whether client has typed a message or received a message
		serverWriter.println(input);
		serverWriter.flush();
		output = serverReader.readLine();
		String[] temp = output.split(" ");
		String former = "";
		String latter = "";
		if(temp.length == 3){
		  former = temp[0];
		  latter = temp[1];
		  chatName = temp[2];
		}
		if(output.equals("You are now logged off.")){
		  x=0;  
		  ownName = "";
		  System.out.println(output);
		  break;
		}
	        else if(former.equals("ACCEPT")){//Client becomes the host server for conversation
		  ServerSocket serverConvo = null;
		  Socket client = null; 
		  BufferedReader br = null;
		  PrintWriter pw = null;
		  try{
		    serverConvo = new ServerSocket(Integer.parseInt(latter));
		  }
		  catch(IOException e){
		    System.out.println(e);
		  }
		  try{
		    client = serverConvo.accept();
	    	    br = new BufferedReader(new InputStreamReader(client.getInputStream()));
	   	    pw = new PrintWriter(client.getOutputStream());
		  }
	  	  catch(IOException e){
	    	    System.out.println(e);
	          } 
		  System.out.println("Connected! Type quitChat() in order to exit the conversation.");
		  System.out.print("Client> ");
		  while(true){
		    if(!clientReader.ready()){
		      if(!br.ready()){
		        continue;
		      }
		      else{
		        output = br.readLine();
		        System.out.println("\n" + chatName + ": " + output);
			if(output.equals("quitChat()")){
			  break;
			}
		        System.out.print("Client> ");
		        continue;
		      }
		    }
		    else{
		      input = clientReader.readLine();
		      pw.println(input);
		      pw.flush();
		      if(input.equals("quitChat()")){
		        System.out.println(ownName + ": " + input);
		        break;
		      }
		      System.out.println(ownName + ": " + input);
		      System.out.print("Client> ");
		      continue; 
		    }
		  }
		  br.close();
		  pw.close();
		  client.close();
		  serverConvo.close();
		  System.out.println("Ending conversation...");
		  System.out.print("Client> ");
		  serverWriter.println("REMOVE(" + chatName + " " + ownName + ")");
		  serverWriter.flush();
		  chatName = "";
		  continue;
		}
		System.out.print(output);
		System.out.print("\nClient> ");
	      }//End of while loop for clients connected to the server
	      }//End of while loop for server-less clients
	}
}
