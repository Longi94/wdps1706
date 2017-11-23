package openNLP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;

public class ONLP_Lemmatizer {
	
	private static DictionaryLemmatizer lemmatizer = null; 
	
	private static void createInstance() {
		InputStream modelIn = null;
		try {
			modelIn = new FileInputStream("models/en-lemmatizer.bin");
			lemmatizer = new DictionaryLemmatizer(modelIn);
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
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
