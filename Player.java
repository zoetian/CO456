import java.util.ArrayList;

abstract class Player {
  protected int myColour;
  protected int numMovesPlayed; 
  protected boolean myKingIsAlive;
  protected int myKingColumn, myKingRow;
  protected boolean myRookIsAlive;
  protected int myRookColumn, myRookRow;
  protected boolean theirKingIsAlive;
  protected int theirKingColumn, theirKingRow;
  protected boolean theirRookIsAlive;
  protected int theirRookColumn, theirRookRow;
  
  protected final int WHITE = 0, BLACK = 1;
  
  public Player() {
  }
  
  public void prepareForSeries() {
    throw new RuntimeException("You need to override the method prepareForSeries.");
  }
  
  public void prepareForMatch() {
    throw new RuntimeException("You need to override the method prepareForMatch.");
  }
  
  public MoveDescription chooseMove() {
    throw new RuntimeException("You need to override the method ChooseMove.");
  }
  
  public final void update(Board board, Colour myColour_, int numMovesPlayed_) {
    myColour = myColour_.toInt();
    numMovesPlayed = numMovesPlayed_;
    
    Cell cell = null;
        
    cell = board.getPieceLocation(new Piece(PieceType.KING, myColour_));
    if (cell == null) {
      myKingIsAlive = false;
      myKingColumn = -1;
      myKingRow = -1;
    } else {
      myKingIsAlive = true;
      myKingColumn = cell.getColumn();
      myKingRow = cell.getRow();
    }
    
    cell = board.getPieceLocation(new Piece(PieceType.ROOK, myColour_));
    if (cell == null) {
      myRookIsAlive = false;
      myRookColumn = -1;
      myRookRow = -1;
    } else {
      myRookIsAlive = true;
      myRookColumn = cell.getColumn();
      myRookRow = cell.getRow();
    }
    
    cell = board.getPieceLocation(new Piece(PieceType.KING, myColour_.reverse()));
    if (cell == null) {
      theirKingIsAlive = false;
      theirKingColumn = -1;
      theirKingRow = -1;
    } else {
      theirKingIsAlive = true;
      theirKingColumn = cell.getColumn();
      theirKingRow = cell.getRow();
    }
    
    cell = board.getPieceLocation(new Piece(PieceType.ROOK, myColour_.reverse()));
    if (cell == null) {
      theirRookIsAlive = false;
      theirRookColumn = -1;
      theirRookRow = -1;
    } else {
      theirRookIsAlive = true;
      theirRookColumn = cell.getColumn();
      theirRookRow = cell.getRow();
    }
  }
  
  public final Colour getColour() {
    return myColour == WHITE ? Colour.WHITE : Colour.BLACK;
  }
  
  protected final ArrayList<MoveDescription> getAllPossibleMoves() {
    // construct board
    char[][] board = new char[5][5];
    for (int column = 1; column <= 4; ++column) {
      for (int row = 1; row <= 4; ++row) {
        board[column][row] = '.';
      }
    }
    if (myKingIsAlive) {
      board[myKingColumn][myKingRow] = 'a';
    }
    if (myRookIsAlive) {
      board[myRookColumn][myRookRow] = 'a';
    }
    if (theirKingIsAlive) {
      board[theirKingColumn][theirKingRow] = 'b';
    }
    if (theirRookIsAlive) {
      board[theirRookColumn][theirRookRow] = 'b';
    }
    
    ArrayList<MoveDescription> allPossibleMoves = new ArrayList<MoveDescription> ();
    
    // generate all possible moves for my king
    if (myKingIsAlive) {
      int[][] possibleDirections = new int[][] {{-1, -1}, {-1, 0}, {-1, +1}, {0, -1}, {0, +1}, {+1, -1}, {+1, 0}, {+1, +1}};
      for (int[] direction : possibleDirections) {
        int column = myKingColumn + direction[0];
        int row = myKingRow + direction[1];
        if (1 <= column && column <= 4 && 1 <= row && row <= 4 && (board[column][row] == '.' || board[column][row] == 'b')) {
          allPossibleMoves.add(new MoveDescription("king", column, row));
        }
      }
    }
    
    // generate all possible moves for my rook
    if (myRookIsAlive) {
      int[][] possibleDirections = new int[][] {{-1, 0}, {+1, 0}, {0, -1}, {0, +1}};
      for (int[] direction : possibleDirections) {
        int column = myRookColumn + direction[0];
        int row = myRookRow + direction[1];
        while (1 <= column && column <= 4 && 1 <= row && row <= 4 && (board[column][row] == '.' || board[column][row] == 'b')) {
          allPossibleMoves.add(new MoveDescription("rook", column, row));
          if (board[column][row] == 'b') {
            break;
          } else {
            column += direction[0];
            row += direction[1];
          }
        }
      }
    }
    
    return allPossibleMoves;
  }
  
  public final String getName() {
    String result = getClass().getName();
    return result.substring(result.indexOf('$') + 1);
  }
}
