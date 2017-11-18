import java.util.*;

public class TeamSlowRealist extends Player {

  private HashMap<String, Node> results;
  int maxNumMoves;
  
  public TeamSlowRealist(int maxNumMoves_) {
    maxNumMoves = maxNumMoves_;
    LinkedList<BoardPosition> allInitialBoardPositions = getAllInitialBoardPositions();
    
    results = new HashMap<String, Node> ();

    for (BoardPosition boardPosition : allInitialBoardPositions) {
      computeBestMove(boardPosition);
    }
  }

  public void prepareForSeries() {
  }

  public void prepareForMatch() {
  }
  
  public void receiveMatchOutcome(int matchOutcome) {
  }

  public MoveDescription chooseMove() {
    BoardPosition boardPosition = toBoardPosition();
    return results.get(boardPosition.toString()).bestMove;
  }

  private Node computeBestMove(BoardPosition boardPosition) {
    String currentPositionString = boardPosition.toString();

    Node savedResult = results.get(currentPositionString);
    if (savedResult != null) {
      return savedResult;
    }

    int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;

    Node ret = null;

    double[] payoffsWhiteWins = new double[] {3.0, 0.0};
    double[] payoffsBlackWins = new double[] {0.0, 3.0};
    double[] payoffsTie = new double[] {2.0, 2.0};
    double[] payoffsDraw = new double[] {1.0, 1.0};

    // if both kings have been captured, the game has ended
    if (boardPosition.whiteKingPosition == 0 && boardPosition.blackKingPosition == 0) {
      ret = new Node(null, payoffsTie);
    } else if (boardPosition.whiteKingPosition == 0 && currentPlayerColour == BLACK) {
      // if white king has been captured and it's black player's turn, the game has ended
      // black won
      ret = new Node(null, payoffsBlackWins);
    } else if (boardPosition.blackKingPosition == 0 && currentPlayerColour == WHITE) {
      // if black king has been captured and it's white player's turn, the game has ended
      // white won
      ret = new Node(null, payoffsWhiteWins);
    } else if (currentPlayerColour == WHITE && boardPosition.whiteKingPosition == 0 && boardPosition.whiteRookPosition == 0) {
      // if it's white player's turn but he has no pieces left, the game has ended
      // black won
      ret = new Node(null, payoffsBlackWins);
    } else if (currentPlayerColour == BLACK && boardPosition.blackKingPosition == 0 && boardPosition.blackRookPosition == 0) {
      // if it's black player's turn but he has no pieces left, the game has ended
      // white won
      ret = new Node(null, payoffsWhiteWins);
    } else if (boardPosition.numMovesPlayed == maxNumMoves) {
      // if we have reached the maximum number of moves, the game is over
      if (boardPosition.whiteKingPosition != 0 && boardPosition.blackKingPosition != 0) {
        // draw
        ret = new Node(null, payoffsDraw);
      } else if (boardPosition.blackKingPosition == 0) {
        // white won
        ret = new Node(null, payoffsWhiteWins);
      } else {
        // !whiteKingRemains
        // black won
        ret = new Node(null, payoffsBlackWins);
      }
    } else {

      // Game has not ended
      // Explore all possible moves
      ArrayList<MoveDescription> allPossibleMoves = boardPosition.getAllPossibleMoves();
      for (MoveDescription moveDescription : allPossibleMoves) {
        BoardPosition newBoardPosition = boardPosition.doMove(moveDescription);
        Node node = computeBestMove(newBoardPosition);
        if (ret == null || node.getScore(currentPlayerColour) > ret.getScore(currentPlayerColour)) {
          ret = new Node(moveDescription, node.getScore(WHITE), node.getScore(BLACK));
        }
      }
    }
    results.put(currentPositionString, ret);
    return ret;
  }


  private class Node {
    public MoveDescription bestMove;
    public double scoreWhite, scoreBlack;

    public Node(MoveDescription bestMove_, double scoreWhite_, double scoreBlack_) {
      bestMove = bestMove_;
      scoreWhite = scoreWhite_;
      scoreBlack = scoreBlack_;
    }

    public Node(MoveDescription bestMove_, double [] score_) {
      this(bestMove_, score_[WHITE], score_[BLACK]);
    }

    public double getScore(int colour) {
      return (colour == WHITE) ? scoreWhite : scoreBlack;
    }
  }
}
