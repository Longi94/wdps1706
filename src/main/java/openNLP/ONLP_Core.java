package openNLP;

import opennlp.tools.util.Span;
import openNLP.ONLP_Lemmatizer;
import openNLP.ONLP_NER;
import openNLP.ONLP_POSTagger;
import openNLP.ONLP_Tokenizer;

public class ONLP_Core {
	
	public static void process(String content) {
		
		// ---- START PROCESSING
		
		// #1 Tokenize
		String[] tokens = ONLP_Tokenizer.tokenize(content);
		
		// #2 POS Tagger
		String[] posTags = ONLP_POSTagger.tag(tokens);
		
		// #3 Lemmatize
		String[] lemmas = ONLP_Lemmatizer.lemmatize(tokens, posTags);
		
		// #3.1 replace 'O' lemmas by original token
		String[] processedText = new String[tokens.length];
		for(int i = 0; i < lemmas.length; i++) {
			if(lemmas[i] == "O") processedText[i] = tokens[i];
			else processedText[i] = lemmas[i];
		}
		
		// #4 NER
		Span nameSpans[] = ONLP_NER.findEntities(processedText);
		
		// ---- END PROCESSING
		
		System.out.println("Tokens\t\t\tPOS\t\t\tLemma\\t\t\tResult\n-------------------------------");
		for(int i=0;i<tokens.length;i++){
			System.out.println(tokens[i] + "\t\t\t" + posTags[i] + "\t\t\t" + lemmas[i] + "\t\t\t" + processedText[i]);
		}
		
		System.out.println("====================================================");
		
		for(int i=0;i<nameSpans.length;i++) {
			System.out.println();
			System.out.println("#### " + i + " ###");
			System.out.print("[" + nameSpans[i].getType() + "]: [" + nameSpans[i].getStart() + "," + nameSpans[i].getEnd() + "] => ");
			int startPoint = nameSpans[i].getStart();
			int endPoint = nameSpans[i].getEnd();
			
			for(int x = startPoint; x < endPoint; x++) {
				System.out.print(tokens[x] + " ");
			}
			System.out.println("");
			System.out.println("------ END -----");
		}
		
	}
	
}
