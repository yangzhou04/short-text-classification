package exper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import preprocess.CsvToFolders;
//import preprocess.SegmentText;
import analysis.Sampler;
import classifier.EMNaiveBayesClassifier;

public class Exper2 {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        final String DIR = "exper3/";
//        SegmentText.readStopwords("exper2/stopwords.txt");
//        SegmentText.segmentLabeled("exper2/all.csv", "exper2/all.seged.csv");
        for (int i = 1; i < 10; i++) {
            double testPercent = (double)i/10;
            final String SUB_DIR = String.valueOf(i) + "_" + String.valueOf(10-i) + "/";
            File out = new File(DIR+SUB_DIR+"out.txt");
            if (!out.exists()) {
                out.getParentFile().mkdirs();
                out.createNewFile();
            }
            System.setOut(new PrintStream(out, "UTF-8"));
            final String TRAIN_FILE = DIR+SUB_DIR+"train.csv";
            final String TEST_FILE = DIR+SUB_DIR+"test.csv";
            
            Sampler.sample(DIR + "all.topic.csv", TRAIN_FILE, TEST_FILE, 1-testPercent);
            
            final String TRAIN_FOLDER = DIR+SUB_DIR+"train/";
            final String TEST_FOLDER = DIR+SUB_DIR+"test/";
            CsvToFolders trainTranslate = new CsvToFolders(TRAIN_FILE, 
                    TRAIN_FOLDER);
            trainTranslate.translateLabeled();
            CsvToFolders testTranslate = new CsvToFolders(TEST_FILE, 
                    TEST_FOLDER);
            testTranslate.translateLabeled();
            
            EMNaiveBayesClassifier emnbc = new EMNaiveBayesClassifier(DIR+SUB_DIR, // labeled corpus
                    DIR + "unlabeled", // unlabeled corpus
                    100, // maximum iteration
                    1, // minimum token count
                    1, // minimum improvement
                    100000, // cat prior
                    1, // token prior
                    20); // length norm
            
            System.out.println(emnbc.getConfusionMatrix());
            System.out.println("Macro precision = " + emnbc.macroAvgPrecision());
            System.out.println("Macro precision without NaN = " + emnbc.macroAvgPrecisionExceptNaN());
            System.out.println("Micro accuracy = " + emnbc.microAccuracy());
            System.out.println("Total accuracy = " + emnbc.totalAccuracy());
            System.out.println("Micro precision = " + emnbc.microPrecision());
            System.out.println("Micro recall = " + emnbc.microRecall());
            System.out.println("Micro fmeasure = " + emnbc.microFmeasure());
            emnbc.printCategoriesAprioriProbability();
        }
    }

}
