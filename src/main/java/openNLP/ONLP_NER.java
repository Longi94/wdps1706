package openNLP;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

public class ONLP_NER {
	
	private static String[] nerModels = {
		"models/en-ner-date.bin",
		"models/en-ner-location.bin",
		"models/en-ner-money.bin",
		"models/en-ner-organization.bin",
		"models/en-ner-percentage.bin",
		"models/en-ner-person.bin",
		"models/en-ner-time.bin"
	};
	
	private static List<NameFinderME> nameFinders = null;
	
	private static void createInstance() {
		nameFinders = new ArrayList<NameFinderME>();
		InputStream modelIn = null;
		try {
			for(int i = 0; i < nerModels.length; i++) {
				modelIn = new FileInputStream(nerModels[i]);
				TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
				nameFinders.add(new NameFinderME(model));
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
	
	
}
