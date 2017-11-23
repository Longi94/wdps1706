package openNLP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

public class ONLP_POSTagger {
	
	private static POSTaggerME tagger = null;
	
	private static void createInstance() {
		InputStream modelIn = null;
		try {
			modelIn = new FileInputStream("models/en-pos-maxent.bin");
			POSModel model = new POSModel(modelIn);
			tagger = new POSTaggerME(model);
			
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
	
	public static String[] tag(String[] input) {
		if(tagger == null) {
			createInstance();
		}
		String[] result = tagger.tag(input);
		return result;
	}

}