package gr.eap.LSHDB.apps;

import gr.eap.LSHDB.DataStore;
import gr.eap.LSHDB.HammingLSHStore;
import gr.eap.LSHDB.NoKeyedFieldsException;
import gr.eap.LSHDB.StoreInitException;
import gr.eap.LSHDB.util.QueryRecord;

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


    public void query(String s) {
        QueryRecord q =  new QueryRecord(100);
        q.setKeyedField("author", new String[]{s}, 0.8, false);
        try {
            DataStore d = HammingLSHStore.open(storeName);
            System.out.println(d.query(q).asJSON());
            d.close();
            d.getConfiguration().close();
        } catch (NoKeyedFieldsException | StoreInitException ex) {
            ex.printStackTrace();
        }        
        //lsh.close();
    }

   

    public static void main(String[] args) {
        ClientApp app = new ClientApp();
        app.query("Karapiperis");       
    }

}
