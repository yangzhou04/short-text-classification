package classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.aliasi.classify.Classified;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.CollectionUtils;
import com.aliasi.util.Factory;

import corpus.TrainLabeledCorpus;
import corpus.UnlabeledCorpus;

public class EMNaiveBayesClassifier implements Serializable {

    private static final long serialVersionUID = 7388561872019002215L;
    private int maxIter;
    private int minTokenCount;
    private int minImprovement;
    private final String DELIM = "\\P{Z}+";

    private transient TrainLabeledCorpus labeledCorpus;
    private transient UnlabeledCorpus unlabeledCorpus;

    private TradNaiveBayesClassifier emClassifier;

    public EMNaiveBayesClassifier(String labeledDirectory,
            String unlabeledDirectory) throws IOException {
        this(labeledDirectory, unlabeledDirectory, 100, // maximum iteration
                2,// minimum token count 
                0); // minimum improvment
    }

    public EMNaiveBayesClassifier(String labeledDirectory,
            String unlabeledDirectory, int maxIter, int minTokenCount,
            int minImprovement) throws IOException {
        this.maxIter = maxIter;
        this.minTokenCount = minTokenCount;
        this.minImprovement = minImprovement;
        this.labeledCorpus = new TrainLabeledCorpus(labeledDirectory);
        this.unlabeledCorpus = new UnlabeledCorpus(unlabeledDirectory);
    }

    public void train() throws IOException {
        train(100, 0.1, 5);
    }

    public void train(final double catPrior, final double tokPrior,
            final double lengthNorm) throws IOException {
        // train initial classifier
        final String[] CATEGORIES = labeledCorpus.getCatogories();
        TokenizerFactory tf = new RegExTokenizerFactory(DELIM);
        Set<String> catSet = CollectionUtils.asSet(CATEGORIES);
        TradNaiveBayesClassifier initClassifier = new TradNaiveBayesClassifier(
                catSet, tf, catPrior, tokPrior, lengthNorm);
        labeledCorpus.visitTrain(initClassifier);

        // EM naive bayes
        Factory<TradNaiveBayesClassifier> nbcFactory = new Factory<TradNaiveBayesClassifier>() {
            @Override
            public TradNaiveBayesClassifier create() {
                TokenizerFactory tf = new RegExTokenizerFactory(DELIM);
                Set<String> catSet = CollectionUtils.asSet(CATEGORIES);
                TradNaiveBayesClassifier classifier = new TradNaiveBayesClassifier(
                        catSet, tf, catPrior, tokPrior, lengthNorm);
                return classifier;
            }
        };

        // using em to train naive bayes classifier
        emClassifier = TradNaiveBayesClassifier.emTrain(initClassifier,
                nbcFactory, labeledCorpus, unlabeledCorpus, minTokenCount,
                maxIter, minImprovement, null);
    }

    public double bestCategoryProbability(CharSequence cs) {
        JointClassification jc = emClassifier.classify(cs);
        return jc.conditionalProbability(jc.bestCategory());
    }

    public String bestCategory(CharSequence cs) {
        return emClassifier.classify(cs).bestCategory();
    }

    public void printAllCategoryProb(CharSequence cs) {
        JointClassification jc = emClassifier.classify(cs);
        for (int i = 0; i < emClassifier.categorySet().size(); i++) {
            System.out.println(jc.category(i) + ": "
                    + jc.conditionalProbability(i));
        }
    }

    public JointClassification classify(CharSequence cs) {
        return emClassifier.classify(cs);
    }

    public void printCatProb() throws IOException {
        String[] cats = labeledCorpus.getCatogories();
        for (int i = 0; i < cats.length; i++)
            System.out.println(cats[i] + ": " + emClassifier.probCat(cats[i]));
    }

