import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;


/*
  this is a client class..

 */

public class Client {

    public static void main(String[] args) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        if(args.length < 1) {
            log(" Usage: java server machineName required ");
            System.exit(0);
        }

        /*
          collecting available servers from UI
         */

        Scanner s = new Scanner(System.in);
        log("Enter n no of servers available for client to connect at this IP address");

        int n = s.nextInt();
        int[] serverPorts = new int[n];
        ArrayList<String> allServers = new ArrayList<>();

        log("Enter " + n + " of server port nos");
        for(int i=0; i<n; i++){
            serverPorts[i] = s.nextInt(); allServers.add("" + serverPorts[i]);
        }

        try {

             /*
               Updating all servers information
             */

            //InetAddress host = InetAddress.getByName(args[0]);
            for (int i = 0 ; i < allServers.size() ; i++)
            {
                Registry registry = LocateRegistry.getRegistry(args[0], serverPorts[i]);
                ServerInterface stub = (ServerInterface) registry.lookup("ServerInterface");

                stub.updateAllServers(allServers);
            }

            /*
              Choosing one machine from available machines from user and connecting to it
             */
            System.out.print("We have " + n + " machines, enter one machine port address from above to connect \n");
            int choose = s.nextInt();

            Registry registry = LocateRegistry.getRegistry(args[0], choose);
            ServerInterface server = (ServerInterface) registry.lookup("ServerInterface");
            connect(server, choose, reader);
            System.out.print("Closing the connection to the server " + serverPorts[choose]);

        } catch (Exception e) {
            log(e.getMessage());
        }
    }


    public static void connect( ServerInterface server, int port , BufferedReader reader) throws RemoteException {


        System.out.println("Connected to the server on port  " + port);

        try {


            System.out.println("doing pre-population PUT, GET, DELETE on server with port " + port);
            //pre populate
            for(int i=0; i<5; i++){
                log(server.put(String.valueOf(UUID.randomUUID()),  "key" + i, "val" + i));
                log(server.get(String.valueOf(UUID.randomUUID()), "key" + i));
                log(server.delete(String.valueOf(UUID.randomUUID()),"key" + i));
            }


            String KEY = "";
            String VALUE = "";

            // Client UI access
            System.out.println("Enter 1 for PUT operation, 2 for GET operation, 3 for DELETE operation, and 4 for exit");
            while (true) {
                int operation = Integer.parseInt(reader.readLine());
                if (operation >= 1 && operation <= 4) {

                    if (operation == 1) {
                        System.out.println("Enter KEY");
                        KEY = reader.readLine();
                        System.out.println("Enter VALUE");
                        VALUE = reader.readLine();
                        log("Request sent to server for PUT operation with key = " + KEY + " and value = " + VALUE);
                        log("Response from server : " + server.put(String.valueOf(UUID.randomUUID()), KEY, VALUE));

                    } else if (operation == 2) {
                        System.out.println("Enter KEY to do GET in HASHMAP");
                        KEY = reader.readLine();
                        log("Request sent to server for GET operation with key = " + KEY);
                        log("Response from server : " + server.get(String.valueOf(UUID.randomUUID()),  KEY));

                    } else if (operation == 3) {
                        System.out.println("Enter KEY to do DELETE in HASHMAP");
                        KEY = reader.readLine();
                        log("Request sent to server for DELETE operation with key = " + KEY);
                        log("Response from server : " + server.delete(String.valueOf(UUID.randomUUID()), KEY));

                    } else if (operation == 4) {
                        break;
                    }
                } else {
                    System.out.println("Enter correct operation");
                }
            }
        }
        catch (Exception e){
            log("Server exception : " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static String getCurrentTimeStamp()
    {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date());
    }

    public static void log(String msg){
        System.out.println(getCurrentTimeStamp() +  " : " + msg);
    }
}


