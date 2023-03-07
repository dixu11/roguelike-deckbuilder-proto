package dixu.deckard.server.effect;

import dixu.deckard.server.Card;
import dixu.deckard.server.CardContext;

public class BlockEffect extends BasicEffect implements CardEffect {
    private int value;

    public BlockEffect(int value, Card card) {
        super(value,card);
        this.value = value;
    }

    @Override
    public void execute(CardContext context) {
        context.getOwnTeam().addBlock(value);
    }

    @Override
    public String getDescription() {
        return  " +" + value + "\uD83D\uDEE1";
    }

    @Override
    public int getAttack() {
        return 0;
    }

    @Override
    public int getBlock() {
        return value;
    }
}
