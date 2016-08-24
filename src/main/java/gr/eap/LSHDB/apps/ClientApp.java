package gr.eap.LSHDB.apps;

import gr.eap.LSHDB.HammingLSHStore;
import gr.eap.LSHDB.NoKeyedFieldsException;
import gr.eap.LSHDB.NodeCommunicationException;
import gr.eap.LSHDB.StoreInitException;
import gr.eap.LSHDB.client.Client;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.Result;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
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


    public void query(String s) {
        QueryRecord q =  new QueryRecord(100);
        q.setKeyedField("author", new String[]{s}, 0.8, true);
        try {
            System.out.println(HammingLSHStore.open(storeName).query(q).asJSON());
        } catch (NoKeyedFieldsException | StoreInitException ex) {
            ex.printStackTrace();
        }
        lsh.close();
    }

   

    public static void main(String[] args) {
        ClientApp app = new ClientApp();
        app.query("Kucherov");       
    }

}
