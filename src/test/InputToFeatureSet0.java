package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.List;

public class InputToFeatureSet0 {

    /**
     * Read Features, ignore features not in the top range.
     * 
     * @param path
     *            feature file path.
     * @param percent
     *            how many percent of top features do you want.
     * @return
     * @throws IOException
     */
    public static List<String> readTopFeature(String path, double percent)
            throws IOException {
        File in = new File(path);
        List<String> featList = new LinkedList<String>();
        LineNumberReader lnr = new LineNumberReader(new FileReader(in));
        while (lnr.ready()) {
            String line = lnr.readLine();
            String feat = (line.split(" ")[0].split("/"))[0];
            featList.add(feat);
        }

        lnr.close();

        return featList.subList(0, (int) (featList.size() * percent));
    }

    private static boolean isDigit(String src) {
        for (int i = 0; i < src.length(); i++) {
            if (!Character.isDigit(src.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Translate one big csv file to folders. A folder is a classification
     * 
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void translateTrainOrTestStructure(String src, String dest,
            List<String> topFeature) throws IOException {
        File in = new File(src);
        LineNumberReader lnr = new LineNumberReader(new FileReader(in));

        while (lnr.ready()) {
            String line = lnr.readLine();
            String[] lineSplit = line.split("\t");
            String label = lineSplit[0];
            String context = lineSplit[1];

            File destDir = new File(dest, label);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File destFile = new File(destDir, "target");
            if (!destFile.exists())
                destFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(destFile,
                    true));

            String[] wordsWithPOS = context.split(" ");
            for (String wp : wordsWithPOS) {
                String word = wp.split("/")[0];
                if (topFeature.contains(word)) {
                    if (isDigit(word))
                        bw.append("NUM");
                    else
                        bw.append(word);
                    bw.append(" ");
                }
            }
            bw.append("\n");
            bw.close();
        }
        lnr.close();
    }

    public static void main(String[] args) throws IOException {
        String trainFilePath = "C:/cygwin/home/Mark/tokenization/2-8/2part2.csv";
        String testFilePath = "C:/cygwin/home/Mark/tokenization/2-8/8part1.csv";
        String featFilePath = "C:/cygwin/home/Mark/tokenization/2-8/8part1-feature-ig.csv";

        List<String> feature = InputToFeatureSet0.readTopFeature(featFilePath, 0.5);
        InputToFeatureSet0.translateTrainOrTestStructure(trainFilePath,
                "C:/Users/Mark/workspace/ClassifyAbstracts/data/abstract/train",
                feature);
        InputToFeatureSet0.translateTrainOrTestStructure(testFilePath,
                "C:/Users/Mark/workspace/ClassifyAbstracts/data/abstract/test",
                feature);
    }
}
