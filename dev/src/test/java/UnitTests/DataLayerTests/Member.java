package UnitTests.DataLayerTests;

import DataLayer.DbConfig;
import DataLayer.Services.NotificationService.INotificationRepo;
import DataLayer.Services.NotificationService.NotificationRepo;
import DataLayer.User.IUserRepo;
import DataLayer.User.ORM.DataMemberAddress;
import DataLayer.User.UserRepo;
import Domain.User.MemberAddress;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.NonExistentData;
import util.Records.AddressRecord;
import util.Records.NotificationRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Member extends PersistenceTest {

    private static final String SESSION_ID = "Sess1";
    private static final UserRecord USER_DATA = new UserRecord("User1", "email@email.com", "0501234567", LocalDate.of(1997, 1, 1));
    private static final String PASSWORD = "u1234567";
    private static final AddressRecord PRIMARY_ADDRESS = new AddressRecord("full name", "street 1", "city", "country", "zip", "phone-number");
    private static final AddressRecord SECONDARY_ADDRESS = new AddressRecord("full name2", "street 12", "city2", "country2", "zip2", "phone-number2");
    private static final NotificationRecord NOTIFICATION1 = new NotificationRecord("user1", USER_DATA.username(), LocalDateTime.now(), "msg1");
    private static final NotificationRecord NOTIFICATION2 = new NotificationRecord("user2", USER_DATA.username(), LocalDateTime.now().minusDays(1), "msg2");
    private static IUserRepo USER_REPO;
    private static INotificationRepo NOTIFICATION_REPO;

    @BeforeEach
    void initRepo() {
        USER_REPO = new UserRepo();
        NOTIFICATION_REPO = new NotificationRepo();
    }

    @Test
    void Persist_Member() {
        try {
            // Normal behaviour
            assertTrue(USER_REPO.getAllMembers().isEmpty(), "At the beginning the repo should be empty");
            USER_REPO.addMember(SESSION_ID, USER_DATA, PASSWORD);

            // Check existence of data
            assertEquals(1, USER_REPO.getAllMembers().size(), "1 user added - 1 should exist");
            Domain.User.Member user = USER_REPO.getMember(USER_DATA.username());
            assertEquals(USER_DATA.username(), user.getUserName());
            assertEquals(USER_DATA.email(), user.getEmail());
            assertEquals(USER_DATA.phoneNumber(), user.getPhoneNumber());
            assertEquals(USER_DATA.dateOfBirth(), user.getBirthday());
            user.login(SESSION_ID, PASSWORD);

            // Check existence of data after reopen
            closeReopen();

            assertEquals(1, USER_REPO.getAllMembers().size(), "1 user added - 1 should exist");
            user = USER_REPO.getMember(USER_DATA.username());
            assertEquals(USER_DATA.username(), user.getUserName());
            assertEquals(USER_DATA.email(), user.getEmail());
            assertEquals(USER_DATA.phoneNumber(), user.getPhoneNumber());
            assertEquals(USER_DATA.dateOfBirth(), user.getBirthday());
            assertFalse(user.isLoggedIn());
            user.login(SESSION_ID, PASSWORD);

            // Check removal
            USER_REPO.removeMember(USER_DATA.username());

            closeReopen();

            assertFalse(USER_REPO.isMemberExists(USER_DATA.username()));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void Persist_MemberAddress() {
        try {
            // Normal behaviour
            USER_REPO.addMember(SESSION_ID, USER_DATA, PASSWORD);
            Domain.User.Member member = USER_REPO.getMember(USER_DATA.username());
            assertTrue(member.getAllMemberAddresses().isEmpty(), "Newly created member has no addresses");
            int primaryID = member.addAddress(PRIMARY_ADDRESS);
            int secondaryID = member.addAddress(SECONDARY_ADDRESS);

            // Check existence of data
            assertEquals(2, member.getAllMemberAddresses().size(), "2 addresses added - 2 should exist");
            /// check primary
            MemberAddress primary = member.getAddress(primaryID);
            assertEquals(PRIMARY_ADDRESS, new AddressRecord(primary));
            assertTrue(primary.getPrimary());
            /// check secondary
            MemberAddress secondary = member.getAddress(secondaryID);
            assertEquals(SECONDARY_ADDRESS, new AddressRecord(secondary));
            assertFalse(secondary.getPrimary());

            // Check existence of data after reopen
            closeReopen();

            member = USER_REPO.getMember(USER_DATA.username());
            assertEquals(2, member.getAllMemberAddresses().size(), "2 addresses added - 2 should exist");
            /// check primary
            primary = member.getAddress(primaryID);
            assertEquals(PRIMARY_ADDRESS, new AddressRecord(primary));
            assertTrue(primary.getPrimary());
            /// check secondary
            secondary = member.getAddress(secondaryID);
            assertEquals(SECONDARY_ADDRESS, new AddressRecord(secondary));
            assertFalse(secondary.getPrimary());

            // Check removal
            member.removeAddress(primaryID);

            closeReopen();

            member = USER_REPO.getMember(USER_DATA.username());
            assertEquals(1, member.getAllMemberAddresses().size());
            Domain.User.Member finalMember = member;
            assertThrows(NonExistentData.class, () -> finalMember.getAddress(primaryID));
            assertDoesNotThrow(() -> finalMember.getAddress(secondaryID));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void Cascade_MemberAddress(){
        try {
            // Preparation
            USER_REPO.addMember(SESSION_ID, USER_DATA, PASSWORD);
            Domain.User.Member member = USER_REPO.getMember(USER_DATA.username());
            member.addAddress(PRIMARY_ADDRESS);
            member.addAddress(SECONDARY_ADDRESS);

            // Test
            closeReopen();

            USER_REPO.removeMember(USER_DATA.username());

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataMemberAddress> query = session.createQuery("FROM DataMemberAddress a WHERE a.key.member.username = :username", DataMemberAddress.class);
                query.setParameter("username", USER_DATA.username());
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void Persist_Notification(){
        try {
            //Data
            List<NotificationRecord> notifications =
                    List.of(NOTIFICATION1, NOTIFICATION2).stream()
                            .sorted(NotificationRecord::compareTo)
                            .toList();

            // Preparation
            USER_REPO.addMember(SESSION_ID, USER_DATA, PASSWORD);

            // Normal behaviour
            assertTrue(NOTIFICATION_REPO.getNotificationsFor(USER_DATA.username()).isEmpty(), "At the beginning the repo should be empty");
            NOTIFICATION_REPO.add(new LinkedList<>(notifications));

            // Check existence of Data
            assertEquals(notifications, NOTIFICATION_REPO.getNotificationsFor(USER_DATA.username()).stream().toList());

            // Check existence of data after reopen
            NOTIFICATION_REPO.add(new LinkedList<>(notifications));
            closeReopen();

            // Check existence of Data
            assertEquals(notifications, NOTIFICATION_REPO.getNotificationsFor(USER_DATA.username()).stream().toList());

            // Check removal
            closeReopen();
            assertTrue(NOTIFICATION_REPO.getNotificationsFor(USER_DATA.username()).isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void Cascade_Notification(){
        try {
            //Data
            List<NotificationRecord> notifications =
                    List.of(NOTIFICATION1, NOTIFICATION2).stream()
                            .sorted(NotificationRecord::compareTo)
                            .toList();

            // Preparation
            USER_REPO.addMember(SESSION_ID, USER_DATA, PASSWORD);
            NOTIFICATION_REPO.add(new LinkedList<>(notifications));

            // Test cascade
            closeReopen();
            USER_REPO.removeMember(USER_DATA.username());

            assertTrue(NOTIFICATION_REPO.getNotificationsFor(USER_DATA.username()).isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }
}
