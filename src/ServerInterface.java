import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;

public interface ServerInterface extends Remote {


    void updateAllServers(ArrayList<String> allServers) throws RemoteException;

    String get(String id, String key) throws RemoteException, ServerNotActiveException;

    String put(String id, String key, String value) throws RemoteException, ServerNotActiveException, UnknownHostException, NotBoundException, InterruptedException;

    String delete(String id,String key) throws RemoteException, ServerNotActiveException, UnknownHostException, NotBoundException, InterruptedException;

    void getAcknowledgeFromEachServerForPrepare(String id, String thisServer) throws RemoteException, UnknownHostException, NotBoundException;

    void acknowledgeForPrepare(String id, String thisServer) throws RemoteException;

    void getAcknowledgeFromEachServerForGo(String id, String thisServer, int type, String key, String value) throws RemoteException, UnknownHostException, NotBoundException;

    void acknowledgeForGo(String id, String thisServer) throws RemoteException;

}
