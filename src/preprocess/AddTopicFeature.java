package preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AddTopicFeature {
    private List<List<String>> extFeature = new ArrayList<List<String>>();
    private Map<String, Set<String>> map = new HashMap<String, Set<String>>();
    
    public AddTopicFeature(String topicSrc) throws IOException {
        int count = 0;
        BufferedReader extFeatureReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(
                        topicSrc), "UTF-8"));
        while (extFeatureReader.ready()) {
            String[] tokens = extFeatureReader.readLine().split("\t");
            for (String t : tokens) {
                if (!map.containsKey(t))
                    map.put(t, new TreeSet<String>());
                map.get(t).add("feat"+count);
            }
            count++;
            extFeature.add(Arrays.asList(tokens));
        }
        extFeatureReader.close();
    }
    
    public void add(String src, String dst) throws IOException {
        BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(
                        src), "UTF-8"));
        BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(
                        dst), "UTF-8"));
        
        while (r.ready()) {
            String line = r.readLine();
            String[] splits = line.split("\t");
            String feat = splits[0];
            String clazz = null;
            if (splits.length > 1) 
                clazz = splits[1];
            String[] tokens = feat.split(" ");
            for (String t : tokens) {
                w.append(t);
                w.append(" ");
                if (map.containsKey(t)) {
                    Set<String> topicFeat = map.get(t);
                    for (String topic : topicFeat) {
                        w.append(topic);
                        w.append(" ");
                    }
                }
            }
            if (clazz != null) {
                w.append("\t");
                w.append(clazz);
            }
            w.newLine();
        }
        r.close();
        w.close();
    }
    
    public static void main(String[] args) throws IOException {
        AddTopicFeature atf = new AddTopicFeature("exper3/knowledge.txt");
//        atf.add("exper3/all.seged.csv", "exper3/all_topoc.csv");
        atf.add("exper3/unlabeled.clean.seged.csv", "exper3/unlabeled.topic.csv");
    }

}
