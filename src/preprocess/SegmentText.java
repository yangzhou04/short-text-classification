package preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;

public class SegmentText {
    private static Set<String> stopwordSet = new TreeSet<String>();
    private static Segmenter seg = new Segmenter();
    private static Tagger tagger = new Tagger();
    
    public static void segmentLabeled(String src, String dst) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(src), "UTF-8"));
        File f = new File(dst);
        if (!f.exists())
            f.createNewFile();
        else
            System.err.println("Warning: " + f + " exists, overwriting happens");

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "UTF-8"));

        while (in.ready()) {
            String line = in.readLine();
            String[] splits = line.split("\t");

            if (splits.length == 2) {
                String text = splits[0];
                String clazz = splits[1];
                text = text.replaceAll(TextFilter.DIGIT, "")
                        .replaceAll(TextFilter.ALPHA, "")
                        .replaceAll(TextFilter.PARENTHESIS, "")
                        .replaceAll(TextFilter.SPACE, "")
                        .replaceAll(TextFilter.EQUATION_TAG, "");
                List<Word> words = seg.segmentSentence(text);
                List<TaggedWord> taggedWords = tagger.tagSentence(words);
                for (TaggedWord word : taggedWords) {
                    if (!stopwordSet.contains(word.word())
                            && !word.tag().equals("PU")) {
                        out.append(word.word());
                        out.append(" ");
                    }
                }
                out.append("\t");
                out.append(clazz);
                out.newLine();
            } else {
                System.err.println("Warning: splits count != 2 => " + line);
            }
        }
        out.close();
        in.close();
    }

    public static void segmentUnlabeled(String src, String dst) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(src), "UTF-8"));

        File f = new File(dst);
        if (!f.exists())
            f.createNewFile();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "UTF-8"));

        while (in.ready()) {
            String text = in.readLine();
            text = text.replaceAll(TextFilter.PARENTHESIS, "")
                    .replaceAll(TextFilter.SPACE, "")
                    .replaceAll(TextFilter.EQUATION_TAG, "");
            List<Word> words = seg.segmentSentence(text);
            List<TaggedWord> taggedWords = tagger.tagSentence(words);
            for (TaggedWord word : taggedWords) {
                if (!stopwordSet.contains(word.word())
                        && !word.tag().equals("PU")) {
                    out.append(word.word());
                    out.append(" ");
                }
            }
            out.newLine();
        }
        out.close();
        in.close();
    }

    public static void readStopwords(String src) throws IOException {
        BufferedReader stopwordReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(
                        src), "UTF-8"));
        while (stopwordReader.ready())
            stopwordSet.add(stopwordReader.readLine().trim());
        stopwordReader.close();
    }
    
    public static void main(String[] args) throws IOException {
        SegmentText.readStopwords("exper/stopwords.txt");
//        segmentLabeled("exper/labeled/labeled.old.csv", 
//                "exper/labeled/labeled.old.clean.seged.csv");
//        segmentLabeled("exper/labeled/labeled.new.csv", 
//                "exper/labeled/labeled.new.clean.seged.csv");
        SegmentText.segmentUnlabeled("exper/unlabeled/unlabeled.csv", 
                "exper/unlabeled/unlabeled.clean.seged.csv");
    }

}
