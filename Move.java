

public class Move {
	private PieceType pieceType;
	private Cell destination;

	public Move(MoveDescription moveDescription) {
		if (moveDescription.getPieceToMove().equals("king")) {
			pieceType = PieceType.KING;
		} else if (moveDescription.getPieceToMove().equals("rook")) {
			pieceType = PieceType.ROOK;
		} else {
			throw new RuntimeException("Invalid piece type.");
		}

		destination = new Cell(moveDescription.getDestinationColumn(), moveDescription.getDestinationRow());
		if (!destination.isValid()) {
			throw new RuntimeException("Invalid destination.");
		}
	}

	public PieceType getPieceType() {
		return pieceType;
	}

	public Cell getDestination() {
		return destination;
	}
}
