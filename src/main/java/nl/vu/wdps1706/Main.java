package nl.vu.wdps1706;


import openNLP.ONLP_Core;
import openNLP.ONLP_EntityDetails;
import openNLP.ONLP_ResultWrapper;
import openNLP.ONLP_SentenceDetails;

/**
 * @author lngtr
 * @since 2017-11-14
 */

public class Main {


    public static void main(String[] args) {

        String contents = "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29. Mr. Vinken is\n" +
                "chairman of Elsevier N.V., the Dutch publishing group. Rudolph Agnew, 55 years\n" +
                "old and former chairman of Consolidated Gold Fields PLC, was named a director of this\n" +
                "British industrial conglomerate. " +
                "Tout au long de la saison, FF va à la rencontre des techniciens de la Ligue 1 pour les faire parler de leur métier. " +
                "Cette semaine, c'est au tour de Claudio Ranieri dont la passion pour son métier demeure intacte. A soixante-six ans, " +
                "le technicien italien dirige Nantes, le quinzième club de sa carrière de coach. Extraits de l'interview à retrouver dans FF. " +
                "Donald Trump has retweeted three inflammatory videos from a British far-right group.\n" +
                "Asdsdf sdf asdf dfsd fdf sd fs. " +
                "±± ♠ Ω±± ♠ Ω±± ♠ Ω±± ♠ Ω±± ♠ Ω±. " +
                "The first tweet from Jayda Fransen, the deputy leader of Britain First, claims to show a Muslim migrant attacking a man on crutches.\n" +
                "This was followed by two more videos of people Ms Fransen claims to be Muslim.\n" +
                "Responding to Mr Trump's posts, UK Prime Minister Theresa May's official spokesman said it was \"wrong for the president to have done this\".";

        ONLP_ResultWrapper result = ONLP_Core.process(contents);

        for(ONLP_SentenceDetails sw : result.sentenceWrappers) {
            System.out.println("==============");
            System.out.println(sw.sentence);
            System.out.println("--------");
            for(ONLP_EntityDetails entity: sw.entities) {
                System.out.println("entity: " + entity.name + ", type: " + entity.type + ", prob: " + entity.prob);
            }
            System.out.println("==============");
            System.out.println();
        }



    }
}
