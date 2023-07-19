package DataLayer;

import DataLayer.ORM.DataPermission;
import DataLayer.ORM.DataTransactedProduct;
import DataLayer.ORM.DataTransaction;
import DataLayer.Services.NotificationService.ORM.DataNotification;
import DataLayer.Store.ORM.*;
import DataLayer.Store.ORM.Contract.DataAppointConsent;
import DataLayer.Store.ORM.Contract.DataAppointment;
import DataLayer.Store.ORM.Discount.*;
import DataLayer.User.ORM.*;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Map;

public class DbConfig {

    private static Configuration CONF = null;

    private static final Map<String, String> DRIVERS = Map.of("mysql", "com.mysql.jdbc.Driver", "postgresql", "org.postgresql.Driver");
    private static final Map<String, String> DIALECTS = Map.of("mysql", "com.mysql.jdbc.Driver", "postgresql", "org.hibernate.dialect.PostgreSQLDialect");

    private static SessionFactory SESSION_FACTORY = null;
    private static Boolean PERSIST = null;

    public static void init(){
        //add annotated classes
    }
    public static void config(String dbType, String host, String port, String db, String user, String password){
        config(dbType, host, port, db, user, password, "update");
    }

    public static void config(String dbType, String host, String port, String db, String user, String password, String mode){
        CONF = new Configuration();
        CONF.setProperty("hibernate.connection.driver_class", DbConfig.DRIVERS.get(dbType));
        CONF.setProperty("hibernate.connection.url", String.format("jdbc:%s://%s:%s/%s", dbType, host, port, db));
        CONF.setProperty("hibernate.connection.username", user);
        CONF.setProperty("hibernate.connection.password", password);
        CONF.setProperty("hibernate.dialect", DbConfig.DIALECTS.get(dbType));
        CONF.setProperty("hibernate.hbm2ddl.auto", mode);

        CONF.addAnnotatedClass(DataProduct.class);
        CONF.addAnnotatedClass(DataMemberAddress.class);
        CONF.addAnnotatedClass(DataStore.class);
        CONF.addAnnotatedClass(DataBaskedProduct.class);
        CONF.addAnnotatedClass(DataBasket.class);
        CONF.addAnnotatedClass(DataCart.class);
        CONF.addAnnotatedClass(DataMember.class);
        CONF.addAnnotatedClass(DataPermission.class);
        CONF.addAnnotatedClass(DataNotification.class);
        CONF.addAnnotatedClass(DataSystemManager.class);
        CONF.addAnnotatedClass(DataTransaction.class);
        CONF.addAnnotatedClass(DataTransactedProduct.class);
        CONF.addAnnotatedClass(DataOffer.class);
        CONF.addAnnotatedClass(DataOfferConsent.class);
        CONF.addAnnotatedClass(DataDiscount.class);
        CONF.addAnnotatedClass(DataSimpleDiscount.class);
        CONF.addAnnotatedClass(DataCompositeDiscount.class);
        CONF.addAnnotatedClass(DataAddDiscount.class);
        CONF.addAnnotatedClass(DataAndDiscount.class);
        CONF.addAnnotatedClass(DataCategoryDiscount.class);
        CONF.addAnnotatedClass(DataIfThenDiscount.class);
        CONF.addAnnotatedClass(DataMaxDiscount.class);
        CONF.addAnnotatedClass(DataOrDiscount.class);
        CONF.addAnnotatedClass(DataProductDiscount.class);
        CONF.addAnnotatedClass(DataStoreDiscount.class);
        CONF.addAnnotatedClass(DataXorDiscount.class);
        CONF.addAnnotatedClass(DataPurchaseRule.class);
        CONF.addAnnotatedClass(DataCondition.class);
        CONF.addAnnotatedClass(DataConditionDiscount.class);
        CONF.addAnnotatedClass(DataConditionRule.class);
        CONF.addAnnotatedClass(DataAppointment.class);
        CONF.addAnnotatedClass(DataAppointConsent.class);

        setPERSIST(true);

        try {
            closeSession();
            SESSION_FACTORY = CONF.buildSessionFactory();
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeSession(){
        if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed())
            SESSION_FACTORY.close();
    }

    public static void setPERSIST(boolean PERSIST) {
        //if (DbConfig.PERSIST != null && PERSIST != DbConfig.PERSIST)
        //    throw new RuntimeException("Can't change persistence mode once set");
        DbConfig.PERSIST = PERSIST;
    }

    public static boolean shouldPersist(){
        if (PERSIST == null)
            throw new RuntimeException("Persistence mode wasn't set");
        return PERSIST;
    }

    public static SessionFactory getSessionFactory(){
        if (SESSION_FACTORY == null)
            throw new RuntimeException("Hibernate wasn't configured.");
        return SESSION_FACTORY;
    }
}
