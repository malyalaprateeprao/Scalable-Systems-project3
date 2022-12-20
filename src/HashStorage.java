import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class HashStorage {


    /*
      this class deals with storing of data......
     */

    HashMap<String, String> h = new HashMap<>();
    public HashStorage() {
        log("New Storage created for the server");
        h = new HashMap<>();
    }

    public synchronized String get(String key) {
        //System.out.println(h);
        if(h.containsKey(key)){
            return "Get Operation success, key = " + key + " and value = " + h.get(key);
        }
        else{
            return "Get Operation fail, not able to find key = " + key + " in hashmap";
        }
    }

    public synchronized String put(String key, String value) {
        //System.out.println(h);
        h.put(key, value);
        return "Put Operation success, key = " + key + " and value = " + value  + " added Successfully";
    }

    public synchronized String delete(String key) {
        //System.out.println(h);
        if(h.containsKey(key)){
            String v = h.get(key);
            h.remove(key);
            return "Delete Operation success, key = " + key + " and value = " + v + " deleted Successfully";
        }
        else{
            return "Delete Operation fail, not able to find key= " + key +  " in hashmap";
        }
    }

    public String getCurrentTimeStamp()
    {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date());
    }

    public void log(String msg){
        System.out.println(getCurrentTimeStamp() +  " : " + msg);}
}
