import java.io.*;
import java.util.HashMap;

public class test {
    public static void main(String args[])
    {        
        HashMap<Integer,String> ledger = new HashMap<Integer,String>();
        ledger.put(1,"Tom");
        ledger.put(2,"Gabca");

        String value = ledger.get(1);
        System.out.println(value);

        String doesntExist = ledger.get(5);
        System.out.println(doesntExist);

    }
}