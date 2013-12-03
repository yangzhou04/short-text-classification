package preprocess;

import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Tagger {
    private MaxentTagger tagger;
    
    public Tagger() {
        tagger = new MaxentTagger("./data/models/chinese-distsim.tagger");
    }
    
    public List<TaggedWord> taggerSentence(List<Word> sent) {
        if (sent == null) return null;
        
        return tagger.apply(sent);
    }
    
    public List<List<TaggedWord>> taggerSentences(List<List<Word>> seperatedSents) {
        if (seperatedSents == null) return null;
        
        List<List<TaggedWord>> taggedSents = new LinkedList<List<TaggedWord>>();
        for (List<Word> ssent : seperatedSents) {
            taggedSents.add(taggerSentence(ssent));
        }
        return taggedSents;
    }
    
    public static void main(String[] args) {
        // tagger sentence test
        Segmenter seg = new Segmenter();
        String sent = "一种变速机构包含有一定速传动组件、一可变速传动组件、一太阳齿轮组以及一动力源；该太阳齿轮组件包含有:一第一太阳齿轮，是受定速传动组件的带动而以一定速度转动，以及一第二太阳齿轮，其转速可藉可变速传动主件的调整， 令一出力轴具有增速、减速、零速及反转的功能。";
        List<Word> words = seg.segmentSentence(sent);
        Tagger tagger = new Tagger();
        List<TaggedWord> taggedWords = tagger.taggerSentence(words);
        for (TaggedWord taggedWord : taggedWords) {
            System.out.print(taggedWord.word() + "/" + taggedWord.tag() + "  ");
        }
        System.out.println();
        
        // tagger sentences test
        List<String> sents = new LinkedList<String>();
        sents.add("一种变速机构包含有一定速传动组件、一可变速传动组件、一太阳齿轮组以及一动力源；该太阳齿轮组件包含有:一第一太阳齿轮，是受定速传动组件的带动而以一定速度转动，以及一第二太阳齿轮，其转速可藉可变速传动主件的调整， 令一出力轴具有增速、减速、零速及反转的功能。");
        sents.add("本发明涉及一种转矩传递装置，其具有至少两个借助周向作用的储力器相对减振装置产生相反作用力并通过支承可相互转动的飞轮质量，其中包括与内燃机从动轴连接的第一飞轮质量和通过摩擦离合器与传动机构的驱动轴连接的第二飞轮质量。");
        List<List<Word>> seperatedSents = seg.segmentSentences(sents);
        List<List<TaggedWord>> taggedSents = tagger.taggerSentences(seperatedSents);
        for (List<TaggedWord> taggedSent : taggedSents) {
            for (TaggedWord taggedWord : taggedSent) {
                System.out.print(taggedWord.word() + "/" + taggedWord.tag() + "  ");
            }
            System.out.println();
        }
    }
}
