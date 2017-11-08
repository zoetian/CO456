
public class MoveDescription {
  
  private String pieceToMove;
  private int destinationColumn, destinationRow;
  
  public MoveDescription(String pieceToMove_, int destinationColumn_, int destinationRow_) {
    pieceToMove = pieceToMove_;
    destinationColumn = destinationColumn_;
    destinationRow = destinationRow_;
  }
  
  public String getPieceToMove() {
    return pieceToMove;
  }
  
  public int getDestinationColumn() {
    return destinationColumn;
  }
  
  public int getDestinationRow() {
    return destinationRow;
  }
  
  public String toString() {
    return "(" + pieceToMove + ", " + destinationColumn + ", " + destinationRow + ")";
  }
}
