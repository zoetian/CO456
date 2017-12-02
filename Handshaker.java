import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.util.function.Function;

public class Handshaker {
	public static int HANDSHAKE_LENGTH = 10;
	
	// True if the handshake has failed for this series:
	private boolean handshakeFailed;
	// Keeps track of the latest board position:
	private BoardPosition latestBoardPosition;
	
	// Conditions for when we should be sending or receiving a handshake:
	private Function<Handshaker, Boolean> sendingHandshakeCondition, receivingHandshakeCondition;

	// Counts how many handshake moves have been sent while sending/receiving conditions have been met:
	private int sentHandshakeMoves, receivedHandshakeMoves;

	// To construct a Handshaker, you need to specify sending/receiving conditions:
	public Handshaker(Function<Handshaker, Boolean> sendingHandshakeCondition, Function<Handshaker, Boolean> receivingHandshakeCondition) {
		this.sendingHandshakeCondition = sendingHandshakeCondition;
		this.receivingHandshakeCondition = receivingHandshakeCondition;
	}

	// A handshake offerer, e.g. TeamDoubleAgent:
	public static Handshaker createHandshakeOfferer() {
		return new Handshaker(
				//sending condition for a Handshaker h:
				h -> !h.handshakeFailed && h.sentHandshakeMoves<10,
				//receiving condition for a Handshaker h:
				h -> !h.handshakeFailed && h.sentHandshakeMoves==10 && h.receivedHandshakeMoves<10);
	}
	
	// A handshake accepter, e.g. TeamHandshaker:
	public static Handshaker createHandshakeAccepter() {
		return new Handshaker(
				//sending condition for a Handshaker h:
				h -> !h.handshakeFailed && h.receivedHandshakeMoves==10 && h.sentHandshakeMoves<10,
				//receiving condition for a Handshaker h:
				h -> !h.handshakeFailed && h.receivedHandshakeMoves<10);
	}

	public void handshakePrepareForSeries() {
		sentHandshakeMoves=0;
		receivedHandshakeMoves=0;
		handshakeFailed = false;
		latestBoardPosition = null;
	}

	public void handshakePrepareForMatch(BoardPosition toBoardPosition) {
		this.latestBoardPosition = toBoardPosition;
	}

	public void handshakeReceiveMatchOutcome(int matchOutcome, BoardPosition currentBoardPosition) {
		updateTheirMove(currentBoardPosition);
	}

	public boolean shouldSendHandshakeMove() {
		return sendingHandshakeCondition.apply(this);
	}

	public boolean shouldReceiveHandshakeMove() {
		return receivingHandshakeCondition.apply(this);
	}

	public void receiveMyMove(MoveDescription move) {
		BoardPosition beforeBoardPosition=new BoardPosition(latestBoardPosition);
		
		latestBoardPosition = latestBoardPosition.doMove(move);
		
		if (shouldSendHandshakeMove()) {
			if (wasHandshakeMovePlayed(beforeBoardPosition,latestBoardPosition)) {
				++sentHandshakeMoves;
			}
		}
	}

	public void receiveTheirMove(MoveDescription move) {
		BoardPosition beforeBoardPosition=new BoardPosition(latestBoardPosition);
		
		latestBoardPosition = latestBoardPosition.doMove(move);
	
		if (shouldReceiveHandshakeMove()) {
			if (wasHandshakeMovePlayed(beforeBoardPosition,latestBoardPosition)) {
				++receivedHandshakeMoves;
			} else {
				handshakeFailed=true;
			}
		}
	}

	public void updateTheirMove(BoardPosition currentBoardPosition) {
		if (currentBoardPosition.toInt() != latestBoardPosition.toInt()) {
			for (MoveDescription move : latestBoardPosition.getAllPossibleMoves()) {
				if (currentBoardPosition.toInt() == (latestBoardPosition.doMove(move)).toInt()) {
					receiveTheirMove(move);
					return;
				}
			}
		}
	}

	private boolean wasHandshakeMovePlayed(BoardPosition beforeBoardPosition, BoardPosition afterBoardPosition) {
		// This simply checks if the handshake move goes from before to after.
		
		// We make copies with myColour=0 so both parties to handshake have exact same board position:
		beforeBoardPosition = new BoardPosition(beforeBoardPosition);
		beforeBoardPosition.myColour = 0;
		afterBoardPosition = new BoardPosition(afterBoardPosition);
		afterBoardPosition.myColour = 0;

		return beforeBoardPosition.doMove(getHandshakeMove(beforeBoardPosition)).toInt() == afterBoardPosition.toInt();
	}
	
	public boolean handshakeHasFailed() {
		return handshakeFailed;
	}

	public static MoveDescription getHandshakeMove(BoardPosition currentBoardPosition) {
		// We make a copy for modification:
		currentBoardPosition = new BoardPosition(currentBoardPosition);
		// Then we set myColour=0 so that both the DoubleAgent and Handshaker
		// will base their calculations on the exact same board position:
		currentBoardPosition.myColour = 0;

		// Find cells that contain pieces, so we can avoid them:
		HashSet<Integer> occupiedCellIds = new HashSet<Integer>();
		occupiedCellIds.add(currentBoardPosition.whiteKingPosition);
		occupiedCellIds.add(currentBoardPosition.whiteRookPosition);
		occupiedCellIds.add(currentBoardPosition.blackKingPosition);
		occupiedCellIds.add(currentBoardPosition.blackRookPosition);
		// Now let's list all non-capturing moves:
		List<MoveDescription> nonCaptureMoves = new ArrayList<MoveDescription>();
		for (MoveDescription move : currentBoardPosition.getAllPossibleMoves()) {
			int destinationCellId = BoardPosition.getCellId(true, move.getDestinationColumn(),
					move.getDestinationRow());
			if (!occupiedCellIds.contains(destinationCellId)) {
				nonCaptureMoves.add(move);
			}
		}
		// Or in the rare instances where all moves are capturing moves, choose
		// arbitrarily:
		if (nonCaptureMoves.size() == 0) {
			nonCaptureMoves = currentBoardPosition.getAllPossibleMoves();
		}
		// Hash the board position to pseudo-randomly select move.
		// Note: you do not need to understand this part exactly.
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			int moveIndex = new BigInteger(
					messageDigest.digest(BigInteger.valueOf(currentBoardPosition.toInt()).toByteArray()))
					.mod(BigInteger.valueOf(nonCaptureMoves.size())).intValue();
			return nonCaptureMoves.get(moveIndex);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Called getHandshakeMove, but NoSuchAlgorithmException was caught"+e);
			return nonCaptureMoves.get(0); // Never run
		}
	}
}
