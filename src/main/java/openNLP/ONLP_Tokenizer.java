package openNLP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class ONLP_Tokenizer {
	
	// private static TokenizerME tokenizer = null;
	private static Tokenizer tokenizer = null;
	
	private static void createInstance() {
		/*InputStream modelIn = null;
		try {
		modelIn = new FileInputStream("models/en-token.bin");
		TokenizerModel model = new TokenizerModel(modelIn);
		tokenizer = new TokenizerME(model);
		
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
		}*/
		
		//use SimpleTokenizer - better results
		tokenizer = SimpleTokenizer.INSTANCE;
	}
	
	public static String[] tokenize(String input) {
		if(tokenizer == null) {
			createInstance();
		}
		String[] result = tokenizer.tokenize(input);
		return result;
	}
}