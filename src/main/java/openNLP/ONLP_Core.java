package openNLP;

import opennlp.tools.util.Span;

public class ONLP_Core {

	public static ONLP_ResultWrapper process(String content) {
		ONLP_ResultWrapper result = new ONLP_ResultWrapper();

		// ---- START PROCESSING

		// Detect sentences
		String[] sentences = ONLP_SentenceDetector.detectSentences(content);

		for(String sentence : sentences) {
			// #0 Detect Language
			String lang = ONLP_LanguageDetector.getLanguage(sentence);
			// skip non-english sentences
			if(!lang.equals("eng")) continue;

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

			// keep only sentences with entities
			if(sentenceWrapper.entities.size() > 0) {
				result.sentenceWrappers.add(sentenceWrapper);
			}
		}

		//clear adaptive data of NER
		ONLP_NER.clearAdaptiveData();


		return result;

	}

}
