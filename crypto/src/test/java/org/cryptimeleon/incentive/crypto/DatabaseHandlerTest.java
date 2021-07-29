package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.logging.Logger;

/**
 * Contains some simple tests ensuring that the JSON marshalling and Base64 encoding of the transaction objects works.
 */
public class DatabaseHandlerTest {
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    public void transactionToJsonTest() {
        Zn usedZn = new Zn(new BigInteger("1000"));
        Transaction ta = new Transaction(usedZn.createZnElement(new BigInteger("164")), // tid
                new BigInteger("103"), // spend amount
                new DoubleSpendingTag(
                        usedZn.createZnElement(new BigInteger("17")), // c0
                        usedZn.createZnElement(new BigInteger("19")), // c1
                        usedZn.createZnElement(new BigInteger("21")), // gamma
                        usedZn.createZnElement(new BigInteger("23")), // esk prov
                        null, // ctrace0
                        null // ctrace1
                ));

        DatabaseHandler dbHandler = new DatabaseHandler("");
        dbHandler.addTransactionNode(ta);
    }
}
