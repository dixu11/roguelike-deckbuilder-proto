package dixu.deckard.server;

public class CardPlayedEvent implements Event {
    private final Team team;
    private final Card card;
    private final Minion minion;

    public CardPlayedEvent(Team team, Card card, Minion minion) {
        this.team = team;
        this.card = card;
        this.minion = minion;
    }

    public Team getTeam() {
        return team;
    }

    public Card getCard() {
        return card;
    }

    public Minion getMinion() {
        return minion;
    }
}
