package nl.vu.wdps1706.entityLinking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Daniel on 24/11/2017.
 */
public class HitsData {
  private Set<String> idsSet;
  private HashMap<String, Integer> scores;
  private HashMap<String, HashSet<String>> labels;

  public HitsData(Set<String> ids, HashMap<String, Integer> scoresMap, HashMap<String, HashSet<String>> labelsMapSet){
    this.idsSet = ids;
    this.scores = scoresMap;
    this.labels = labelsMapSet;
  }


  public Set<String> getIdsSet() {
    return idsSet;
  }

  public HashMap<String, Integer> getScores() {
    return scores;
  }


  public HashMap<String, HashSet<String>> getLabels() {
    return labels;
  }


}
