package feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class Tfidf {

    private List<Integer> docLen;
    private int docNum;
    private List<Map<String, Integer>> tc;
    private List<Map<String, Double>> tfidf;
    private Map<String, Integer> dc;

    public Tfidf(String dir) throws IOException {
        File fDir = new File(dir);
        if (!fDir.isDirectory()) {
            System.err.println("need Lingpipe train directory architecture");
            return;
        }

        docLen = new LinkedList<Integer>();
        tc = new LinkedList<Map<String, Integer>>();
        dc = new HashMap<String, Integer>();
        tfidf = new LinkedList<Map<String, Double>>();

        for (File cf : fDir.listFiles()) {
            for (File f : cf.listFiles()) {
                docNum++;
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(f), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                while (br.ready())
                    sb.append(br.readLine());

                StringTokenizer st = new StringTokenizer(sb.toString(), " ");
                docLen.add(st.countTokens());
                Map<String, Integer> tcPerDoc = new HashMap<String, Integer>();

                Set<String> seen = new TreeSet<String>();
                while (st.hasMoreTokens()) {
                    String term = st.nextToken();
                    if (tcPerDoc.containsKey(term))
                        tcPerDoc.put(term, tcPerDoc.get(term) + 1);
                    else
                        tcPerDoc.put(term, 1);

                    if (!seen.contains(term)) {
                        if (dc.containsKey(term))
                            dc.put(term, dc.get(term) + 1);
                        else
                            dc.put(term, 1);
                        seen.add(term);
                    }
                }
                tc.add(tcPerDoc);

                br.close();

            }
        }

        for (int i = 0; i < tc.size(); i++) {
            int len = docLen.get(i);
            Map<String, Integer> tcPerDoc = tc.get(i);

            Map<String, Double> tfidfPerDoc = new HashMap<String, Double>();
            for (Iterator<String> iter = tcPerDoc.keySet().iterator(); iter
                    .hasNext();) {
                String term = iter.next();
                int tc = tcPerDoc.get(term);
                double tf = tc / (double) len;

                double idf = Math.log(docNum / (double) dc.get(term));
                tfidfPerDoc.put(term, tf * idf);
            }
            tfidf.add(tfidfPerDoc);
        }
    }
    
    public List<Map<String, Double>> getTfidf() {
        return tfidf;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tfidf.size(); i++) {
            sb.append(tfidf.get(i));
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        //System.setOut(new PrintStream("./exper/tfidf.txt"));
        Tfidf ti = new Tfidf("./exper/abstracts/tmp/");
        System.out.println(ti);
    }
}
