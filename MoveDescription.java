
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

	public int toInt() {
		//Format: bit 0 indicates which piece to move; 0 for king, 1 for rook
		// bits 2-3 indicate column, bits 4-5 indicate row.
		int piecebit=0;
		switch (pieceToMove) {
		case "king": piecebit=0;
			break;
		case "rook": piecebit=1;
			break;
		}
		return (piecebit)
				+((destinationColumn-1)<<1)
				+((destinationRow-1)<<3);
	}
	
	public MoveDescription(int moveint) {
		int piecebit= moveint-((moveint>>1)<<1);
		switch (piecebit) {
		case 0: pieceToMove="king";
			break;
		case 1: pieceToMove="rook";
			break;
		}
		destinationColumn = ((moveint>>1)-((moveint>>3)<<2))+1;
		destinationRow = ((moveint>>3)-((moveint>>5)<<2))+1;
	}
}
