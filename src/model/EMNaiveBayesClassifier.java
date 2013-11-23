package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
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
        this(labeledDirectory, unlabeledDirectory, 100, 2, Integer.MAX_VALUE);
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
        train(0.5, 0.1, Double.NaN);
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
        System.setOut(new PrintStream("./debug.txt"));

//        String storedModelPath = "./emnbc.model";
//        EMNaiveBayesClassifier emnbc = EMNaiveBayesClassifier
//                .load(storedModelPath);
//        if (emnbc == null) {
        EMNaiveBayesClassifier emnbc = new EMNaiveBayesClassifier("./experiment/abstract/train",
                    "./experiment/abstract/unlabeledCorpus-min2.txt");
            emnbc.train();
//            EMNaiveBayesClassifier.save(emnbc, storedModelPath);
//        }

        // accuracy test
        File test = new File("./experiment/abstract/test");
        int c = 0, total = 0;
        for (File cat : test.listFiles()) {
            String trueLabel = cat.getName();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(cat, "labeledCorpus.txt")),
                    "UTF-8"));
            while (br.ready()) {
                String feat = br.readLine();
                String predictLabel = emnbc.bestCategory(feat);
                System.out.println("\n" + feat);
                System.out.println(total + ": P = " + predictLabel + ": "
                        + emnbc.bestCategoryProbability(feat) + " -- T = "
                        + trueLabel);
                emnbc.printAllCategoryProb(feat);
                if (trueLabel.equals(predictLabel))
                    c++;
                total++;
            }
            br.close();
        }

        System.out.println((double) c / total);

        System.out.println("\n\n\n\n=================================================");
        emnbc.printWordProb();
        // String feat =
        // "利用 永磁体 传动 装置 属于 机械 传动 技术 领域 利用 永磁体 磁力 机械 直线 运动 转换 转动 机械 装置 包括 动力 输出 部分 驱动 部分 其中 动力 输出 部分 具有 支承 轴承 主轴 主轴 表面 螺旋状 吸片 驱动 具有 导轮 外力 拖动 传动带 磁体 固定 传动带 表面 工作区 传动 主轴 永磁体 吸片 具有 间隙 相邻 永磁体 间距 等于 主轴 吸片 螺旋 螺距 使用 可以 直线 运动 转换 主轴 转动 装置 用于 机械 技术 领域 改变 机械 运动 状态 ";
        // System.out.println(emnbc.bestCategory(feat) + ": "
        // + emnbc.bestCategoryProbability(feat));
        // emnbc.printAllCategoryProb(feat);

        // emnbc.printCatProb();
        // emnbc.printWordProb(feat);

        // System.out.println("========================================");
        // String s = "电动机";
        // System.out.println(emnbc.bestCategory(s) + ": "
        // + emnbc.bestCategoryProbability(s));
    }

}
