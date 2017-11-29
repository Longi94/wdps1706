package openNLP;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ONLP_NER {

	private static NameFinderME[] nameFinders = null;

	private static final String[] MODELS = new String[]{
            "/en-ner-date.bin",
            "/en-ner-location.bin",
            "/en-ner-money.bin",
            "/en-ner-organization.bin",
            "/en-ner-percentage.bin",
            "/en-ner-person.bin",
            "/en-ner-time.bin"
    };

	private static void createInstance() {

		nameFinders = new NameFinderME[MODELS.length];
		InputStream modelIn;
		try {
            for (int i =0; i < MODELS.length; i++) {
                modelIn = ONLP_NER.class.getResourceAsStream(MODELS[i]);
                TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
                nameFinders[i] = new NameFinderME(model);
                modelIn.close();
            }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Span[] findEntities(String[] input) {
		if(nameFinders == null) {
			createInstance();
		}
		List<Span> entities = new ArrayList<Span>();
		for(NameFinderME nameFinder : nameFinders) {
			entities.addAll(Arrays.asList(nameFinder.find(input)));
			nameFinder.clearAdaptiveData();
		}

		Span[] result = new Span[entities.size()];
		return entities.toArray(result);
	}

	public static void clearAdaptiveData() {
		if(nameFinders.length > 0) {
			for (int i = 0; i < nameFinders.length; i++) {
				nameFinders[i].clearAdaptiveData();
			}
		}
	}


}
