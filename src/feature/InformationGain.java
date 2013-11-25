package feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class InformationGain {

    private int docNum = 0;
    private Map<String, Integer> tMap; // document frequency that term occurs
    private Map<String, Map<String, Integer>> ctMap; // class, term frequency
    private Map<String, Integer> cMap; // class frequency
    private Map<String, Double> igMap; // ig of each term

    public InformationGain() {
        tMap = new HashMap<String, Integer>();
        cMap = new HashMap<String, Integer>();
        ctMap = new HashMap<String, Map<String, Integer>>();
        igMap = new HashMap<String, Double>();
    }

    public InformationGain(String dir) throws IOException {
        this();

        File fdir = new File(dir);
        if (!fdir.isDirectory()) {
            System.err.println("need Lingpipe train directory architecture");
            return;
        }

        for (File cf : fdir.listFiles()) {
            String c = cf.getName();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(cf, "labeledCorpus.txt")),
                    "UTF-8"));

            // term frequency of class c
            Map<String, Integer> cttMap;
            if (ctMap.containsKey(c))
                cttMap = ctMap.get(c);
            else {
                cttMap = new HashMap<String, Integer>();
                ctMap.put(c, cttMap);
            }

            while (br.ready()) {
                docNum++;
                // class frequency statistics
                if (cMap.containsKey(c))
                    cMap.put(c, cMap.get(c) + 1);
                else
                    cMap.put(c, 1);

                String line = br.readLine();
                String[] terms = line.split(",");
                // term frequency statistics
                Set<String> occured = new TreeSet<String>();
                for (String t : terms) {
                    if (!occured.contains(t)) {
                        if (tMap.containsKey(t))
                            tMap.put(t, tMap.get(t) + 1);
                        else
                            tMap.put(t, 1);

                        if (cttMap.containsKey(t))
                            cttMap.put(t, cttMap.get(t) + 1);
                        else
                            cttMap.put(t, 1);

                        occured.add(t);
                    }
                }
            }
            br.close();
        }
        calcInformationGain();
        
    }

    public Map<String, Double> get() {
        return igMap;
    }

    private double entropy(List<Double> X) {
        double e = 0;
        double s = 0;

        for (double x : X) {
            if (Double.isNaN(x))
                return 0;
            if (Double.compare(x, 0) != 0) {
                e += x * Math.log(x);
                s += x;
            }
        }
        assert Math.abs(Double.doubleToLongBits(s)
                - Double.doubleToLongBits(1d)) <= 2;
        return -e;
    }

    private void calcInformationGain() {
        // calc P(C)
        List<Double> pc = new LinkedList<Double>();
        for (Iterator<String> iter = cMap.keySet().iterator(); iter.hasNext();) {
            String c = iter.next();
            pc.add(cMap.get(c) / (double) docNum);
        }

        double hc = entropy(pc);

        Set<String> terms = tMap.keySet();
        Set<String> classes = cMap.keySet();
        for (Iterator<String> term = terms.iterator(); term.hasNext();) {
            String t = term.next();
            double ig = hc;

            // 出现t的文档数
            int docNumContainT = tMap.get(t);
            // 没出现t的文档数
            int docNumNotContainT = docNum - docNumContainT;
            double pt = docNumContainT / (double) docNum;
            double pnt = 1 - pt;

            List<Double> ciGivenTList = new LinkedList<Double>();
            List<Double> ciGivenNTList = new LinkedList<Double>();
            for (Iterator<String> clazz = classes.iterator(); clazz.hasNext();) {
                String c = clazz.next();
//                System.out.println("Processing ..." + c + " " + t);
                assert ctMap.containsKey(c);
                // ci中出现t的文档数
                int docNumInCContainT;
                if (ctMap.get(c).containsKey(t))
                    docNumInCContainT = ctMap.get(c).get(t);
                else {
                    docNumInCContainT = 0;
//                    System.out.println("class = " + c + " <-> term =  " + t);
                }
                // ci中的文档数
                int docNumInC = cMap.get(c);

                // P(c_i | t)
                double ciGivent = docNumInCContainT / (double) docNumContainT;
                ciGivenTList.add(ciGivent);

                // ci中没出现t的文档数
                int docNumInCNotContainT = docNumInC - docNumInCContainT;
                // P(c_i | not t)
                double ciGivenNT = docNumInCNotContainT
                        / (double) docNumNotContainT;
                ciGivenNTList.add(ciGivenNT);
            }
            assert ciGivenTList.size() == ciGivenNTList.size();
            // System.out.println("Size = " + ciGivenTList.size());
            ig -= pt * entropy(ciGivenTList);
            ig -= pnt * entropy(ciGivenNTList);

            final int SCALER = 100;
            igMap.put(t, ig * SCALER);
        }
        igMap = sortByValue(igMap);
    }

    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
            Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
                map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream("./ig_map.txt"));
        InformationGain ig = new InformationGain("./experiment/abstract/train/");
        Map<String, Double> igMap = ig.get();

        for (Iterator<String> term = igMap.keySet().iterator(); term
                .hasNext();) {
            String t = term.next();
            System.out.println(t + ": " + igMap.get(t));
        }
    }
}
