package UnitTests;

import DataLayer.DbConfig;
import org.junit.jupiter.api.BeforeAll;

public abstract class UnitTest {
    @BeforeAll
    static void SET_DB() {
            DbConfig.setPERSIST(false);
    }
}
