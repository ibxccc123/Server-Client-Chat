-module(cs).
-compile([export_all]).

% Server functions
start_server() ->
    register(server,spawn(cs,server_listen,[])).

server_listen() ->
    io:format("Server started.~n"),
    Dict = dict:new(),
    BusyList = dict:new(),
    server_loop(Dict,BusyList).

server_loop(Dict,BusyList) ->
    receive
        {logOn,ClientName,PID} ->
            add_name(ClientName,Dict,PID,BusyList);
        {logOff,ClientName} ->
            remove_name(ClientName,Dict,BusyList);
        {request,QClientName,WClientName,PID} ->
            check_dict(WClientName,PID,Dict,BusyList);
        {invite,QClientName,WClientName,WClientPID,PID} ->
            timer:sleep(1000),
            WClientPID ! {invited,QClientName,WClientPID,PID},
            server_wait(QClientName,WClientName,WClientPID,PID,Dict,BusyList);
        {ClientName} ->
            BusyList1 = dict:erase(ClientName,BusyList),
            server_loop(Dict,BusyList1)
    end.

server_wait(QClientName,WClientName,WClientPID,PID,Dict,BusyList) ->
    receive
        {y} ->
            PID ! {y},
            BusyList1 = dict:store(QClientName,PID,BusyList),
            BusyList2 = dict:store(WClientName,WClientPID,BusyList1),
            server_loop(Dict,BusyList2);
        {n} ->
            PID ! {n},
            server_loop(Dict,BusyList)
    end.

check_dict(WClientName,PID,Dict,BusyList) ->
    case dict:is_key(WClientName,Dict) of
        false ->
            PID ! no,
            server_loop(Dict,BusyList);
        true ->
            check_availability(WClientName,PID,Dict,BusyList)
    end.

check_availability(WClientName,PID,Dict,BusyList) ->
    case dict:is_key(WClientName,BusyList) of
        true ->
            PID ! busy,
            server_loop(Dict,BusyList);
        false ->
            PID ! {free,dict:fetch(WClientName,Dict)},
            server_loop(Dict,BusyList)
        end.

add_name(ClientName,Dict,PID,BusyList) ->
    case dict:is_key(ClientName,Dict) of
        true ->
            PID ! no,
            server_loop(Dict,BusyList);
        false ->
            Dict1 = app(ClientName,Dict,PID),
            PID ! yes,
            server_loop(Dict1,BusyList)
    end.

app(ClientName,Dict,PID) ->
    dict:store(ClientName,PID,Dict).

remove_name(ClientName,Dict,BusyList) ->
    Dict1 = dict:erase(ClientName,Dict),
    server_loop(Dict1,BusyList).

% Client functions
start_client(ClientName,ServerNode) ->
    register(cs_client,spawn(cs,check_name,[ClientName,ServerNode])).

check_name(ClientName,ServerNode) ->
    {server,ServerNode} ! {logOn,ClientName,self()},
    receive
        no ->
            io:format("Name is already taken.~n");
        yes ->
            io:format("Welcome aboard, ~p~n",[ClientName]),
            wait(ServerNode)
    end.

wait(ServerNode) ->
    receive
        {invited,QClientName,WClientPID,PID} ->
            io:format("You have been invited to talk with ~p~n",[QClientName]),
            io:format("Y/N~n"),
            send_message(QClientName,PID,ServerNode);
        {request,QClientName,WClientName,PID} ->
            {server,ServerNode} ! {request,QClientName,WClientName,self()},
            receive
                no ->
                    io:format("User is not online.~n");
                busy ->
                    io:format("User is in a conversation.~n");
                {free,WClientPID} ->
                    io:format("User is free!  Sending them a message.~n"),
                    {server,ServerNode} ! {invite,QClientName,WClientName,WClientPID,self()},
                    wait_accept(QClientName,WClientName,WClientPID,self(),ServerNode)
            end
    end,
    wait(ServerNode).

send_message(QClientName,PID,ServerNode) ->
    Data = string:strip(io:get_line("Prompt:"),right,$\n),
    case Data of
        "N" ->
            {server,ServerNode} ! {n};
        "Y" ->
            {server,ServerNode} ! {y},
            io:format("Connection made!~n"),
            connection(QClientName,PID,ServerNode);
        _  ->
            io:format("This is not a valid response.  Please retype a valid response: Y or N..~n"),
            send_message(QClientName,PID,ServerNode)
    end.

log_out(ClientName,ServerNode) ->
    {server,ServerNode} ! {logOff,ClientName},
    io:format("You have logged off.~n").

request_chat(QClientName,WClientName,ServerNode) ->
    cs_client ! {request,QClientName,WClientName,self()},
    timer:sleep(2000).

wait_accept(QClientName,WClientName,WClientPID,PID,ServerNode) ->
    receive
        {y} ->
            io:format("Conversation accepted!~n"),
            connection(WClientName,WClientPID,ServerNode);
        {n} ->
            io:format("Conversation rejected.~n")
    end.

connection(ClientName,ClientPID,ServerNode) ->
    receive
        {quit} ->
            io:format("Connection ended.~n"),
            {server,ServerNode} ! {ClientName}, 
            timer:sleep(2000),
            ClientPID ! {quit},
            wait(ServerNode);
        {String} ->
            ClientPID ! {message,String},
            connection(ClientName,ClientPID,ServerNode);
        {message,String} ->
            io:format("~p: ~p~n", [ClientName,String]),
            connection(ClientName,ClientPID,ServerNode)
    end.

send(String) ->
    case String of 
        "quit()" ->
            cs_client ! {quit};
        _ ->
            cs_client ! {String}
    end.
