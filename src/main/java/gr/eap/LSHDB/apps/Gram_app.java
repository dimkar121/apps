/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.apps;

import gr.eap.LSHDB.DataStore;
import gr.eap.LSHDB.GramConfiguration;
import gr.eap.LSHDB.GramKey;
import gr.eap.LSHDB.GramStore;
import gr.eap.LSHDB.Key;
import gr.eap.LSHDB.StoreInitException;
import gr.eap.LSHDB.embeddables.StringEmb;
import gr.eap.LSHDB.util.Record;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

/**
 *
 * @author dimkar
 */
public class Gram_app {
    
   
    public static void main(String[] args) {
        try {
            String folder = "c:/LEVELDB";
            String storeName = "dblp_grams";
            String engine = "gr.eap.LSHDB.LevelDB";
            GramStore store;
            int base = 0;
            if (! DataStore.exists(folder,storeName)){
                Key key1 = new GramKey("author", .1, 2,2,'%', true, true, new StringEmb(2,'%'));            
                GramConfiguration hc = new GramConfiguration(folder, storeName, engine, new Key[]{key1}, true);
                hc.saveConfiguration();
                store = new GramStore(folder, storeName, engine, hc, true);
            } else {
                store =(GramStore) GramStore.open(storeName);
            }    
            
            
            
         
           
            String file = "c:/voters/dblp.txt"; // Specify  the full path of dblp.txt   
            //UUID u = UUID.randomUUID();
            
            FileReader input1 = new FileReader(file);
            BufferedReader bufRead1 = new BufferedReader(input1);
            
            int lines = 5300000;
           
            for (int i = 0; i < lines-1; i++) {
                String line1 = bufRead1.readLine();
                StringTokenizer st1 = new StringTokenizer(line1, ",");
                String id = (i)+""; //id
                st1.nextToken();

                String type = st1.nextToken().trim();
                String author = st1.nextToken().trim();
                String title = st1.nextToken().trim().replace(".", "");
                String year = "";
                if (st1.hasMoreTokens()) {
                    year = st1.nextToken().trim();
                }
                String journal = ""; // journal or conference
                if (st1.hasMoreTokens()) {
                    journal = st1.nextToken().trim();
                }
                String pages = "";
                if (st1.hasMoreTokens()) {
                    pages = st1.nextToken().trim();
                }
               
                
                Record rec = new Record();
                rec.setId(id);

                rec.set("author", author);
                String[] authors = author.split(" ");

                rec.set("year", year);

                rec.set("title", title);

                rec.set("pages", pages);
                if (type.equals("A")) {
                    rec.set("J", journal);
                } else {
                    rec.set("C", journal);
                }

                if (authors.length > 1) { // We insert those first authors, who have at least one given name and one surname.                    
                    rec.set("author" + Key.TOKENS, new String[]{authors[authors.length - 1]});
                    store.insert(rec);
                }

                System.out.println(id);

            }
            
            
            /*
            QueryRecord query = new QueryRecord(50); // n denotes the max number of the returned records.
            query.setKeyedField("author", new String[]{"christen"},1.0,true);
            Result result = store.query(query);
            result.prepare();
            System.out.println(result.asJSON());
            */
       
            
            
            
            store.close();
        }catch (StoreInitException ex){
             ex.getMessage();
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }
    }
    
    
    
}
