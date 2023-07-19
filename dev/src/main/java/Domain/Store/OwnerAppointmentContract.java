package Domain.Store;

import DataLayer.Store.ORM.Contract.DataAppointConsent;
import DataLayer.Store.ORM.Contract.DataAppointment;
import util.Exceptions.DataExistentError;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OwnerAppointmentContract {
    private final int storeId;
    private final String assigningOwner;
    String contract;
    String newOwner;
    private final Map<String, Boolean> storeConsent;  // <Owners name, doesConsent>

    private DataAppointment dataAppointment;

    // CTOR for data creation
    public OwnerAppointmentContract(int storeId, String assigningOwner, String newOwner, String contract, Set<String> ownersToConsent) {
        dataAppointment = new DataAppointment(storeId, assigningOwner, newOwner, contract, ownersToConsent);
        this.storeId = storeId;
        this.assigningOwner = assigningOwner;
        this.newOwner = newOwner;
        storeConsent = new ConcurrentHashMap<>();

        for (String owner : ownersToConsent)
            storeConsent.put(owner, false);
    }

    // CTOR for data recovery
    public OwnerAppointmentContract(DataAppointment dataAppointment){
        this.dataAppointment = dataAppointment;
        storeId = dataAppointment.getStore().getId();
        assigningOwner = dataAppointment.getAssigner().getUsername();
        newOwner = dataAppointment.getAssignee().getUsername();
        contract = dataAppointment.getContract();
        storeConsent = new ConcurrentHashMap<>();
        for (Map.Entry<String, DataAppointConsent> entry: dataAppointment.getConsents().entrySet())
            storeConsent.put(entry.getKey(), entry.getValue().isConsent());
    }


    public void updateContract (String newContract){
        this.contract = newContract;
        dataAppointment.setContract(newContract);
        dataAppointment = dataAppointment.persist();
        storeConsent.replaceAll((key, value) -> false);
    }

    public Boolean isStoreConsent() {
        for (Map.Entry<String, Boolean> entry : storeConsent.entrySet())
            if (!entry.getValue())
                return false;
        return true;
    }

    public void setStoreConsent(String userName, boolean isConsent) {
        dataAppointment.updateConsent(userName, isConsent);
        storeConsent.replace(userName, isConsent);
    }

    public void remove() {
        dataAppointment.remove();
    }

    public void removeFromConsent(String ownerToRemove) {
        dataAppointment.removeFromConsent(ownerToRemove);
        storeConsent.remove(ownerToRemove);
    }

    public void addToConsent(String ownerToAdd) {
        if (storeConsent.containsKey(ownerToAdd)) return; //TODO: throw exception maybe?

        dataAppointment.addToConsent(ownerToAdd);
        storeConsent.putIfAbsent(ownerToAdd, false);
    }

    public int getId() {
        return dataAppointment.getId();
    }

    public String getContract() {
        return dataAppointment.getContract();
    }

    public String getAssigningOwner() {
        return assigningOwner;
    }

    public String getNewOwner() {
        return newOwner;
    }

    public Map<String, Boolean> getStoreConsent() {
        return storeConsent;
    }

    public DataAppointment getAppointOwnerContractData() {
        return dataAppointment;
    }

    public int getStoreId() {
        return storeId;
    }

    @Override
    public String toString() {
        StringBuilder consentState = new StringBuilder("Consents=[");
        for (Map.Entry<String, Boolean> entry: storeConsent.entrySet())
            consentState.append(String.format("{%s, %s},", entry.getKey(), entry.getValue()));
        consentState.append("]");
        return
                String.format("Contract[storeID=%d, contract=%s, newOwnerUserName=%s, %s",
                        getStoreId(),
                        getContract(),
                        getNewOwner(),
                        consentState
                );
    }
}
