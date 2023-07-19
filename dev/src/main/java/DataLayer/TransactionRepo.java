package DataLayer;

import DataLayer.ORM.DataTransaction;
import org.hibernate.Session;
import util.Records.Transaction;
import Domain.User.IStoreBasket;
import util.Enums.ErrorStatus;
import util.Exceptions.NonExistentData;
import util.Records.DateTimeRange;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransactionRepo implements ITransactionRepo {
    private final Map<Integer, DataTransaction> transactions;

    public TransactionRepo() {
        this.transactions = new ConcurrentHashMap<>();
    }

    @Override
    public int addTransaction(IStoreBasket basket, String userName, double price) {
        DataTransaction transaction = new DataTransaction(basket, userName, price);
        transactions.put(transaction.getId(), transaction);
        return transaction.getId();
    }

    @Override
    public Map<Integer, Transaction> getTransactions() {
        pullDataIfAbsent();

        return transactions.values().stream()
                .map(DataTransaction::getAsTransaction)
                .collect(Collectors.toMap(Transaction::id, Function.identity()));
    }

    @Override
    public List<Transaction> getTransactionsInRange(DateTimeRange range) {
        pullDataIfAbsent();

        return transactions.values().stream()
                .map(DataTransaction::getAsTransaction)
                .filter(t -> range.isInRange(t.timeStamp()))
                .toList();
    }

    @Override
    public Transaction getTransaction(int id) throws NonExistentData {
        pullDataIfAbsent();

        if (!transactions.containsKey(id))
            throw new NonExistentData(
                    String.format("Transaction with ID '%d' doesn't exist.", id),
                    ErrorStatus.TRANSACTION_DOES_NOT_EXIST
            );
        return transactions.get(id).getAsTransaction();
    }

    @Override
    public List<Transaction> getUserTransactions(String userName) {
        return transactions.values().stream()
                .map(DataTransaction::getAsTransaction)
                .filter(t -> t.userName().equals(userName))
                .toList();
    }

    @Override
    public List<Transaction> getUserTransactionsInRange(String userName, DateTimeRange range) {
        return getUserTransactions(userName).stream()
                .filter(t -> range.isInRange(t.timeStamp()))
                .toList();
    }

    @Override
    public List<Transaction> getStoreTransactions(int storeId) {
        return transactions.values().stream()
                .map(DataTransaction::getAsTransaction)
                .filter(t -> t.storeId() == storeId)
                .toList();
    }

    @Override
    public List<Transaction> getStoreTransactionsInRange(int storeId, DateTimeRange range) {
        return getStoreTransactions(storeId).stream()
                .filter(t -> range.isInRange(t.timeStamp()))
                .toList();
    }

    @Override
    public void clean() {
        if (!DbConfig.shouldPersist()) {
            transactions.clear();
            return;
        }

        try (Session session = DbConfig.getSessionFactory().openSession()) {
            org.hibernate.Transaction transaction = session.beginTransaction();

            // Clear the table
            session.createNativeQuery("TRUNCATE Transaction CASCADE").executeUpdate();

            transaction.commit();
        }
        transactions.clear();
    }

    @Override
    public int getTransactionCount() {
        return transactions.size();
    }

    private void pullDataIfAbsent() {
        if (DbConfig.shouldPersist() && transactions.isEmpty())
            try (Session session = DbConfig.getSessionFactory().openSession()) {
                for (DataTransaction transaction : session.createQuery("FROM DataTransaction", DataTransaction.class).list())
                    transactions.putIfAbsent(transaction.getId(), transaction);
            }
    }
}