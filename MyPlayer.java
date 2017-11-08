import java.util.*;

/*
 * This is a beta version. We will release version 1.0 Monday November 13th.
 * Please email us if you have any comments/bugs to report.
 */

public class MyPlayer extends Player {
  
  /*
  Throughout your code, you can use the following variables,
  which are used to encode the current state of the game.
  They are already declared in the parent class, and they will be
  updated for you. You do not need to change their values, that is,
  you should treat them as read-only variables.
  
  (0 if you are the white player, 1 if you are the black player. 
  You can also write WHITE and BLACK instead of 0 and 1.)
  int myColour;
  
  (the number of moves played so far)
  int numMovesPlayed; 
  
  (true if your king is alive, false otherwise)
  boolean myKingIsAlive;
  
  (the column and row of your king, if it is alive; (-1, -1) otherwise)
  int myKingColumn, myKingRow;
  
  (true if your rook is alive, false otherwise)
  boolean myRookIsAlive;
  
  (the column and row of your rook, if it is alive; (-1, -1) otherwise)
  int myRookColumn, myRookRow;
  
  (true if your opponent's king is alive, false otherwise)
  boolean theirKingIsAlive;
  
  (the column and row of your opponent's king, if it is alive; (-1, -1) otherwise)
  int theirKingColumn, theirKingRow;
  
  (true if your opponent's rook is alive, false otherwise)
  boolean theirRookIsAlive;
  
  (the column and row of your opponent's rook, if it is alive; (-1, -1) otherwise)
  int theirRookColumn, theirRookRow;
  */
  
  /*
  Note: you are also allowed to introduce new variables.
  */
  
  public MyPlayer(int maxNumMoves) {
    /*
    This is the constructor of your player.
    It will be used only once, before the tournament begins.
    
    At this point, the variables explained above (that describe the state of
    the game) are not initialized, so you should not use them.
    
    You can use this method to do precomputation.
    The time limit for this method is 60 seconds.
    The parameter maxNumMoves indicates the maximum number of moves in a match.
    
    After you change this function, you need to remove the line below 
    that throws an exception.
    */
    
    throw new RuntimeException("You need to implement the constructor.");
  }
  
  public void prepareForSeries() {
    /*
    This method will be called each time you are paired off with another player,
    immediately before you play a series of matches against them.
    
    You can use it to initialize variables or do some *light*
    precomputation.
    
    At this point, the variables explained above (that describe the state of
    the game) are not initialized, so you should not use them.

    The time limit for this method is 1 second.
    
    After you change this function, you need to remove the line below 
    that throws an exception.
    */
    
    throw new RuntimeException("You need to implement the method prepareForSeries.");
  }
  
  public void prepareForMatch() {
    /*
    This method will be called immediately before the beginning of each match.
    
    You can use it to initialize variables or do some *light*
    precomputation.
    
    At this point, the variables explained above (that describe the state of
    the game) are not initialized, so you should not use them.

    The time limit for this method is 1 second.
    
    After you change this function, you need to remove the line below 
    that throws an exception.
    */
    
    throw new RuntimeException("You need to implement the method prepareForMatch.");
  }
  
  public MoveDescription chooseMove() {
    /*
    This method will be called each time you need to make a move.
    
    At this point, the variables explained above (that describe the state of
    the game) have been correctly initialized, so you can use them.
    
    The time limit for this method is .1 second.
    
    Your method should return an object of type MoveDescription, which specifies
    which move you want to make. You can create such an object as in the following examples:
      
      MoveDescription move1 = new MoveDescription("king", 3, 4); 
      // this indicates you want to move your king to column 3 and row 4
      
      MoveDescription move2 = new MoveDescription("rook", 2, 1);
      // this indicates you want to move your rook to column 2 and row 1
    
    You are allowed to use the other functions of the class MoveDescription as well,
    in order to access the fields of your object.
    
    For move1 defined above, for example:
      move.getPieceToMove() will return "king"
      move.getDestinationColumn() will return 3
      move.getDestinationRow() will return 4
    
    After you change this function, you need to remove the line below 
    that throws an exception.
     */
    
    throw new RuntimeException("You need to implement the method chooseMove.");
  }
}
