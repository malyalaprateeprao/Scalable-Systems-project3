import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
    Coordinator class helps to get the server ip address, port and remote interface class object and binding them
    to the specified host and port.
 */

public class Coordinator
{

    public static void main(String args[]) throws Exception
    {

        if(args.length < 2) {
            log("Required 2 fields, Server and port no");
            System.exit(0);
        }

        try {

            int port = Integer.parseInt(args[1]);
            ServerInterface server = new Server(args[1]);
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("ServerInterface", stub);
            log("Server is listening on port " + args[1]);
        }
        catch (Exception e) {
            log(e.getMessage());
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
