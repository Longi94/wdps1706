package openNLP;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

public class ONLP_POSTagger {

	private static POSTaggerME tagger = null;

	private static void createInstance() {
		InputStream modelIn = null;
		try {
		  //String modelPath = Paths.get(".").toAbsolutePath().normalize().toString().concat("\\src\\main\\java\\openNLP\\models\\en-pos-maxent.bin");
		  //String modelPath = ONLP_POSTagger.class.getResource("en-pos-maxent.bin").getPath();
      URL resourceURL = ONLP_POSTagger.class.getClassLoader().getResource("en-pos-maxent.bin");
      if(resourceURL != null){
        String resources = resourceURL.getFile();
        modelIn = new FileInputStream(resources);
        POSModel model = new POSModel(modelIn);
        tagger = new POSTaggerME(model);
      }

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
