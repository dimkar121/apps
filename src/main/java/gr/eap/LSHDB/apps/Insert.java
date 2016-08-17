/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.apps;

import gr.eap.LSHDB.HammingConfiguration;
import gr.eap.LSHDB.HammingKey;
import gr.eap.LSHDB.HammingLSHStore;
import gr.eap.LSHDB.Key;
import gr.eap.LSHDB.StoreInitException;
import gr.eap.LSHDB.util.FileUtil;
import gr.eap.LSHDB.util.Record;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.util.StringTokenizer;

/**
 *
 * @author dimkar
 */
public class Insert {

    public static void main(String[] args) {
        try {
            String folder = "c:/MAPDB";
            String storeName = "dblp";
            String engine = "gr.eap.LSHDB.MapDB";
            Key key1 = new HammingKey("author", 30, .1, 75, 700, true, true);
            //Key key2 = new HammingKey("title", 30, .1, 55, 500, true, true);

            HammingConfiguration hc = new HammingConfiguration(folder, storeName, engine, new Key[]{key1}, true);
            hc.saveConfiguration();

            HammingLSHStore lsh = new HammingLSHStore(folder, storeName, engine, hc, true);

            String file = "c:/voters/dblp.txt"; // Specify  the full path of dblp.txt   

            int lines = FileUtil.countLines(file);
            System.out.println("About to insert " + lines + " records.");

            FileReader input1 = new FileReader(file);
            BufferedReader bufRead1 = new BufferedReader(input1);
            //lines = lsh.countLines(file);
            Random r = new Random();
            for (int i = 0; i < lines; i++) {
                String line1 = bufRead1.readLine();
                StringTokenizer st1 = new StringTokenizer(line1, ",");
                String id = st1.nextToken().trim(); //id

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
                //String[] titles = title.split(" ");
                //rec.set("title_tokens", titles);

                rec.set("pages", pages);
                if (type.equals("article")) {
                    rec.set("journal", journal);
                } else {
                    rec.set("conference or workshop", journal);
                }

                if (authors.length > 1) { // We insert those first authors, who have at least one given name and one surname.                    
                    rec.set("author" + Key.TOKENS, new String[]{authors[authors.length - 1]});
                    lsh.insert(rec);
                }

                System.out.println(i);

            }
            lsh.close();
        }catch (StoreInitException ex){
             ex.getMessage();
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }
    }
}
