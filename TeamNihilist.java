import java.util.*;

public class TeamNihilist extends Player {
  private Random rand;

  public TeamNihilist(int maxNumMoves) {
    rand = new Random(0);
  }
  
  public void prepareForSeries() {
  }
  
  public void prepareForMatch() {
  }
  
  public MoveDescription chooseMove() {
    ArrayList<MoveDescription> allPossibleMoves = getAllPossibleMoves();
    
    return allPossibleMoves.get(rand.nextInt(allPossibleMoves.size()));
  }
}
