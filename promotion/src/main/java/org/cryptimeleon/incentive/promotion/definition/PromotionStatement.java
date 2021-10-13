package org.cryptimeleon.incentive.promotion.definition;

import lombok.AllArgsConstructor;

import java.util.function.Function;

public class PromotionStatement {

    boolean verify(StatementTree statementTree) {
        return statementTree instanceof TrueLeafStatement;
    }

    StatementTree applyKnowledge(StatementTree statementTree, KnowledgeProvider knowledgeProvider) {
        return applyKnowledgeImpl(statementTree, unevaluatedLeafStatement -> {
            Statement resultStatement = knowledgeProvider.applyKnowledge(unevaluatedLeafStatement.statement);
            if (resultStatement instanceof Statement.FalseStatement) return new FalseLeafStatement();
            if (resultStatement instanceof Statement.TrueStatement) return new TrueLeafStatement();
            return new UnevaluatedLeafStatement(resultStatement);
        });
    }



    private StatementTree simplify(StatementTree statementTree) {
        return applyKnowledgeImpl(statementTree, s -> s);
    }

    /**
     * Apply knowledge to Statements and simplify boolean formula.
     * Evaluates the boolean formula in Postorder and simplifies formula if possible, e.g. false ^ x => false without
     * evaluating x.
     *
     * @param statementTree          the initial statement tree
     * @param applyKnowledgeFunction a function that maps statements to statements trees by applying some knowledge,
     *                               e.g. evaluating statements to true or false
     * @return the simplified statement tree
     */
    private StatementTree applyKnowledgeImpl(StatementTree statementTree, Function<UnevaluatedLeafStatement, StatementTree> applyKnowledgeFunction) {
        if (statementTree instanceof AndStatement) {
            AndStatement andStatement = (AndStatement) statementTree;

            StatementTree left = applyKnowledgeImpl(andStatement.leftStatement, applyKnowledgeFunction);
            if (left instanceof FalseLeafStatement) {
                return new FalseLeafStatement();
            }

            StatementTree right = applyKnowledgeImpl(andStatement.rightStatement, applyKnowledgeFunction);
            if (right instanceof FalseLeafStatement) {
                return new FalseLeafStatement();
            }

            if (left instanceof TrueLeafStatement && right instanceof TrueLeafStatement) {
                return new TrueLeafStatement();
            } else if (left instanceof TrueLeafStatement) {
                return right;
            } else if (right instanceof TrueLeafStatement) {
                return left;
            } else {
                return new AndStatement(left, right);
            }
        } else if (statementTree instanceof OrStatement) {
            OrStatement orStatement = (OrStatement) statementTree;

            StatementTree left = applyKnowledgeImpl(orStatement.leftStatement, applyKnowledgeFunction);
            if (left instanceof TrueLeafStatement) {
                return new TrueLeafStatement();
            }

            StatementTree right = applyKnowledgeImpl(orStatement.rightStatement, applyKnowledgeFunction);
            if (right instanceof TrueLeafStatement) {
                return new TrueLeafStatement();
            }

            if (left instanceof FalseLeafStatement && right instanceof FalseLeafStatement) {
                return new FalseLeafStatement();
            } else if (left instanceof FalseLeafStatement) {
                return right;
            } else if (right instanceof FalseLeafStatement) {
                return left;
            } else {
                return new OrStatement(left, right);
            }
        } else if (statementTree instanceof UnevaluatedLeafStatement) {
            return applyKnowledgeFunction.apply((UnevaluatedLeafStatement) statementTree);
        } else {
            return statementTree;
        }
    }

    static class StatementTree {

    }

    @AllArgsConstructor
    static class AndStatement extends StatementTree {
        final StatementTree leftStatement;
        final StatementTree rightStatement;
    }

    @AllArgsConstructor
    static class OrStatement extends StatementTree {
        final StatementTree leftStatement;
        final StatementTree rightStatement;
    }

    @AllArgsConstructor
    static class UnevaluatedLeafStatement extends StatementTree {
        final Statement statement;
    }

    static class TrueLeafStatement extends StatementTree {
    }

    static class FalseLeafStatement extends StatementTree {
    }
}
