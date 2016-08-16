package gr.eap.LSHDB.apps;

import gr.eap.LSHDB.HammingConfiguration;
import gr.eap.LSHDB.HammingKey;
import gr.eap.LSHDB.HammingLSHStore;
import gr.eap.LSHDB.Key;
import gr.eap.LSHDB.NoKeyedFieldsException;
import gr.eap.LSHDB.StoreInitException;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.Result;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dimkar
 */
public class ClientApp {

    HammingLSHStore lsh;
    String storeName = "dblp";

    public ClientApp() {
        try {
            String folder = "c:/MAPDB";
            String engine = "gr.eap.LSHDB.MapDB";
            lsh = new HammingLSHStore(folder, storeName, engine);
            
        } catch (StoreInitException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }
    }

    public void query(String s) {
        QueryRecord query = new QueryRecord(40);
        query.setKeyedField("author", new String[]{s}, .8, true);
        Result r = null;
        try {
            r=lsh.query(query);
            r.prepare();
            ArrayList<Record> arr = r.getRecords();
            for (int i = 0; i < arr.size(); i++) {
                Record rec = arr.get(i);
                System.out.println(rec.get("author") + " " + rec.get("title"));
            }
        } catch (NoKeyedFieldsException ex) {
            ex.printStackTrace();
        }
        lsh.close();
    }

    public static void main(String[] args) {
        ClientApp app = new ClientApp();
        app.query("Becker");
    }

}
