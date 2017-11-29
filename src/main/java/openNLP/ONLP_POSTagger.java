package openNLP;


import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.IOException;
import java.io.InputStream;

public class ONLP_POSTagger {

	private static POSTaggerME tagger = null;

	private static void createInstance() {
		InputStream modelIn = null;
		try {
			modelIn = ONLP_POSTagger.class.getResourceAsStream("/en-pos-maxent.bin");
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
