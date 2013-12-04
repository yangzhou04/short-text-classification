package classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import com.aliasi.classify.JointClassification;
import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.CollectionUtils;

import corpus.TrainLabeledCorpus;

public class NaiveBayesClassifier implements Serializable {
    private static final long serialVersionUID = 7388561872019002215L;
    private final String DELIM = "\\P{Z}+";
    private TradNaiveBayesClassifier classifier;

    private transient TrainLabeledCorpus labeledCorpus;

    public NaiveBayesClassifier(String labeledDirectory) throws IOException {
        this.labeledCorpus = new TrainLabeledCorpus(labeledDirectory);
    }
    
    public void train() throws IOException {
        train(0.5, 0.1, Double.NaN);
    }
    
    public void train(final double catPrior, final double tokPrior,
            final double lengthNorm) throws IOException {
        // train initial classifier
        final String[] CATEGORIES = labeledCorpus.getCatogories();
        TokenizerFactory tf = new RegExTokenizerFactory(DELIM);
        Set<String> catSet = CollectionUtils.asSet(CATEGORIES);
        classifier = new TradNaiveBayesClassifier(
                catSet, tf, catPrior, tokPrior, lengthNorm);
        labeledCorpus.visitTrain(classifier);
    }

    public double bestCategoryProbability(CharSequence cs) {
        JointClassification jc = classifier.classify(cs);
        return jc.conditionalProbability(jc.bestCategory());
    }

    public String bestCategory(CharSequence cs) {
        return classifier.classify(cs).bestCategory();
    }

    public void printAllCategoryProb(CharSequence cs) {
        JointClassification jc = classifier.classify(cs);
        for (int i = 0; i < classifier.categorySet().size(); i++) {
            System.out.println(jc.category(i) + ": "
                    + jc.conditionalProbability(i));
        }
    }

    public JointClassification classify(CharSequence cs) {
        return classifier.classify(cs);
    }

    public void printCatProb() throws IOException {
        String[] cats = labeledCorpus.getCatogories();
        for (int i = 0; i < cats.length; i++)
            System.out.println(cats[i] + ": " + classifier.probCat(cats[i]));
    }

    public void printWordProb() throws IOException {
        String[] cats = labeledCorpus.getCatogories();

        Set<String> knownTokenSets = classifier.knownTokenSet();
        Iterator<String> iter = knownTokenSets.iterator();
        while (iter.hasNext()) {
            String token = iter.next();
            System.out.println(token + "=>");
            for (int i = 0; i < cats.length; i++) {
                System.out.println(cats[i] + ": "
                        + classifier.probToken(token, cats[i]) + "  ");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NaiveBayesClassifier nbc = new NaiveBayesClassifier("./experiment/abstract/train");
        nbc.train();
        
        File test = new File("./experiment/abstract/test");
        int c = 0, total = 0;
        for (File cat : test.listFiles()) {
            String trueLabel = cat.getName();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(cat, "labeledCorpus.txt")),
                    "UTF-8"));
            while (br.ready()) {
                String feat = br.readLine();
                String predictLabel = nbc.bestCategory(feat);
//                System.out.println("\n" + feat);
//                System.out.println(total + ": P = " + predictLabel + ": "
//                        + nbc.bestCategoryProbability(feat) + " -- T = "
//                        + trueLabel);
//                nbc.printAllCategoryProb(feat);
                if (trueLabel.equals(predictLabel))
                    c++;
                total++;
            }
            br.close();
        }

        System.out.println((double) c / total);
    }
}
