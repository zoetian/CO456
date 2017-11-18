
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

  public String toString() {
    switch (this) {
    case WHITE_WINS: return "White wins";
    case BLACK_WINS: return "Black wins";
    case TIE: return "Tie";
    case DRAW: return "Draw";
    default: return null;
    }
  }

  public int toInt(Colour playerColour) {
    switch (this) {
    case WHITE_WINS: return playerColour == Colour.WHITE ? 1 : 2;
    case BLACK_WINS: return playerColour == Colour.WHITE ? 2 : 1;
    case TIE: return 3;
    case DRAW: return 4;
    default: return 0;
    }
  }
}
