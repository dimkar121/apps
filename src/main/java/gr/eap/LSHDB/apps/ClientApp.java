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
            System.out.println(HammingLSHStore.open(storeName).query(q).  asJSON());
        } catch (NoKeyedFieldsException | StoreInitException ex) {
            ex.printStackTrace();
        }
        //lsh.close();
    }

    public static void tcpQuery(String s) {

        int n = 50;
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            int a = i;
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < 100; j++) {
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
        app.query("Kucherov");
       // ClientApp.tcpQuery("Chris");
    }

}
