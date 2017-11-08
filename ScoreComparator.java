import java.util.*;

public class ScoreComparator implements Comparator<Integer> {
  double[] scores;
  
  public ScoreComparator(double[] scores_) {
    scores = scores_;
  }

  @Override
  public int compare(Integer o1, Integer o2) {
    if (scores[o1] > scores[o2]) {
      return -1;
    } else if (scores[o1] < scores[o2]) {
      return 1;
    } else {
      return 0;
    }
  }
}
