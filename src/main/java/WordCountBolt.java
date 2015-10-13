import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mayanka on 17-Sep-15.
 * Modified by Hastimal on 10-oct-2015
 */
public class WordCountBolt extends BaseBasicBolt {
    Map<String, Integer> counts = new HashMap<String, Integer>();

    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        String word = tuple.getStringByField("words");
        Integer count = counts.get(word);
        if (count == null)
            count = 0;
        count++;
        counts.put(word, count);
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(new File("output"), true));
            br.append(word + ":" + count + "\n");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        insertIntoMongoDB(word, count);
        basicOutputCollector.emit(new Values(word, count));

    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }


    public static void insertIntoMongoDB(String word, Integer count) {
        try {
           // URL url = new URL("https://api.mongolab.com/api/1/databases/cs5543/collections/TwitterWordCount?apiKey=FqMHhDW_NfxEBuo6BZ67IlskGbAAdr2Z");
            URL url = new URL("https://api.mongolab.com/api/1/databases/twitterdbkafka/collections/twitterdbkafkacollection?apiKey=Zzl0OZUNbG988MQrWklonTyiuqYg90YA");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            String input = "{\"word\":\"" + word + "\",\"count\":\"" + count + "\",\"time\":\"" + System.currentTimeMillis() + "\"}";

            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("The code is " + conn.getResponseMessage());
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();
        } catch (Exception e) {

            e.printStackTrace();

        }

    }
}
