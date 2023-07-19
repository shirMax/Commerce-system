package UnitTests.DataLayerTests;

import DataLayer.DbConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class PersistenceTest {
    private static final String dbType = "postgresql";
    private static final String host = "139.59.209.55";
    private static final String port = "5432";
    private static final String db = "test_roi";
    private static final String user = "postgres";
    private static final String password = "123";
    private static final String mode = "create";

    @BeforeEach
    void dbSetup () {
        DbConfig.config(dbType, host, port, db, user, password, mode);
    }

    abstract void initRepo();

    @AfterEach
    protected void closeSession(){
        DbConfig.closeSession();
    }

    protected void connectToUpdate(){
        DbConfig.config(dbType, host, port, db, user, password, "update");
    }

    protected void closeReopen(){
        // Close-Reopen system
        closeSession();
        connectToUpdate();
        initRepo();
    }

}
