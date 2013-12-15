package classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.CollectionUtils;
import com.aliasi.util.Factory;

import corpus.LabeledCorpus;
import corpus.UnlabeledCorpus;

public class EMNaiveBayesClassifier implements Serializable {

    private static final long serialVersionUID = -8117025601326861957L;
    private int mMaxIter;
    private int mMinTokenCount;
    private int mMinImprovement;
    private final String DELIM = "\\P{Z}+";

    private TradNaiveBayesClassifier mClassifier;
    
    private transient LabeledCorpus mLabeledCorpus;
    private transient UnlabeledCorpus nUnlabeledCorpus;
    private transient ConfusionMatrix mConfusionMatrix;

    /**
     * Create and Train a 
     * @param labeledDirectory
     * @param unlabeledDirectory
     * @param maxIter
     * @param minTokenCount
     * @param minImprovement
     * @param catPrior
     * @param tokPrior
     * @param lengthNorm
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public EMNaiveBayesClassifier(String labeledDirectory,
            String unlabeledDirectory, int maxIter, int minTokenCount,
            int minImprovement, int catPrior, double tokPrior,
            double lengthNorm) throws IOException, ClassNotFoundException {
        this.mMaxIter = maxIter;
        this.mMinTokenCount = minTokenCount;
        this.mMinImprovement = minImprovement;
        this.mLabeledCorpus = new LabeledCorpus(new File(labeledDirectory, "train"),
                new File(labeledDirectory, "test"));
        this.nUnlabeledCorpus = new UnlabeledCorpus(unlabeledDirectory);
        train(catPrior, tokPrior, lengthNorm);
    }

    private void train(final double catPrior, final double tokPrior,
            final double lengthNorm) throws IOException, ClassNotFoundException {
        // train initial classifier with labeled corpus
        final TokenizerFactory tf = new RegExTokenizerFactory(DELIM);
        final Set<String> catSet = CollectionUtils.asSet(mLabeledCorpus.getCatogories());
        TradNaiveBayesClassifier initClassifier = new TradNaiveBayesClassifier(
                catSet, tf, catPrior, tokPrior, lengthNorm);
        mLabeledCorpus.visitTrain(initClassifier);

        // create a new classifier for EM train per iteration
        Factory<TradNaiveBayesClassifier> nbcFactory = new Factory<TradNaiveBayesClassifier>() {
            @Override
            public TradNaiveBayesClassifier create() {
                TradNaiveBayesClassifier classifier = new TradNaiveBayesClassifier(
                        catSet, tf, catPrior, tokPrior, lengthNorm);
                return classifier;
            }
        };

        mClassifier = TradNaiveBayesClassifier.emTrain(initClassifier,
                nbcFactory, mLabeledCorpus, nUnlabeledCorpus, mMinTokenCount,
                mMaxIter, mMinImprovement, null);
        @SuppressWarnings("unchecked")
        JointClassifier<CharSequence> compiledClassifier = 
            (JointClassifier<CharSequence>) AbstractExternalizable.compile(mClassifier);
        JointClassifierEvaluator<CharSequence> evaluator = new JointClassifierEvaluator<CharSequence>(
              compiledClassifier, mLabeledCorpus.getCatogories(), false);
        mLabeledCorpus.visitTest(evaluator);
        mConfusionMatrix = evaluator.confusionMatrix();
    }

    public double bestCategoryProbability(CharSequence cs) {
        JointClassification jc = mClassifier.classify(cs);
        return jc.conditionalProbability(jc.bestCategory());
    }

    public String bestCategory(CharSequence cs) {
        return mClassifier.classify(cs).bestCategory();
    }

    public void printCategoriesProbability(CharSequence cs) {
        JointClassification jc = mClassifier.classify(cs);
        for (int i = 0; i < mClassifier.categorySet().size(); i++) {
            System.out.println(jc.category(i) + ": "
                    + jc.conditionalProbability(i));
        }
    }

    public void printCategoriesAprioriProbability() throws IOException {
        String[] cats = mLabeledCorpus.getCatogories();
        for (int i = 0; i < cats.length; i++)
            System.out.println(cats[i] + ": " + mClassifier.probCat(cats[i]));
    }

    public void printWordsProbability() throws IOException {
        String[] cats = mLabeledCorpus.getCatogories();
        Set<String> knownTokenSets = mClassifier.knownTokenSet();
        for (Iterator<String> iter = knownTokenSets.iterator();iter.hasNext();) {
            String token = iter.next();
            System.out.println(token + "=>");
            for (int i = 0; i < cats.length; i++) {
                System.out.println(cats[i] + ": "
                        + mClassifier.probToken(token, cats[i]) + "  ");
            }
        }
    }

    public ConfusionMatrix getConfusionMatrix() {
        return mConfusionMatrix;
    }
    
    public double macroAvgPrecision() {
        return mConfusionMatrix.macroAvgPrecision();
    }
    
    public double totalAccuracy() {
        return mConfusionMatrix.totalAccuracy();
    }
    
    public double microAccuracy() {
        return mConfusionMatrix.microAverage().accuracy();
    }
    
    public double precision() {
        return mConfusionMatrix.microAverage().precision();
    }
    
    public double recall() {
        return mConfusionMatrix.microAverage().recall();
    }
    
    public double fmeasure() {
        return mConfusionMatrix.microAverage().fMeasure();
    }
    
    public static void save(EMNaiveBayesClassifier emnbc, String path)
            throws IOException {
        File f = new File(path);
        if (f.exists())
            f.delete();
        f.getParentFile().mkdirs();
        f.createNewFile();
        
        FileOutputStream fileOut = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(emnbc);
        out.close();
    }

    public static EMNaiveBayesClassifier load(String path) {
        try {
            FileInputStream fin = new FileInputStream(path);
            ObjectInputStream oin = new ObjectInputStream(fin);
            Object obj = oin.readObject();
            oin.close();
            fin.close();
            if (obj instanceof EMNaiveBayesClassifier) {
                System.out.println("Using model from: " + path);
                EMNaiveBayesClassifier emnbc = (EMNaiveBayesClassifier) obj;
                return emnbc;
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
        String storedModelPath = "";
        EMNaiveBayesClassifier emnbc = EMNaiveBayesClassifier.load(storedModelPath);
        if (emnbc == null) {
            emnbc = new EMNaiveBayesClassifier("exper/abstracts/exper6/", // labeled corpus
                    "exper/abstracts/unlabeled", // unlabeled corpus 
                    10, // maximum iteration
                    2, // minimum token count 
                    0, // minimum improvment
                    100, // cat prior
                    2, // token prior
                    20); // length norm
//            EMNaiveBayesClassifier.save(emnbc, storedModelPath);
        }

//        System.out.println("Total accuracy = " + emnbc.totalAccuracy());
//        System.out.println("Macro accuracy = " + emnbc.macroAvgPrecision());
        System.out.println("Micro accuracy = " + emnbc.microAccuracy());
        System.out.println("Precision = " + emnbc.precision());
        System.out.println("Recall = " + emnbc.recall());
        System.out.println("Fmeasure = " + emnbc.fmeasure());
    }

}
