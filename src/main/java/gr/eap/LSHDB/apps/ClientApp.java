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
import java.net.ConnectException;
import java.net.UnknownHostException;
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
        QueryRecord query = new QueryRecord(100);
        query.setKeyedField("author", new String[]{s}, 1, true);
        Result r = null;
        try {
            r = lsh.query(query);
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

    public static void tcpQuery(String s) {

        int n = 100;
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            int a = i;
            threads[i] = new Thread(new Runnable() {
                public void run() {
                  for (int j=0;j<1000;j++){  
                    try {
                        Thread.sleep(200);
                        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                        int t = bean.getThreadCount();
                        //System.out.println("t="+t);    
                        QueryRecord query = new QueryRecord("dblp", 100);
                        query.setKeyedField("author", new String[]{s}, .8, true);
                        final Client c = new Client("localhost", 4443);
                        Result r = c.queryServer(query);
                        System.out.println(a + ".  " + r.getRecords().size() + " " + r.getTime());
                    } catch (NodeCommunicationException ex) {
                        System.out.println(ex.getMessage());
                    } catch (InterruptedException ex) {
                        System.out.println(ex.getMessage());
                    }
                 }
                }
            });
            threads[i].start();
        }

    }

    public static void main(String[] args) {
        ClientApp app = new ClientApp();
        app.query("Becker");
        //ClientApp.tcpQuery("Chris");
    }

}
