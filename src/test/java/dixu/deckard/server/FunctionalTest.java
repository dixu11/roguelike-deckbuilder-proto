package dixu.deckard.server;

import dixu.deckard.server.event.ActionEventName;
import dixu.deckard.server.event.BusManager;
import dixu.deckard.server.event.CoreEvent;
import dixu.deckard.server.event.CoreEventName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static dixu.deckard.server.CardType.*;
import static dixu.deckard.server.GameParams.*;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class FunctionalTest {
    Team firstTeam;
    Team secondTeam;
    Leader firstLeader;
    Leader secondLeader;
     BusManager bus;

    @BeforeEach
    public void before() {
        setClassicParams();
        loadGame(); //remove if performance get worse because tests with custom params load engine twice
    }
    @AfterEach
    public void after() {
        //Buses are singleton and also needs to be re-initialized after each test
        BusManager.reInitialize();
    }

    private void setClassicParams() {
        MINION_PER_TEAM = 2;
        MINION_DRAW_PER_TURN = 2;
        SECOND_TEAM_INITIAL_BLOCK = 3;
        changeCardBaseValueTo(BASIC_MINION,3);
        changeCardBaseValueTo(BASIC_ATTACK,1);
        changeCardBaseValueTo(UPGRADED_ATTACK,2);
        changeCardBaseValueTo(BASIC_BLOCK,1);
        changeCardBaseValueTo(UPGRADED_BLOCK,2);
    }

    void changeCardBaseValueTo(CardType type,int value){
        type.setBaseValue(value);
    }

    int minionInitialHp() {
        return BASIC_MINION.getValue();
    }

    private void loadGame() { //todo refactor copying same code here and in App
        Fight.disableDelay();
        bus = BusManager.instance();
        LeaderFactory leaderFactory = new LeaderFactory();
        firstLeader = leaderFactory.create(LeaderType.PLAYER);
        secondLeader = leaderFactory.create(LeaderType.SIMPLE_BOT);
        firstTeam = firstLeader.getTeam();
        secondTeam = secondLeader.getTeam();
        Fight fight = new Fight(firstLeader,secondLeader);
        fight.start();
    }

    void reloadGame() {
        after(); //reset bus
        loadGame();
    }

    void executeTurn() {
        bus.post(CoreEvent.of(CoreEventName.TURN_ENDED));
    }

    List<Minion> allMinions() {
        List<Minion> all = new ArrayList<>();
        all.addAll(firstTeam.getMinions());
        all.addAll(secondTeam.getMinions());
        return all;
    }
    //todo could not find way to avoid repetition - my try was EventName interface but i can't figure out how to

    //put it back to overloaded  bus.register() call - it makes compile error -> 'suspicious call'
    <T> AtomicBoolean listenEventPosted(CoreEventName eventName) {
        //  T elem = eventName.getObject();
        AtomicBoolean wasPosted = new AtomicBoolean(false);
        bus.register(event -> wasPosted.set(true), eventName); // if i pass elem here - there's a problem
        return wasPosted;
    }

    AtomicBoolean listenEventPosted(ActionEventName eventName) {
        AtomicBoolean wasPosted = new AtomicBoolean(false);
        bus.register(event -> wasPosted.set(true), eventName);
        return wasPosted;
    }

    void failIfWasNotPosted(AtomicBoolean wasPosted) {
        if (!wasPosted.get()) {
            fail();
        }
    }

    void clearAllCards() {
        for (Minion minion : allMinions()) {
            clearMinionHand(minion);
            clearMinionDraw(minion);
        }
    }

    void clearMinionsHand(Team team) {
        giveMinionsCards(team);
    }

    void clearMinionHand(Minion minion) {
        composeMinionHand(minion);
    }

    void clearMinionDraw(Minion minion) {
        minion.clearDraw();
    }

    void disableBlockClear() {
        firstTeam.setClearBlockEnabled(false);
        secondTeam.setClearBlockEnabled(false);
    }

    /**
     * @param cards when no elements are passed minion has no cards in hand
     */
    void giveMinionsCards(Team team, CardType... cards) {
        team.getMinions()
                .forEach(minion -> composeMinionHand(minion, cards));
    }

    void giveAllMinionsBlockCard() {
        allMinions().forEach(minion -> composeMinionHand(minion, CardType.BASIC_BLOCK));
    }

     void composeMinionHand(Minion minion, CardType... cards) {
        CardFactory factory = new CardFactory();
        List<Card> newHand = new ArrayList<>();
        for (CardType type : cards) {
            newHand.addAll(factory.createCards(1, type));
        }
        minion.setHand(newHand);
    }

    public Minion firstMinion(Team team) {
        return team.getMinions().get(0);
    }

    public Minion secondMinion(Team team) {
        return team.getMinions().get(1);
    }

     Card minionHandFirstCard(Minion minion) {
        return minion.getHand().get(0);
    }

    void disableBasicAttack() {
        changeCardBaseValueTo(BASIC_ATTACK, 0);
    }

    void disableBasicBlock() {
        changeCardBaseValueTo(CardType.BASIC_BLOCK, 0);
    }
}
