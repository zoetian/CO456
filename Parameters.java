
public class Parameters {
  public static final int NUM_COLUMNS = 4;
  public static final int NUM_ROWS = 4;
  public static final int MAX_NUM_MOVES = 30;
  public static final int NUM_PIECES = 4;
  
  public static final double[] PAYOFFS_WHITE_WINS = {3, 0};
  public static final double[] PAYOFFS_BLACK_WINS = {0, 3};
  public static final double[] PAYOFFS_TIE = {2, 2};
  public static final double[] PAYOFFS_DRAW = {0, 0};
  
  public static int MIN_NUM_DIFFERENT_BOARDS = 18;
  
  //public static final boolean PRINT_LOG = true;
  public static final boolean PRINT_LOG = false;
  
  public static final double TIME_LIMIT_CONSTRUCTOR = 120;
  public static final double TIME_LIMIT_PREPARE_FOR_SERIES = 1;
  public static final double TIME_LIMIT_PREPARE_FOR_MATCH = 1;
  public static final double TIME_LIMIT_CHOOSE_MOVE = 1;
}
