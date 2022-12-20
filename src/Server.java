import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static java.rmi.server.RemoteServer.getClientHost;

/*

      this class implements the 2phase locking protocol (growing phase (prepare requests) and committing shrinking phase (committing and go))

 */



public class Server implements ServerInterface{


    HashStorage storage= null;
    HashMap<String, HashMap<String, Boolean>> prepare;
    HashMap<String, HashMap<String, Boolean>> go;

    HashMap<String, data> dataTmp;

    ArrayList<String> allServers;

    String thisServer;

    public Server(String server) {
        this.thisServer = server;
        storage = new HashStorage();
        prepare = new HashMap<>();
        go = new HashMap<>();
        dataTmp = new HashMap<>();
    }



    @Override
    public void updateAllServers(ArrayList<String> allServers) throws RemoteException {
        this.allServers = allServers;
    }

    public String Dealer(String id) throws UnknownHostException, NotBoundException, RemoteException, InterruptedException {

        prepare(id);
        boolean pre = ACKprepare(id);

        if(!pre){
            return "fail to get prepare ack from all servers, after doing a 3 retries";
        }
        else {
            log("All servers accepted prepare request");
        }

        go(id);
        boolean go = ACKgo(id);

        if(!go){
            return "fail to get go ack from all servers, after doing a 3 retries";
        }
        else{
            log("All servers accepted go request, and the transaction is committed on all servers");
        }

        return "Successfully done with the Transaction, do a get operation to validate it";
    }

    @Override
    public String get(String id, String key) throws RemoteException, ServerNotActiveException {
        log("Received a get request to the server from client for a key = " + key + " from Client host " + getClientHost());
        return storage.get(key);
    }

    @Override
    public String put(String id, String key, String value) throws RemoteException, ServerNotActiveException, UnknownHostException, NotBoundException, InterruptedException {
        log("Received a put request to the server from client  with key = " + key + " and value = " + value + " from Client host " + getClientHost());
        dataTmp.put(id, new data(1, key, value));
        return Dealer(id);
    }

    @Override
    public String delete(String id, String key) throws RemoteException, ServerNotActiveException, UnknownHostException, NotBoundException, InterruptedException {
        log("Received a delete request to the server from client for a key = " + key + " from Client host " + getClientHost());
        dataTmp.put(id, new data(2, key, ""));
        return Dealer(id);
    }


    //growing phase
    public boolean ACKprepare(String id) throws UnknownHostException, RemoteException, NotBoundException, InterruptedException {

        int countOfServers = allServers.size();
        int count =0;
        int retry = 3;

        while(retry > 0) {

            count = 0;
            HashMap<String, Boolean> tmp = prepare.get(id);

            for(String it: tmp.keySet()) {

                if(tmp.get(it)){
                    count ++;
                }
                else{
                    try {

                        InetAddress ip = InetAddress.getLocalHost();
                        Registry registry = LocateRegistry.getRegistry(ip.getHostAddress(), Integer.parseInt(it));
                        ServerInterface server = (ServerInterface) registry.lookup("ServerInterface");
                        server.getAcknowledgeFromEachServerForPrepare(id, thisServer);
                    }
                    catch (Exception e) {
                        log("exception msg = " + e.getMessage() + "exception stack trace = " + e.getStackTrace());
                        continue;
                    }
                }
            }
            if (count == countOfServers) {
                return true;
            }
            retry --;

            log("Keep the thread to sleep for some time before doing a retry, in prepare block" + thisServer);
            synchronized (this) {

                try {
                    this.wait(100000);  // wait for some time before doing a retry.
                }
                catch (Exception e) {
                    log("exception in wait" + e);
                }

            }
        }

        return false;
    }


