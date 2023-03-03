package dixu.deckard.server.event;

import dixu.deckard.server.*;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ActionEvent implements Event<ActionEventName> {

    //when adding field update lombok builder on bottom!
    private ActionEventName name;
    private Object source;
    private Team ownTeam;
    private Team enemyTeam;
    private Minion minion;
    private Card card;
    private Card oldCard;
    private int value;
    private Leader leader;

    public static ActionEvent of(ActionEventName name, CardContext context) {
        return ofName(name)
                .ownTeam(context.getOwnTeam())
                .enemyTeam(context.getEnemyTeam())
                .minion(context.getMinion())
                .card(context.getCard())
                .build();
    }

    private static ActionEventBuilder ofName(ActionEventName name) {
        return builder()
                .name(name);
    }

    public boolean isSpecial() {
        return name.isSpecial();
    }

    @Override
    public ActionEventName getName() {
        return name;
    }

    public static class ActionEventBuilder {

        public ActionEvent build() {
            ActionEvent actionEvent = new ActionEvent(this.name, this.source, this.ownTeam,
                    this.enemyTeam, this.minion, this.card,this.oldCard, this.value,this.leader);
            actionEvent.source = ActionEventName.determineSourceFromEventName(actionEvent);

            return actionEvent;
        }
    }
}
