package test;

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

import preprocess.Segmenter;
import preprocess.Tagger;
import preprocess.TextFilter;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;

public class SegmentText {

    private static Set<String> stopwordSet = new TreeSet<String>();

    public static void segmentLabeled2() throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream("./exper/labeled2.csv"), "UTF-8"));
        File f = new File("./exper/labeled2.seged.csv");
        if (!f.exists()) f.createNewFile();
        else System.err.println("Warning: " + f + " exists, overwriting happens");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "UTF-8"));

        Segmenter seg = new Segmenter();
        Tagger tagger = new Tagger();
        Set<String> pos = new TreeSet<String>();
        pos.add("NN");
        pos.add("VV");
        while (in.ready()) {
            String line = in.readLine();
            String[] splits = line.split("\t");

            if (splits.length == 3) {
                String title = splits[0];
                String text = splits[1];
                String fullText = title + "。" + text;
                String clazz = splits[2];
                fullText = fullText.replaceAll(TextFilter.DIGIT, "")
                        .replaceAll(TextFilter.PARENTHESIS, "")
                        .replaceAll(TextFilter.SPACE, "")
                        .replaceAll(TextFilter.EQUATION_TAG, "");
                List<Word> words = seg.segmentSentence(fullText);
                List<TaggedWord> taggedWords = tagger.tagSentence(words);
                for (TaggedWord word : taggedWords) {
//                    if (word.word().length() > 1 && pos.contains(word.tag())) {
                    if (!stopwordSet.contains(word.word()) && !word.tag().equals("PU")) {
                        out.append(word.word());
                        out.append(" ");
                    }
//                    }
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
    
    public static void segmentLabeled() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream("./exper/labeled.clean.csv"), "UTF-8"));
        File f = new File("./exper/labeled.seged.csv");
        if (!f.exists()) f.createNewFile();
        else System.err.println("Warning: " + f + " exists, overwriting happens");
        
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "UTF-8"));

        Segmenter seg = new Segmenter();
        Tagger tagger = new Tagger();
        while (in.ready()) {
            String line = in.readLine();
            String[] splits = line.split("\t");

            if (splits.length == 2) {
                String text = splits[0];
                String clazz = splits[1];
                text = text.replaceAll(TextFilter.DIGIT, "")
                        .replaceAll(TextFilter.PARENTHESIS, "")
                        .replaceAll(TextFilter.SPACE, "")
                        .replaceAll(TextFilter.EQUATION_TAG, "");
                List<Word> words = seg.segmentSentence(text);
                List<TaggedWord> taggedWords = tagger.tagSentence(words);
                for (TaggedWord word : taggedWords) {
                    if(!stopwordSet.contains(word.word()) && !word.tag().equals("PU")) {
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

    public static void segmentUnlabeled() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream("./exper/ulabeled.clean.csv"), "UTF-8"));
        File f = new File("./exper/unlabeled.seged.csv");
        if (!f.exists())
            f.createNewFile();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "UTF-8"));

        Segmenter seg = new Segmenter();
        Tagger tagger = new Tagger();
        while (in.ready()) {
            String text = in.readLine();
            text = text.replaceAll(TextFilter.PARENTHESIS, "")
                    .replaceAll(TextFilter.SPACE, "")
                    .replaceAll(TextFilter.EQUATION_TAG, "");
            List<Word> words = seg.segmentSentence(text);
            List<TaggedWord> taggedWords = tagger.tagSentence(words);
            for (TaggedWord word : taggedWords) {
                if (!stopwordSet.contains(word.word()) && !word.tag().equals("PU")) {
                    out.append(word.word());
                    out.append(" ");
                }
            }
            out.newLine();
        }
        out.close();
        in.close();
    }

    public static String extractFeatur(Segmenter seg, Tagger tagger, String text) {
        text = text.replaceAll(TextFilter.PARENTHESIS, "")
                .replaceAll(TextFilter.SPACE, "")
                .replaceAll(TextFilter.EQUATION_TAG, "");
        List<Word> words = seg.segmentSentence(text);
        List<TaggedWord> taggedWords = tagger.tagSentence(words);
        StringBuilder sb = new StringBuilder();
        for (TaggedWord word : taggedWords) {
            if (!stopwordSet.contains(word.word()) && !word.tag().equals("PU")) {
                sb.append(word.word());
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    
    public static void main(String[] args) throws IOException {
        BufferedReader stopwordReader = new BufferedReader(new InputStreamReader(
                new FileInputStream("./exper/stopwords.txt"), "UTF-8"));
        while (stopwordReader.ready())
            stopwordSet.add(stopwordReader.readLine().trim());
        stopwordReader.close();
//        segmentLabeled();
//        System.out.println("starting unlabled");
//        segmentUnlabeled();
    }

}
