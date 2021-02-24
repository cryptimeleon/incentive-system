package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.math.serialization.converter.JSONPrettyConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SetupTest {

    JSONPrettyConverter jsonPrettyConverter = new JSONPrettyConverter();

    @Test
    void testSetup() {
        var pp = IncentiveSystem.setup();
        var serializedPP = jsonPrettyConverter.serialize(pp.getRepresentation());
        var deserializedPP = new IncentivePublicParameters(jsonPrettyConverter.deserialize(serializedPP));

        assertThat(deserializedPP).isEqualTo(pp);
    }
}
