package util;

import Domain.MarketLogger;
import Domain.Store.Category;
import Service.ISystemFacade;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Service.Result;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class CreateDataForTest {
    private static String sessId = new Random().ints(256, 0, 62).mapToObj(i -> "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(i)).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    private static String scenarioNumber;
    public static void config(String number) {
        scenarioNumber = number;
    }
    public static void createData(ISystemFacade facade) {
        if(scenarioNumber == null) {
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        try {
            List<JsonNode> commandList = objectMapper.readValue(new File("src/main/resources/scenario" + scenarioNumber + ".json"), new TypeReference<List<JsonNode>>() {
            });
            facade.connect(sessId);
            // Execute each command
            for (JsonNode commandNode : commandList) {
                String command = commandNode.get("command").asText();
                JsonNode paramsNode = commandNode.get("params");

                try {
                    switch (command) {
                        case "Register":
                            executeRegisterCommand(paramsNode, facade);
                            break;
                        case "Login":
                            executeLoginCommand(paramsNode, facade);
                            break;
                        case "OpenStore":
                            executeOpenStoreCommand(paramsNode, facade);
                            break;
                        case "AppointOwner":
                            executeAppointOwnerCommand(paramsNode, facade);
                            break;
                        case "Logout":
                            executeLogoutCommand(facade);
                            break;
                        case "CreateSystemManager":
                            executeCreateSystemManagerCommand(paramsNode, facade);
                            break;
                        case "AppointManager":
                            executeAppointManagerCommand(paramsNode, facade);
                            break;
                        case "AddNewProduct":
                            executeAddNewProductCommand(paramsNode, facade);
                            break;
                        case "ModifyPermission":
                            executeModifyPermissionCommand(paramsNode, facade);
                            break;
                        default:
                            MarketLogger.logError("CreateDataForTest", "createData", "Couldn't find the command: ", command);
                    }
                } catch (RuntimeException e) {
                    MarketLogger.logError("CreateDataForTest", "createData", e.getMessage());
                }
            }
        } catch (IOException e) {
            MarketLogger.logError("CreateDataForTest", "createData", e.getMessage());
        }
    }


    private static void executeModifyPermissionCommand(JsonNode paramsNode, ISystemFacade facade) {
        int storeId = paramsNode.get("storeId").asInt();
        String username = paramsNode.get("username").asText();
        int permission = paramsNode.get("permission").asInt();
        Result modifyPermissionResult = facade.modifyPermissionsFor(sessId, username, storeId, permission);
        if (modifyPermissionResult.errorOccured()) {
            throw new RuntimeException("Command execution failed: Modify permission");
        }
    }

    private static void executeAddNewProductCommand(JsonNode paramsNode, ISystemFacade facade) {
        int storeId = paramsNode.get("storeId").asInt();
        String category = paramsNode.get("category").asText();
        String name = paramsNode.get("name").asText();
        double cost = paramsNode.get("cost").asDouble();
        int quantity = paramsNode.get("quantity").asInt();
        Result addNewProductResult = facade.addNewProduct(sessId, new ProductRecord(storeId, name, cost, Category.valueOf(category), quantity));
        if (addNewProductResult.errorOccured()) {
            throw new RuntimeException("Command execution failed: Add new product");
        }
    }

    private static void executeCreateSystemManagerCommand(JsonNode paramsNode, ISystemFacade facade) {
        String username = paramsNode.get("username").asText();
        String password = paramsNode.get("password").asText();
        Result createSystemManagerResult = facade.createSystemManager(sessId, username, password);
        if (createSystemManagerResult.errorOccured()) {
            throw new RuntimeException("Command execution failed: Create System Manager");
        }
    }

    public static void executeRegisterCommand(JsonNode paramsNode, ISystemFacade facade) {
        String username = paramsNode.get("username").asText();
        String email = paramsNode.get("email").asText();
        String phone = paramsNode.get("phone").asText();
        String password = paramsNode.get("password").asText();
        LocalDate currentDate = LocalDate.now();

        Result registerResult = facade.register(sessId, new UserRecord(username, email, phone, currentDate), password);
        if (registerResult.errorOccured()) {
            throw new RuntimeException("Command execution failed: Register");
        }
    }

    public static void executeLoginCommand(JsonNode paramsNode, ISystemFacade facade) {
        String username = paramsNode.get("username").asText();
        String password = paramsNode.get("password").asText();

        Result loginResult = facade.login(sessId, username, password);
        if (loginResult.errorOccured()) {
            throw new RuntimeException("Command execution failed: Login");
        }
    }

    public static void executeOpenStoreCommand(JsonNode paramsNode, ISystemFacade facade) {
        String storeName = paramsNode.get("storeName").asText();
        String storeDescription = paramsNode.get("storeDescription").asText();

        Result openStoreResult = facade.openStore(sessId, new StoreRecord(storeName, storeDescription));
        if (openStoreResult.errorOccured()) {
            throw new RuntimeException("Command execution failed: OpenStore");
        }
    }

    public static void executeAppointOwnerCommand(JsonNode paramsNode, ISystemFacade facade) {
        String username = paramsNode.get("username").asText();
        int storeId = paramsNode.get("storeId").asInt();

        Result appointOwnerResult = facade.appointOwner(sessId, username, storeId);
        if (appointOwnerResult.errorOccured()) {
            throw new RuntimeException("Command execution failed: AppointOwner");
        }
    }

    public static void executeAppointManagerCommand(JsonNode paramsNode, ISystemFacade facade) {
        String username = paramsNode.get("username").asText();
        int storeId = paramsNode.get("storeId").asInt();

        Result appointOwnerResult = facade.appointManager(sessId, username, storeId);
        if (appointOwnerResult.errorOccured()) {
            throw new RuntimeException("Command execution failed: AppointManager");
        }
    }

    public static void executeLogoutCommand(ISystemFacade facade) {
        Result logoutResult = facade.logout(sessId);
        if (logoutResult.errorOccured()) {
            throw new RuntimeException("Command execution failed: Logout");
        }
    }
}