    public void prepare(String id) {

        HashMap<String, Boolean> tmp = new HashMap<>();;
        for(int i=0; i<allServers.size(); i++){
            tmp.put(allServers.get(i), false);
        }
        prepare.put(id, tmp);


        for(int i=0; i<allServers.size(); i++) {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                Registry registry = LocateRegistry.getRegistry(ip.getHostAddress(), Integer.parseInt(allServers.get(i)));
                ServerInterface server = (ServerInterface) registry.lookup("ServerInterface");
                server.getAcknowledgeFromEachServerForPrepare(id, thisServer);
            }
            catch (Exception e){
                log("exception msg = " + e.getMessage() + "exception stack trace = " + e.getStackTrace());
                continue;
            }
        }
    }

    public void getAcknowledgeFromEachServerForPrepare(String id, String rootServer) throws UnknownHostException, NotBoundException, RemoteException {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            Registry registry = LocateRegistry.getRegistry(ip.getHostAddress(), Integer.parseInt(rootServer));
            ServerInterface server = (ServerInterface) registry.lookup("ServerInterface");

            server.acknowledgeForPrepare(id, thisServer);
        }
        catch (Exception e) {
            throw e;
        }
    }

    public void acknowledgeForPrepare(String id, String thisServer) {

        if(prepare.containsKey(id)){
            prepare.get(id).put(thisServer, true);
        }
    }








    //commit and shrinking phase
    public boolean ACKgo(String id) throws UnknownHostException, RemoteException, NotBoundException, InterruptedException {

        int countOfServers = allServers.size();
        int count =0;
        int retry = 3;

        while(retry > 0) {

            count = 0;
            HashMap<String, Boolean> tmp = go.get(id);
            for(String it: tmp.keySet()) {

                if(tmp.get(it)){
                    count ++;
                }
                else{
                    try {
                        data d = this.dataTmp.get(id);
                        InetAddress ip = InetAddress.getLocalHost();
                        Registry registry = LocateRegistry.getRegistry(ip.getHostAddress(), Integer.parseInt(it));
                        ServerInterface server = (ServerInterface) registry.lookup("ServerInterface");
                        server.getAcknowledgeFromEachServerForGo(id, thisServer, d.type, d.key, d.value);
                    }
                    catch (Exception e) {
                        log("exception msg = " + e.getMessage() + "exception stack trace = " + e.getStackTrace());
                        continue;
                    }
                }
            }
            if (count == countOfServers) {
                return true;
            }
            retry --;

            log("Keep the thread to sleep for some time before doing a retry in go block" + thisServer);
            synchronized (this) {

                try {
                    this.wait(100000);  // wait for some time before doing a retry.
                }
                catch (Exception e) {
                    log("exception in wait" + e);
                }

            }
        }

        return false;
    }

    public void go(String id) {

        HashMap<String, Boolean> tmp = new HashMap<>();;
        for(int i=0; i<allServers.size(); i++){
            tmp.put(allServers.get(i), false);
        }
        go.put(id, tmp);


        data d = this.dataTmp.get(id);
        for(int i=0; i<allServers.size(); i++) {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                Registry registry = LocateRegistry.getRegistry(ip.getHostAddress(), Integer.parseInt(allServers.get(i)));
                ServerInterface server = (ServerInterface) registry.lookup("ServerInterface");
                server.getAcknowledgeFromEachServerForGo(id, thisServer, d.type, d.key, d.value);
            }
            catch (Exception e){
                log("exception msg = " + e.getMessage() + "exception stack trace = " + e.getStackTrace());
                continue;
            }
        }

    }

    public void getAcknowledgeFromEachServerForGo(String id, String rootServer, int type, String key, String value) throws UnknownHostException, NotBoundException, RemoteException {
        try {

            //update storage
            if (type == 1) {
                this.storage.put(key, value);
            }
            else if(type == 2) {
                this.storage.delete(key);
            }

            InetAddress ip = InetAddress.getLocalHost();
            Registry registry = LocateRegistry.getRegistry(ip.getHostAddress(), Integer.parseInt(rootServer));
            ServerInterface server = (ServerInterface) registry.lookup("ServerInterface");

            server.acknowledgeForGo(id, thisServer);
        }
        catch (Exception e) {
            throw e;
        }
    }

    public void acknowledgeForGo(String id, String thisServer) {

        if(go.containsKey(id)){
            go.get(id).put(thisServer, true);
        }
    }


    public String getCurrentTimeStamp()
    {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date());
    }

    public void log(String msg){
        System.out.println(getCurrentTimeStamp() +  " : " + msg);}

}


class data {

    int type;   // 1 for put and 2 for delete
    String key;
    String value;

    data(int type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }
}