package openNLP;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;

import java.io.IOException;
import java.io.InputStream;

public class ONLP_Lemmatizer {

	private static DictionaryLemmatizer lemmatizer = null;

	private static void createInstance() {
		InputStream modelIn = null;
		try {
			modelIn = ONLP_Lemmatizer.class.getResourceAsStream("/en-lemmatizer.bin");
			lemmatizer = new DictionaryLemmatizer(modelIn);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				  e.printStackTrace();
				}
			}
		}

	}

	public static String[] lemmatize(String[] tokens, String[] posTags) {
		if(lemmatizer == null) {
			createInstance();
		}
		String[] result = lemmatizer.lemmatize(tokens, posTags);
		return result;
	}

}
