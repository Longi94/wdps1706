package openNLP;

import opennlp.tools.util.Span;

public class ONLP_Core {

	public static ONLP_ResultWrapper process(String content) {
		ONLP_ResultWrapper result = new ONLP_ResultWrapper();

		// ---- START PROCESSING

		// #0 Detect sentences
		String[] sentences = ONLP_SentenceDetector.detectSentences(content);

		for(String sentence : sentences) {
			ONLP_SentenceDetails sentenceWrapper = new ONLP_SentenceDetails();
			sentenceWrapper.sentence = sentence;

			// #1 Tokenize
			String[] tokens = ONLP_Tokenizer.tokenize(sentence);

			// #2 POS Tagger
			String[] posTags = ONLP_POSTagger.tag(tokens);

			// #3 Lemmatize
			String[] lemmas = ONLP_Lemmatizer.lemmatize(tokens, posTags);

			// #3.1 replace 'O' lemmas by original token
			String[] processedText = new String[tokens.length];
			for (int i = 0; i < lemmas.length; i++) {
				if (lemmas[i].equals("O")) processedText[i] = tokens[i];
				else processedText[i] = lemmas[i];
			}

			// #4 NER
			Span nameSpans[] = ONLP_NER.findEntities(processedText);

			for(Span nameSpan : nameSpans) {
				String entityName = "";
				String entityType = nameSpan.getType();
				double entityProb = nameSpan.getProb();

				int startPoint = nameSpan.getStart();
				int endPoint = nameSpan.getEnd();

				for(int x = startPoint; x < endPoint; x++) {
					entityName += tokens[x] + " ";
				}

				sentenceWrapper.entities.add(new ONLP_EntityDetails(entityName, entityType, entityProb));
			}

			result.sentenceWrappers.add(sentenceWrapper);
		}

		// ---- END PROCESSING

		/*
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
		*/


		return result;

	}

}
