package preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class InputToFeatureSet {

    public static void main(String[] args) throws IOException {
        // load segmenter model
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", "data");
        // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
        // props.setProperty("normTableEncoding", "UTF-8");
        // below is needed because CTBSegDocumentIteratorFactory accesses it
        props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
        // props.setProperty("testFile", args[0]);
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");

        CRFClassifier<CoreLabel> segmenter = new CRFClassifier<CoreLabel>(props);
        segmenter.loadClassifierNoExceptions("data/ctb.gz", props);

        // POS tagger model
        MaxentTagger tagger = new MaxentTagger(
                "./data/models/chinese-distsim.tagger");
        TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.factory(
                new CoreLabelTokenFactory(), "untokenizable=noneKeep");

        int count = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("./experiment/ulabeled2.csv"), "UTF-8"));
//        Map<String, List<List<String>>> map = new HashMap<String, List<List<String>>>();
        List<List<String>> ufeatList = new LinkedList<List<String>>();
        while (br.ready()) {
            //String[] splits = br.readLine().split("\t");
            //String sent = splits[0];
            //String label = splits[1];
            String sent = br.readLine();
            // translate format to match POS tagger input
            List<String> res = segmenter.segmentString(sent);
            StringBuilder sb = new StringBuilder();
            for (String w : res) {
                sb.append(w);
                sb.append(" ");
            }
            BufferedReader r = new BufferedReader(new StringReader(
                    sb.toString()));
            DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(
                    r);
            documentPreprocessor.setTokenizerFactory(ptbTokenizerFactory);
            for (List<HasWord> sentence : documentPreprocessor) {
                List<TaggedWord> tSentence = tagger.tagSentence(sentence);
                List<String> feat = new LinkedList<String>();
                for (TaggedWord taggedWord : tSentence) {
                    if (taggedWord.tag().equals("NN")
                            || taggedWord.tag().equals("VV")
                            || taggedWord.tag().equals("JJ")) {
                        if (!taggedWord.equals("-LRB-") 
                                && !taggedWord.equals("-RRB-"))
                            feat.add(taggedWord.word());
                    }
                }
                //if (!map.containsKey(label))
                //    map.put(label, new LinkedList<List<String>>());
                //map.get(label).add(feat);
                ufeatList.add(feat);
            }
            System.out.print(count++ + "\t");
            if (count % 10 == 0)
                System.out.println();
        }

        br.close();

        //for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();) {
            //String key = iter.next();
            //List<List<String>> featList = map.get(key);
            String path = "./experiment/abstract/unlabeledCorpus.txt";
            File out = new File(path);
            if (!out.exists()) {
                File dir = new File(out.getParent());
                dir.mkdirs();
                out.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(out), "UTF-8"));

            for (List<String> feat : ufeatList) {
                for (String f : feat) {
                    bw.append(f);
                    bw.append(" ");
                }
                bw.append("\n");
            }
            bw.close();
       // }

    }
}
