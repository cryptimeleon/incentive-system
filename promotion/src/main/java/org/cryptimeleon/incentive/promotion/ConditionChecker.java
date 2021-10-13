package org.cryptimeleon.incentive.promotion;

import java.util.List;
import java.util.stream.Collectors;

public class ConditionChecker {

    // + generate conditions from reward
    // + parse and evaluate ZKPs
    // + which side effect on token?

    boolean checkConditions(List<Condition> conditions, long basketValue, List<Condition> zkpProvedConditions) {

        List<Condition> conditionsAfterBasket = processConditionsWithBasketValue(conditions, basketValue);

        // No further conditions to check using ZKPs
        if (conditionsAfterBasket.isEmpty()) return true;

        return processConditionsWithZkpResults(conditions, zkpProvedConditions);
    }

    boolean processConditionsWithZkpResults(List<Condition> conditions, List<Condition> zkpProvedConditions) {
        return zkpProvedConditions.containsAll(conditions);
    }

    /**
     * Outputs all conditions that are not satisfied by the basket value.
     */
    List<Condition> processConditionsWithBasketValue(List<Condition> conditions, long basketValue) {
        return conditions.stream().filter(
                condition -> {
                    if (condition instanceof GreaterEqualCondition) {
                        GreaterEqualCondition geqCondition = (GreaterEqualCondition) condition;
                        return basketValue < geqCondition.getValue();
                    }
                    if (condition instanceof LessEqualCondition) {
                        LessEqualCondition leqCondition = (LessEqualCondition) condition;
                        return basketValue > leqCondition.getValue();
                    }
                    return true;
                }).map(
                condition -> {
                    if (condition instanceof GreaterEqualCondition) {
                        GreaterEqualCondition geqCondition = (GreaterEqualCondition) condition;
                        return new GreaterEqualCondition(geqCondition.getValue() - basketValue);
                    }
                    if (condition instanceof LessEqualCondition) {
                        LessEqualCondition leqCondition = (LessEqualCondition) condition;
                        return new LessEqualCondition(leqCondition.getValue() + basketValue);
                    }
                    return condition;
                }).collect(Collectors.toList());
    }
}
