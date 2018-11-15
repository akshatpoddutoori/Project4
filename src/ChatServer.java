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


    private ChatServer(int port) {
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
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

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server;
        if (args.length == 1)
            server = new ChatServer(Integer.parseInt(args[0]));
        else
            server = new ChatServer(1500);
        server.start();
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

        private synchronized void broadcast(String message) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            if (message != null) {
                try {
                    sOutput.writeObject(formatter.format(date) + " ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                for (ClientThread c : clients) {
//                    writeMessage(c.username + ": " + message + "\n");
//                }
            }
        }

        private boolean writeMessage(String msg) {
            if (!socket.isConnected()) {
                return false;
            } else {
                try {
                    sOutput.writeObject(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }


        private void remove(int id) {

        }




        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            while (true) {
                try {
//                while(true) {
//                    String msg = (String) sInput.readObject();
//                    System.out.print(msg);
                    cm = (ChatMessage) sInput.readObject();
//                    System.out.println(cm.getMessage());
//                }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
//            System.out.println(username + ": Ping");
//            System.out.println("???" + cm.getMessage());
//            broadcast(cm.getMessage());



                // Send message back to the client
//                try {
//                    sOutput.writeObject("Pong");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                Date date = new Date();
                if (cm.getMessage() != null) {
                    if (cm.getType().equals("connected")) {
                        System.out.println("Server waiting for Clients on port " + port + ".");
                        System.out.println(username + " has connected.");
                    }
                    else if (cm.getType().equals("logout")) {
                        close();
                    }
                    else {
                        for (ClientThread c : clients) {
//                        try {
//                            sOutput.writeObject(formatter.format(date) + " ");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                            c.writeMessage(formatter.format(date) + " " +
                                    username + ": " + cm.getMessage() + "\n");
//                    broadcast(cm.getMessage());
                        }
                        System.out.println(formatter.format(date) + " " +
                                username + ": " + cm.getMessage());
                    }
                }
            }
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
    }
}
