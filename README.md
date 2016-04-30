# Server-Client-Chat
A chatroom setup between multiple clients connecting to a server in console.
Handles general communication between two or more online users connected to a server.  Thus, it handles chat-string concatenation, node programming in Erlang, and socket programming in Java.

The objective was to create a working chat engine in console where clients would connect to a server via IP address.  
Clients are separated by name and connect to one another through a commandline.
Coded in Java and Erlang to have nearly identical functionality.  The focus was to see how OOP and functional programming would differ in handling the same server/client features, in both runtime and code.

For the Erlang side, the cookie file must be in the same runtime directory for both the client and server. 
Nodes represent each runtime virtual machine that is ready to connect to another node.  The server and clients are represented as nodes.  Clients communicate   
Commands: start_server(), start_client(ClientName, ServerNode), request_chat(QClientName,WClientName,ServerNode), send(Message).  Note that all commands are actual functions that the client-server code runs when they are called.  In this aspect, Erlang does not require intense string parsing.


For the Java side, the server and the client communicate among each other via low-level sockets.  The server splits off separate threads for each respective client, so that it is able to handle simulantaeous and different queries from clients.
All commands are parsed as strings by BufferedReaders in the Client.java code.
Commands: goOnline(name), requestChatWith(name), goOffline(). 
