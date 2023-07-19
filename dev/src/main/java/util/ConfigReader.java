package util;

import DataLayer.DbConfig;
import Service.ISystemFacade;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    public static void ReadConfig(ISystemFacade systemFacade) {
        Properties properties = new Properties();
        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
            properties.load(fileInputStream);
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //db details
        String dbType = properties.getProperty("dbType");
        String host = properties.getProperty("host");
        String port = properties.getProperty("port");
        String db = properties.getProperty("db");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");
        String mode = properties.getProperty("mode");

        //scenario details
        String scenarioNumber = properties.getProperty("scenarioNumber");

        //External services details
        String serviceURL = properties.getProperty("serviceURL");

        configDb(dbType, host, port, db, user, password, mode);
        configScenaraioNumber(scenarioNumber);
        configPaymentService(serviceURL, systemFacade);
    }

    private static void configPaymentService(String serviceURL, ISystemFacade systemFacade) {
        if(serviceURL != null) {
            systemFacade.updatePaymentServiceURL(serviceURL);
        }
    }

    public static void configDb(String dbType, String host, String port, String db, String user, String password, String mode) {
        if (mode != null) {
            DbConfig.config(dbType, host, port, db, user, password, mode);
        } else {
            DbConfig.config(dbType, host, port, db, user, password);
        }
    }
    public static void configScenaraioNumber(String scenarioNumber) {
        if(scenarioNumber.equals("1") || scenarioNumber.equals("2") || scenarioNumber.equals("3")) {
            CreateDataForTest.config(scenarioNumber);
        }
    }
}


