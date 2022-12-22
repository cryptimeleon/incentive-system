package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import java.util.Objects;

/**
 * A class representing the third message of the Issue  {@literal <}-{@literal >} Join protocol.
 */
public class JoinResponse implements Representable {
    private final SPSEQSignature preCertificate; // preliminary certificate for the user token

    private final ZnElement eskProv; // the share of the provider for the ElGamal encryption key for the initial token of the user

    public JoinResponse(Representation repr, IncentivePublicParameters pp) {
        // force passed representation into a list representation (does not throw class cast exception in intended use cases)
        var list = (ListRepresentation) repr;

        // retrieve restorers
        var usedZn = pp.getBg().getZn();
        var usedG1 = pp.getBg().getG1();
        var usedG2 = pp.getBg().getG2();

        // restore fields
        this.preCertificate = new SPSEQSignature(list.get(0), usedG1, usedG2);
        this.eskProv = usedZn.restoreElement(list.get(1));
    }

    public JoinResponse(SPSEQSignature preCertificate, ZnElement eskProv) {
        this.preCertificate = preCertificate;
        this.eskProv = eskProv;
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                this.preCertificate.getRepresentation(),
                this.eskProv.getRepresentation()
        );
    }

    public SPSEQSignature getPreCertificate() {
        return this.preCertificate;
    }

    public ZnElement getEskProv() {
        return this.eskProv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoinResponse that = (JoinResponse) o;
        return Objects.equals(preCertificate, that.preCertificate) && Objects.equals(eskProv, that.eskProv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preCertificate, eskProv);
    }

    public String toString() {
        return "JoinResponse(preCertificate=" + this.getPreCertificate() + ", eskProv=" + this.getEskProv() + ")";
    }
}
