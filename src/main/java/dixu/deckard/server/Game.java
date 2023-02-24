package dixu.deckard.server;

public class Game implements EventHandler{
    private final EventBus eventBus = new EventBus();
    private final Player player;
    private final Computer computer;


    public Game(Player player, Computer computer) {
        this.player = player;
        this.computer = computer;
    }

    public void start() {
        eventBus.register(this,GameStartedEvent.class);
        eventBus.register(this,GameOverEvent.class);
        eventBus.register(this, NextTurnEvent.class);
        eventBus.post(new GameStartedEvent());
    }

    @Override
    public void handle(Event event) {
        if (event instanceof GameStartedEvent) {
            System.out.println("Game: started");
        }else if (event instanceof NextTurnEvent){
            System.out.println("Game: next turn");
        }

    }

    public void nextTurn(){

    }

    public void onCardPlayed(Event event) {

    }

    public void onGameOver(Event event) {

    }
    //metody dla clienta:

    public void playCard(Player player, int index) {
        Card card = player.playCard(index);
        eventBus.post(new CardPlayedEvent(player, card,null ));
    }

    public void endTurn() {
        eventBus.post(new NextTurnEvent());
    }

    //get current player


}
