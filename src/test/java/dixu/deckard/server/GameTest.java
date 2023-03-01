package dixu.deckard.server;

import dixu.deckard.server.event.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static dixu.deckard.server.GameParams.*;
import static org.junit.jupiter.api.Assertions.*;

//tests for core game functionalities
class GameTest {

    private Team firstTeam;
    private Team secondTeam;
    private BusManager bus;

    @BeforeEach
    public void before() {
        Game.disableDaley();
        bus = BusManager.instance();
        TeamFactory factory = new TeamFactory();
        firstTeam = factory.createFirst();
        secondTeam = factory.createSecond();
        Game game = new Game(firstTeam, secondTeam);
        game.start();
    }

    @AfterEach
    public void after() {
        //Buses are singleton and also needs to be re-initialized after each test
        BusManager.reset();
    }

    @Test
    @DisplayName("On start teams have correct block")
    public void test1() {
        assertEquals(0, firstTeam.getBlock());
        assertEquals(SECOND_TEAM_INITIAL_BLOCK, secondTeam.getBlock());
    }

    @Test
    @DisplayName("On start all minions draw two cards, has 0 discarded and 2 in draw deck")
    public void test2() {
        for (Minion minion : allMinions()) {
            assertEquals(MINION_DRAW_PER_TURN, minion.getHand().size());
            assertEquals(INITIAL_MINION_DECK_SIZE - MINION_DRAW_PER_TURN,
                    minion.getDraw().size());
            assertEquals(0, minion.getDiscard().size());
        }
    }

    @Test
    @DisplayName("On start all minions have correct hp")
    public void test3() {
        for (Minion minion : allMinions()) {
            assertEquals(MINION_INITIAL_HP, minion.getHealth());
        }
    }

    @Test
    @DisplayName("Block card gives block to team")
    public void test4() {
        disableClearBlock();
        giveAllMinionsBlockCard();

        executeTurn();
        int blockFromCards = DEFAULT_BLOCK_VALUE * MINION_PER_TEAM;

        assertEquals(blockFromCards, firstTeam.getBlock());
        assertEquals(blockFromCards + SECOND_TEAM_INITIAL_BLOCK, secondTeam.getBlock());
    }

    @Test
    @DisplayName("Block is cleared for every team, first team - after plays, second - before plays")
    public void test5() {
        giveAllMinionsBlockCard();

        executeTurn();
        int blockFromCards = DEFAULT_BLOCK_VALUE * MINION_PER_TEAM;

        assertEquals(0, firstTeam.getBlock());
        assertEquals(blockFromCards, secondTeam.getBlock());
    }

    @Test
    @DisplayName("After slaying all minions enemy has empty team and game ends")
    public void test6() {
        DEFAULT_BLOCK_VALUE = 0;
        DEFAULT_ATTACK_VALUE = 3;
        giveMinionsCards(firstTeam, CardType.ATTACK, CardType.ATTACK);
        giveMinionsCards(secondTeam, CardType.BLOCK);
        AtomicBoolean wasPosted = listenEventPosted(CoreEventName.GAME_OVER);

        executeTurn();

        assertTrue(secondTeam.getMinions().isEmpty());
        failIfWasNotPosted(wasPosted);
    }

    @Test
    @DisplayName("After character died proper event is post and it's no longer in team")
    public void test7() {
        DEFAULT_ATTACK_VALUE = 3;
        giveMinionsCards(firstTeam, CardType.ATTACK);
        clearMinionsHand(secondTeam);
        AtomicBoolean wasPosted = listenEventPosted(ActionEventName.MINION_DIED);

        executeTurn();

        failIfWasNotPosted(wasPosted);
        assertEquals(MINION_PER_TEAM - 1, secondTeam.getMinions().size());
    }

    private void failIfWasNotPosted(AtomicBoolean wasPosted) {
        if (!wasPosted.get()) {
            fail();
        }
    }

    //todo could not find way to avoid repetition - my try was EventName interface but i can't figure out how to
    //put it back to overloaded bus.register call - it makes compile error - suspicious call
    private AtomicBoolean listenEventPosted(CoreEventName eventName) {
        AtomicBoolean wasPosted = new AtomicBoolean(false);
        bus.register(event -> wasPosted.set(true), eventName);
        return wasPosted;
    }

    private AtomicBoolean listenEventPosted(ActionEventName eventName) {
        AtomicBoolean wasPosted = new AtomicBoolean(false);
        bus.register(event -> wasPosted.set(true), eventName);
        return wasPosted;
    }

    private void clearMinionsHand(Team team) {
        giveMinionsCards(team);
    }

    private void executeTurn() {
        bus.post(CoreEvent.of(CoreEventName.TURN_ENDED));
    }

    private void disableClearBlock() {
        firstTeam.setClearBlockEnabled(false);
        secondTeam.setClearBlockEnabled(false);
    }

    private List<Minion> allMinions() {
        List<Minion> all = new ArrayList<>();
        all.addAll(firstTeam.getMinions());
        all.addAll(secondTeam.getMinions());
        return all;
    }

    /**
     * @param cards when no elements are passed minion has no cards in hand
     */
    private void giveMinionsCards(Team team, CardType... cards) {
        team.getMinions()
                .forEach(minion -> composeMinionHand(minion, cards));
    }

    private void giveAllMinionsBlockCard() {
        allMinions().forEach(minion -> composeMinionHand(minion, CardType.BLOCK));
    }

    private void composeMinionHand(Minion minion, CardType... cards) {
        CardFactory factory = new CardFactory();
        List<Card> newHand = new ArrayList<>();
        for (CardType type : cards) {
            newHand.addAll(factory.createCards(1, type));
        }
        minion.setHand(newHand);
    }
}
