
public enum MatchOutcome {
  WHITE_WINS, BLACK_WINS, TIE, DRAW;
  
  public double[] getPayoffs() {
    switch (this) {
      case WHITE_WINS: return Parameters.PAYOFFS_WHITE_WINS;
      case BLACK_WINS: return Parameters.PAYOFFS_BLACK_WINS;
      case TIE: return Parameters.PAYOFFS_TIE;
      case DRAW: return Parameters.PAYOFFS_DRAW;
      default: return null;
    }
  }
  
  /*public char toChar() {
    switch (this) {
      case WHITE_WINS: return 'W';
      case BLACK_WINS: return 'B';
      case TIE: return 'T';
      case DRAW: return 'D';
      default: return ' ';
    }
  }*/
}
