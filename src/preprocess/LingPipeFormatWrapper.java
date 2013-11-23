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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.StringUtils;

public class LingPipeFormatWrapper {

    private Properties props;
    private CRFClassifier<CoreLabel> segmenter;
    private MaxentTagger tagger;
    private TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer
            .factory(new CoreLabelTokenFactory(), "untokenizable=noneKeep");

    public LingPipeFormatWrapper(Properties p) {
        props = p;
        segmenter = new CRFClassifier<CoreLabel>(props);
        segmenter.loadClassifierNoExceptions("data/ctb.gz", props);
        tagger = new MaxentTagger("./data/models/chinese-distsim.tagger");
    }

    public void processLabelled(String csv, String opath) throws IOException {
        int count = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csv), "UTF-8"));

        Map<String, List<List<String>>> map = new HashMap<String, List<List<String>>>();
        Pattern p = Pattern.compile("[^\\p{L}]|[A-Za-z0-9]+");
        while (br.ready()) {
            String[] splits = br.readLine().split("\t");
            String sent = splits[0];
            String label = splits[1];
            List<String> res = segmenter.segmentString(sent);
            BufferedReader r = new BufferedReader(new StringReader(
                    StringUtils.join(res, " ")));
            DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(
                    r);
            documentPreprocessor.setTokenizerFactory(ptbTokenizerFactory);
            for (List<HasWord> sentence : documentPreprocessor) {
                List<TaggedWord> tSentence = tagger.tagSentence(sentence);
                List<String> feat = new LinkedList<String>();
                for (TaggedWord taggedWord : tSentence) {
                    if (taggedWord.tag().equals("NN")
                            || taggedWord.tag().equals("VV")
                    /* || taggedWord.tag().equals("JJ") */) {
                        String tw = taggedWord.word().trim();
                        if (/* tw.length() > 1 && */!tw.equals("-LRB-")
                                && !tw.equals("-RRB-")) {
                            Matcher m = p.matcher(tw);
                            if (!m.matches())
                                feat.add(tw);
                        }
                    }
                }
                if (!map.containsKey(label))
                    map.put(label, new LinkedList<List<String>>());
                map.get(label).add(feat);

                System.out.print(count++ + "\t");
                if (count % 10 == 0)
                    System.out.println();
            }
        }
        br.close();

        for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();) {
            String key = iter.next();
            List<List<String>> featList = map.get(key);
            File o = new File(opath + "/" + key + "/labeledCorpus.txt");
            File dir = new File(o.getParent());
            if (!dir.exists())
                dir.mkdirs();
            if (o.exists())
                o.delete();
            o.createNewFile();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(o), "UTF-8"));

            for (List<String> flist : featList) {
                for (String f : flist) {
                    bw.append(f);
                    bw.append(" ");
                }
                bw.append("\n");
            }
            bw.close();
        }

    }

    public void processUnlabelled(String csv, String opath) throws IOException {
        // POS tagger model
        int count = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csv), "UTF-8"));

        File out = new File(opath);
        if (!out.exists()) {
            File dir = new File(out.getParent());
            dir.mkdirs();
            out.createNewFile();
        }
        Pattern p = Pattern.compile("[^\\p{L}]|[A-Za-z0-9]+");

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(out), "UTF-8"));
        while (br.ready()) {
            String sent = br.readLine();
            // translate format to match POS tagger input
            List<String> res = segmenter.segmentString(sent);
            BufferedReader r = new BufferedReader(new StringReader(
                    StringUtils.join(res, " ")));
            DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(
                    r);

            documentPreprocessor.setTokenizerFactory(ptbTokenizerFactory);
            for (List<HasWord> sentence : documentPreprocessor) {
                List<TaggedWord> tSentence = tagger.tagSentence(sentence);
                List<String> feat = new LinkedList<String>();
                for (TaggedWord taggedWord : tSentence) {
                    if (taggedWord.tag().equals("NN")
                            || taggedWord.tag().equals("VV")
                    /* || taggedWord.tag().equals("JJ") */) {
                        String tw = taggedWord.word().trim();
                        if (tw.length() > 1 && !tw.equals("-LRB-")
                                && !tw.equals("-RRB-")) {
                            Matcher m = p.matcher(tw);
                            if (!m.matches())
                                feat.add(tw);
                        }
                    }
                }
                for (String f : feat) {
                    bw.append(f);
                    bw.append(" ");
                }
                bw.append("\n");
            }
            System.out.print(count++ + "\t");
            if (count % 10 == 0)
                System.out.println();
        }
        br.close();
        bw.close();
    }

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

        LingPipeFormatWrapper i2f = new LingPipeFormatWrapper(props);
        i2f.processLabelled("./experiment/lpart1.csv",
                "./experiment/abstract/test");
        i2f.processLabelled("./experiment/rpart1.csv",
                "./experiment/abstract/train");
        // i2f.unlabeledFeatureSet("./experiment/ulabeled.csv",
        // "./experiment/abstract/unlabeledCorpus.txt");
    }
}
