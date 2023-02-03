package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Objects;

/**
 * Data associated to a spend operation which the provider requires to trace double-spending.
 * c0, c1 and gamma allow to reveal a double-spending user's secret key and the ElGamal secret key
 * used to trace the remainder token of the malicious transaction.
 * Called 'dstag' in the cryptimeleon incentive system paper.
 */
public class DoubleSpendingTag implements Representable {
    @Represented(restorer = "Zn")
    private Zn.ZnElement c; // challenge for deriving the user secret key

    @Represented(restorer = "Zn")
    private Zn.ZnElement gamma; // challenge generation helper value

    public DoubleSpendingTag(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "Zn").deserialize(repr);
    }

    /**
     * Constructs a double-spending tag from serialized representations of its components.
     */
    public DoubleSpendingTag(IncentivePublicParameters pp, String serializedCRepr, String serializedGammaRepr) {
        Zn usedZn = pp.getBg().getZn();
        JSONConverter jsonConverter = new JSONConverter();

        Representation c0Repr = jsonConverter.deserialize(serializedCRepr);
        this.c = usedZn.restoreElement(c0Repr);

        Representation gammaRepr = jsonConverter.deserialize(serializedGammaRepr);
        this.gamma = usedZn.restoreElement(gammaRepr);
    }

    public DoubleSpendingTag(Zn.ZnElement c, Zn.ZnElement gamma) {
        this.c = c;
        this.gamma = gamma;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public String toString() {
        return this.c.toString() + " "
                + this.gamma.toString();
    }

    public Zn.ZnElement getC() {
        return c;
    }

    public Zn.ZnElement getGamma() {
        return gamma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleSpendingTag that = (DoubleSpendingTag) o;
        return Objects.equals(c, that.c) && Objects.equals(gamma, that.gamma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c, gamma);
    }
}