    public void printWordProb() throws IOException {
        String[] cats = labeledCorpus.getCatogories();

        Set<String> knownTokenSets = emClassifier.knownTokenSet();
        Iterator<String> iter = knownTokenSets.iterator();
        while (iter.hasNext()) {
            String token = iter.next();
            System.out.println(token + "=>");
            for (int i = 0; i < cats.length; i++) {
                System.out.println(cats[i] + ": "
                        + emClassifier.probToken(token, cats[i]) + "  ");
            }
        }
    }

    public double evaluate(String testPath) throws IOException,
            ClassNotFoundException {
        Corpus<ObjectHandler<Classified<CharSequence>>> corpus = new TrainLabeledCorpus(
                new File(testPath));
        String[] categories = emClassifier.categorySet().toArray(new String[0]);
        Arrays.sort(categories);
        @SuppressWarnings("unchecked")
        JointClassifier<CharSequence> compiledClassifier = (JointClassifier<CharSequence>) AbstractExternalizable
                .compile(emClassifier);
        boolean storeInputs = false;
        JointClassifierEvaluator<CharSequence> evaluator = new JointClassifierEvaluator<CharSequence>(
                compiledClassifier, categories, storeInputs);
        corpus.visitTest(evaluator);
        return evaluator.confusionMatrix().totalAccuracy();
    }

    public static void save(EMNaiveBayesClassifier emnbc, String path)
            throws IOException {
        FileOutputStream fileOut = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(emnbc);
        out.close();
    }

    public static EMNaiveBayesClassifier load(String path) {
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Object obj = in.readObject();
            in.close();
            fileIn.close();
            if (obj instanceof EMNaiveBayesClassifier) {
                System.out.println("Using model from: " + path);
                return (EMNaiveBayesClassifier) obj;
            } else
                return null;
        } catch (FileNotFoundException e1) {
            System.err.println("Warning: File not found, retrain model...");
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found, retrain model...");
            return null;
        } catch (IOException e) {
            System.err.println("Can't read object. retrain model...");
            return null;
        }
    }

    public static void main(String[] args) throws IOException,
            ClassNotFoundException {
        // System.setOut(new PrintStream("./exper/em.txt"));
        String storedModelPath = "";
        EMNaiveBayesClassifier emnbc = EMNaiveBayesClassifier
                .load(storedModelPath);
        if (emnbc == null) {
            emnbc = new EMNaiveBayesClassifier("exper/abstracts/exper3/train",
                    "exper/empty.csv");
            emnbc.train();
            // EMNaiveBayesClassifier.save(emnbc, storedModelPath);
        }

        // accuracy test
        Map<String, Integer> counter = new HashMap<String, Integer>();
        File test = new File("exper/abstracts/exper3/test");
        int c = 0, total = 0, ignore = 0;
        for (File cat : test.listFiles()) {
            String trueLabel = cat.getName();

            for (File testFile : cat.listFiles()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(testFile), "UTF-8"));
                while (br.ready()) {
                    String feat = br.readLine();
                    double p = emnbc.bestCategoryProbability(feat);
                    if (p > 0.75) {
                        String predictLabel = emnbc.bestCategory(feat);
                        if (counter.containsKey(predictLabel))
                            counter.put(predictLabel,
                                    counter.get(predictLabel) + 1);
                        else
                            counter.put(predictLabel, 1);
//                        System.out.println("\n" + feat);
//                        System.out.println(total + ": P = " + predictLabel
//                                + ": " + emnbc.bestCategoryProbability(feat)
//                                + " -- T = " + trueLabel);
//                        emnbc.printAllCategoryProb(feat);
                        if (trueLabel.equals(predictLabel))
                            c++;
                        total++;
                    } else {
                        ignore++;
                    }
                }
                br.close();
            }
        }

        System.out.println("Class priority = ");
        emnbc.printCatProb();
        System.out.println(counter);
        System.out.println("Ignored: " + ignore);
        System.out.println("Totoal: = " + total);
        System.out.println("================");
        System.out.print("Average accrucy = ");
        System.out.println((double) c / total);
    }

}
