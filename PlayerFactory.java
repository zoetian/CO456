import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class PlayerFactory {
    private final Map<String, Function<Integer, Player>> registeredPlayers = 
                                                                new HashMap<>();

    public PlayerFactory registerPlayer (String PlayerID, 
                                 Function<Integer, Player> PlayerFactory) {
        registeredPlayers.put(PlayerID, PlayerFactory);
        return this;
    }

    public Player createPlayer(String PlayerID, int maxNumMoves) {
        return registeredPlayers.get(PlayerID).apply(maxNumMoves);
    }
    
    public Set<String> playerIds() {
    	return registeredPlayers.keySet();
    }
}
