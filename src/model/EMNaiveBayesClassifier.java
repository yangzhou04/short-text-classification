package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
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

import corpus.LabeledCorpus;
import corpus.UnlabeledCorpus;

public class EMNaiveBayesClassifier implements Serializable {

    private static final long serialVersionUID = 7388561872019002215L;
    private int maxIter;
    private int minTokenCount;
    private int minImprovement;
    private final String DELIM = "\\P{Z}+";

    private transient LabeledCorpus labeledCorpus;
    private transient UnlabeledCorpus unlabeledCorpus;

    private TradNaiveBayesClassifier emClassifier;

    public EMNaiveBayesClassifier(String labeledDirectory,
            String unlabeledDirectory) throws IOException {
        this(labeledDirectory, unlabeledDirectory, 100, 2, 1);
    }

    public EMNaiveBayesClassifier(String labeledDirectory,
            String unlabeledDirectory, int maxIter, int minTokenCount,
            int minImprovement) throws IOException {
        this.maxIter = maxIter;
        this.minTokenCount = minTokenCount;
        this.minImprovement = minImprovement;
        this.labeledCorpus = new LabeledCorpus(labeledDirectory);
        this.unlabeledCorpus = new UnlabeledCorpus(unlabeledDirectory);
    }

    public void train() throws IOException {
        // train initial classifier
        final String[] CATEGORIES = labeledCorpus.getCatogories();
        TokenizerFactory tf = new RegExTokenizerFactory(DELIM);
        double catPrior = 1.0;
        double tokPrior = 1;
        double lengthNorm = Double.NaN;
        Set<String> catSet = CollectionUtils.asSet(CATEGORIES);
        TradNaiveBayesClassifier initClassifier = new TradNaiveBayesClassifier(
                catSet, tf, catPrior, tokPrior, lengthNorm);
        labeledCorpus.visitTrain(initClassifier);

        // EM naive bayes
        Factory<TradNaiveBayesClassifier> nbcFactory = new Factory<TradNaiveBayesClassifier>() {
            @Override
            public TradNaiveBayesClassifier create() {
                TokenizerFactory tf = new RegExTokenizerFactory(DELIM);
                double catPrior = 1.0;
                double tokPrior = 1;
                double lengthNorm = Double.NaN;
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

    public JointClassification classify(CharSequence cs) {
        return emClassifier.classify(cs);
    }

    public double evaluate(String testPath) throws IOException,
            ClassNotFoundException {
        Corpus<ObjectHandler<Classified<CharSequence>>> corpus = new LabeledCorpus(
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
        EMNaiveBayesClassifier emnbc;
        String storedModelPath = "./emnbc.model";
        emnbc = EMNaiveBayesClassifier.load(storedModelPath);
        if (emnbc == null) {
            emnbc = new EMNaiveBayesClassifier("./data/abstract/train",
                    "./data/abstract/unlabeledCorpus.txt");
            emnbc.train();
            EMNaiveBayesClassifier.save(emnbc, storedModelPath);
        }
        String target = "";
        System.out.println(emnbc.bestCategory(target));
        System.out.println(emnbc.bestCategoryProbability(target));

        // System.out.println(emnbc.evaluate("./data/corpus/test"));
    }

}
