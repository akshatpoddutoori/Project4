

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private ChatFilter filter;


    private ChatServer(int port) {
        this.port = port;
        filter = new ChatFilter("");
    }

    private ChatServer(int port, ChatFilter filter) {
        this.port = port;
        this.filter = filter;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            System.out.println(formatter.format(new Date()) + " Server waiting to Clients on port " + port + ".");
            while(true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void broadcast(String message) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        message = filter.filter(message);
        for (ClientThread c : clients) {
            c.writeMessage(formatter.format(new Date()) + " " + message);
        }
        System.out.println(formatter.format(new Date()) + " " + message);
    }

    private boolean directMessage(String message, String sender, String recipient) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        message = filter.filter(message);
        if (!sender.equals(recipient)) {
            for (ClientThread c : clients) {
                if (c.username.equals(recipient)) {
                    c.writeMessage(formatter.format(new Date()) + " " + sender + " -> " + recipient + ": " + message);
                    return true;
                }
            }
        }
        return false;
    }

    private synchronized void remove(int id) {
        for (ClientThread c : clients) {
            if (c.id == id) {
                clients.remove(c);
                break;
            }
        }
    }


    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server;
        if (args.length == 2) {
            server = new ChatServer(Integer.parseInt(args[0]), new ChatFilter(args[1]));
            if (server.filter.numWords() > 0)
                System.out.println(server.filter.toString());
            server.start();
        } else if (args.length == 1) {
            server = new ChatServer(Integer.parseInt(args[0]));
            server.start();
        } else {
            server = new ChatServer(1500);
            server.start();
        }
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private boolean writeMessage(String message) {
            message = filter.filter(message);
            if (!socket.isConnected())
                return false;
            try {
                sOutput.writeObject(message + "\n");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        // it lists all connected clients except urself
        private void list() {
            if (clients.size() > 1) {
                String list = "";
                for (ClientThread c : clients) {
                    if (!c.username.equals(username))
                        list += c.username + "\n";
                }
                list = list.substring(0, list.length() - 1);
                writeMessage(list);
            } else {
                writeMessage("");
            }
        }

        private boolean uniqueUsername() {
            int num = 0;
            for (ClientThread c : clients) {
                if (c.username.equals(username))
                    num++;
            }
            return num == 1;
        }

        private void close() {
            try {
                sInput.close();
                sOutput.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            boolean open = true;
            while(open) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                    if (cm.getType() == ChatMessage.MessageType.CONNECTED) {    // when first connecting
                        // check if username is unique
                        if (uniqueUsername()) {
                            writeMessage("Connection accepted localhost" + socket.getLocalSocketAddress());
                            System.out.println(formatter.format(new Date()) + " " + username + " just connected.");
                            System.out.println(formatter.format(new Date()) + " Server waiting for Clients on port " + port + ".");
                        } else {
                            writeMessage("Username has already been taken.");
                            close();
                            remove(id);
                            open = false;
                        }
                    } else if (cm.getType() == ChatMessage.MessageType.LOGOUT) {
                        System.out.println(formatter.format(new Date()) + " " + username + " disconnected with a LOGOUT message.");
                        close();
                        remove(id);
                        open = false;
                    } else if (cm.getType() == ChatMessage.MessageType.GENERAL) {
                        broadcast(username + ": " + cm.getMessage());
                    } else if (cm.getType() == ChatMessage.MessageType.DIRECT) {  // DIRECT MESSAGE
                        if (directMessage(cm.getMessage(), username, cm.getRecipient())) {
                            writeMessage(formatter.format(new Date()) + " " + username + " -> " + cm.getRecipient() + ": " + cm.getMessage());
                            System.out.println(formatter.format(new Date()) + " " + username + " -> " + cm.getRecipient() + ": " + filter.filter(cm.getMessage()));
                        } else
                            writeMessage("Recipient not found.");
                    } else if (cm.getType() == ChatMessage.MessageType.LIST) {
                        list();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(formatter.format(new Date()) + " " + username + " disconnected.");
                    close();
                    remove(id);
                    open = false;
                }
            }

//            System.out.println(username + ": Ping");
//
//            // Send message back to the client
//            try {
//                sOutput.writeObject("Pong");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}
