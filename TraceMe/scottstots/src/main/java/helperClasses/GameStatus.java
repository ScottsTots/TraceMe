package helperClasses;

/**
 * Created by matthewebeweber on 4/5/14.
 */

public enum GameStatus {
    WAITING_FOR_OPPONENT("Waiting on Opponent..", 0),
    IN_PROGRESS("Game in Progress..", 1),
    GAME_OVER("Game Over..", 2),
    CHALLENGED("Challenged", 3),
    CHALLENGE_DECLINED("Challenge Declined", 4);

    private int intValue;
    private String stringValue;
    public int id;

    private GameStatus(String stringValue, int intValue) {
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.id = intValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
