package preprocess;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;

/**
 * 
 * @author Yang
 * 
 */
public class Segmenter {

    private CRFClassifier<CoreLabel> crfSeg;

    public Segmenter() {
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", "data");
        // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
        // props.setProperty("normTableEncoding", "UTF-8");
        // below is needed because CTBSegDocumentIteratorFactory accesses it
        props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");
        crfSeg = new CRFClassifier<CoreLabel>(props);
        crfSeg.loadClassifierNoExceptions("data/ctb.gz", props);
    }

    public List<Word> segmentSentence(String sent) {
        if (sent == null) return null;
        
        List<Word> words = new LinkedList<Word>();
        for (String w : crfSeg.segmentString(sent)) {
            words.add(new Word(w));
        }
        return words;
    }

    public List<List<Word>> segmentSentences(List<String> sents) {
        if (sents == null) return null;

        List<List<Word>> results = new LinkedList<List<Word>>();
        for (String sent : sents) {
            results.add(segmentSentence(sent));
        }
        return results;
    }
    
    public static void main(String[] args) {
        // segment sentence test
        Segmenter seg = new Segmenter();
        String sent = "一种变速机构包含有一定速传动组件、一可变速传动组件、一太阳齿轮组以及一动力源；该太阳齿轮组件包含有:一第一太阳齿轮，是受定速传动组件的带动而以一定速度转动，以及一第二太阳齿轮，其转速可藉可变速传动主件的调整， 令一出力轴具有增速、减速、零速及反转的功能。";
        List<Word> words = seg.segmentSentence(sent);
        for (Word word : words) {
            System.out.print(word.word() + "  ");
        }
        System.out.println();
        
        // segment sentences test
        List<String> sents = new LinkedList<String>();
        sents.add("一种变速机构包含有一定速传动组件、一可变速传动组件、一太阳齿轮组以及一动力源；该太阳齿轮组件包含有:一第一太阳齿轮，是受定速传动组件的带动而以一定速度转动，以及一第二太阳齿轮，其转速可藉可变速传动主件的调整， 令一出力轴具有增速、减速、零速及反转的功能。");
        sents.add("本发明涉及一种转矩传递装置，其具有至少两个借助周向作用的储力器相对减振装置产生相反作用力并通过支承可相互转动的飞轮质量，其中包括与内燃机从动轴连接的第一飞轮质量和通过摩擦离合器与传动机构的驱动轴连接的第二飞轮质量。");
        List<List<Word>> wordsList = seg.segmentSentences(sents);
        for (List<Word> wList : wordsList) {
            for (Word word : wList) {
                System.out.print(word.word() + "  ");
            }
            System.out.println();
        }
    }
}
