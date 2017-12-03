import java.util.Random;

public class TeamTwitchForTat extends TeamTitForTat {
	
	private Random rand;
	private int twitchFrequency;
	
	public TeamTwitchForTat(int maxNumMoves) {
		super(maxNumMoves);
		this.rand = new Random(System.currentTimeMillis());
		this.twitchFrequency=20;
	}
	
	public int updateTrust(int oldTrust, int myMatchPayoff) {
		if (myMatchPayoff < 2) {
			// I didn't take your king? I trust you less now.
			oldTrust -= BETRAYAL_DELTA;
		} else if (opponentHadWinningPosition && myMatchPayoff >= 2) {
			// You gave up a win? I trust you more now.
			oldTrust += COOPERATION_DELTA;

		}
		
		// 1/twitchFrequency chance of randomly decreasing (increasing) trust:
		int twitch = rand.nextInt(twitchFrequency);
		if (twitch==0) {
			return oldTrust-1;
		} else if (twitch==1) {
			return oldTrust+1;
		}
		return oldTrust;
	}
}
