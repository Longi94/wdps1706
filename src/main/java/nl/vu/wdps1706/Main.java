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
                "British industrial conglomerate.";

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
