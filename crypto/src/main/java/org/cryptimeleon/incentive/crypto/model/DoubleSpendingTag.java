package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
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
    private Zn.ZnElement c0; // challenge for deriving the user secret key

    @Represented(restorer = "Zn")
    private Zn.ZnElement c1; // challenge for deriving the encryption secret key

    @Represented(restorer = "Zn")
    private Zn.ZnElement gamma; // challenge generation helper value

    @Represented(restorer = "Zn")
    private Zn.ZnElement eskStarProv; // provider share for ElGamal encryption secret key

    @Represented(restorer = "G1")
    private GroupElementVector ctrace0;

    @Represented(restorer = "G1")
    private GroupElementVector ctrace1;

    public DoubleSpendingTag(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "Zn").register(pp.getBg().getG1(), "G1").deserialize(repr);
    }

    /**
     * Constructs a double-spending tag from serialized representations of its components.
     */
    public DoubleSpendingTag(IncentivePublicParameters pp, String serializedC0Repr, String serializedC1Repr, String serializedGammaRepr, String serializedEskProvStarRepr, String serializedCTrace0Repr, String serializedCTrace1Repr) {
        Zn usedZn = pp.getBg().getZn();
        Group groupG1 = pp.getBg().getG1();
        JSONConverter jsonConverter = new JSONConverter();

        Representation c0Repr = jsonConverter.deserialize(serializedC0Repr);
        this.c0 = usedZn.restoreElement(c0Repr);

        Representation c1Repr = jsonConverter.deserialize(serializedC1Repr);
        this.c1 = usedZn.restoreElement(c1Repr);

        Representation gammaRepr = jsonConverter.deserialize(serializedGammaRepr);
        this.gamma = usedZn.restoreElement(gammaRepr);

        Representation eskProvRepr = jsonConverter.deserialize(serializedEskProvStarRepr);
        this.eskStarProv = usedZn.restoreElement(eskProvRepr);

        Representation cTrace0Repr = jsonConverter.deserialize(serializedCTrace0Repr);
        this.ctrace0 = groupG1.restoreVector(cTrace0Repr);

        Representation cTrace1Repr = jsonConverter.deserialize(serializedCTrace1Repr);
        this.ctrace1 = groupG1.restoreVector(cTrace1Repr);
    }

    public DoubleSpendingTag(Zn.ZnElement c0, Zn.ZnElement c1, Zn.ZnElement gamma, Zn.ZnElement eskStarProv, GroupElementVector ctrace0, GroupElementVector ctrace1) {
        this.c0 = c0;
        this.c1 = c1;
        this.gamma = gamma;
        this.eskStarProv = eskStarProv;
        this.ctrace0 = ctrace0;
        this.ctrace1 = ctrace1;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public String toString() {
        return this.c0.toString() + " "
                + this.c1.toString() + " "
                + this.eskStarProv.toString() + " "
                + this.gamma.toString() + " "
                + this.ctrace0.toString() + " "
                + this.ctrace1.toString();
    }

    public Zn.ZnElement getC0() {
        return this.c0;
    }

    public Zn.ZnElement getC1() {
        return this.c1;
    }

    public Zn.ZnElement getGamma() {
        return this.gamma;
    }

    public Zn.ZnElement getEskStarProv() {
        return this.eskStarProv;
    }

    public GroupElementVector getCtrace0() {
        return this.ctrace0;
    }

    public GroupElementVector getCtrace1() {
        return this.ctrace1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleSpendingTag that = (DoubleSpendingTag) o;
        return Objects.equals(c0, that.c0) && Objects.equals(c1, that.c1) && Objects.equals(gamma, that.gamma) && Objects.equals(eskStarProv, that.eskStarProv) && Objects.equals(ctrace0, that.ctrace0) && Objects.equals(ctrace1, that.ctrace1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c0, c1, gamma, eskStarProv, ctrace0, ctrace1);
    }
}
