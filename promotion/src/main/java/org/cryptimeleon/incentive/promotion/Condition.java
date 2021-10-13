package org.cryptimeleon.incentive.promotion;

abstract public class Condition {
    abstract String getDescription();

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("Not implemented!");
    }
}
