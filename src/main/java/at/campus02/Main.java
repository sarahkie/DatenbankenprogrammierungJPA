package at.campus02;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.SelectionQuery;

import java.util.List;

public class Main {
    private static SessionFactory sessionFactory = null;

    public static void main(String[] args) {
        // Hibernate initialisieren
        sessionFactory = new Configuration()
                .configure()
                .buildSessionFactory(); //Factory wird gebaut - lebt so lange, wie unser JavaProgramm läuft

        try {
            // Neuen User erstellen
            User user = new User();
            user.setFirstname("Johann");
            user.setLastname("Grabner");
            user.setGeschlecht("männlich");

            // User speichern
            insertUser(user);

            // Alle User anzeigen
            displayAllUsers();

            // Alle User aus der DB lesen und anzeigen
            List<User> allUsers = getAllUsers();
            for (User u : allUsers) {
                System.out.println(u.getFirstname() + " " + u.getLastname());
            }

            //Benutzer mit ID auslesen
            User foundUser = getUserById(1);

            if (foundUser != null) {
                System.out.println("Gefundener User: " + foundUser);
            } else {
                System.out.println("User nicht gefunden!");
            }
            User foundUserHQL = getUserByHQLById(1);

            if (foundUserHQL != null) {
                System.out.println("Gefundener User mit HQL: " + foundUserHQL);
            } else {
                System.out.println("User mit HQL nicht gefunden!");
            }


            List<User> weiblicherUser = getWeiblicheUser();
            System.out.println("\nweibliche User");
            for (User u : allUsers) {
                System.out.println(u.getFirstname() + " " + u.getLastname());
            }

            changeUser(1, "Johann Hans");

            updateDetachedUser(foundUserHQL);
            deleteUserById(1);


            // Neues Geschenk erstellen
            Geschenk geschenk = new Geschenk();
            geschenk.setGeschenksEmpfaenger("Jakob");
            geschenk.setGeschenksIdee("Lupin Kostüm");

            Geschenk nocheinGeschenk = new Geschenk();
            nocheinGeschenk.setGeschenksEmpfaenger("Sophie");
            nocheinGeschenk.setGeschenksIdee("Dinosaurier");
            // Buch in der Datenbank speichern
            insertGeschenk(geschenk);
            insertGeschenk(nocheinGeschenk);
            List<Geschenk> sophiesGeschenke = getSophiesGeschenke();
            for (Geschenk g : sophiesGeschenke) {
                System.out.println(g);
            }

            // Geschenk mit generierter ID ausgeben
            System.out.println(geschenk);


        } finally {
            // SessionFactory erst am Programmende schließen
            sessionFactory.close();
        }

    }

    public static void insertUser(User newUser) {
        try (Session session = sessionFactory.openSession()) {

            Transaction transaction = session.beginTransaction();
            session.persist(newUser);
            transaction.commit();
            System.out.println("User erfolgreich gespeichert!");
            System.out.println("User-ID: " + newUser.getUserId());
        } finally {

        }
    }

    public static void displayAllUsers() {
        try (Session session = sessionFactory.openSession()) {

            List<User> myUsers = session.createQuery("select u from User u", User.class).list();
            for (User user : myUsers) {
                System.out.println(user);
            }
        } finally {

        }
    }

    public static List<User> getAllUsers() {
        try (Session session = sessionFactory.openSession()) {

            return session.createQuery(
                    "select us from User us", User.class
            ).list();
        }
    }

    public static User getUserById(int id) {
        try (Session session = sessionFactory.openSession()) {
            //Suche anhand des Primärschlüssels
            return session.get(User.class, id);
        }
    }

    public static User getUserByHQLById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "select u from User u where u.userId = :id",
                            User.class
                    )
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    public static List<User> getWeiblicheUser() {

        try (Session session = sessionFactory.openSession()) {

            // 1. HQL-Abfrage erstellen
            SelectionQuery<User> query = session.createQuery(
                    "select u from User u where u.geschlecht = :geschlecht",
                    User.class
            );

            // 2. Parameter setzen
            query.setParameter("geschlecht", "weiblich");

            // 3. Abfrage ausführen und Ergebnis speichern
            List<User> weiblicheUser = query.list();

            // 4. Ergebnis zurückgeben
            return weiblicheUser;
        }
    }

    public static void saveUpdate(User changedUser) {

        try (Session session = sessionFactory.openSession()) {

            Transaction transaction = session.beginTransaction();

            // Detached-Objekt in die neue Session übernehmen
            session.merge(changedUser);

            // Änderungen in der Datenbank speichern
            transaction.commit();
        }
    }

    public static void changeUser(int userId, String newFirstname) {

        try (Session session = sessionFactory.openSession()) {

            // Transaktion starten
            Transaction transaction = session.beginTransaction();

            // Benutzer anhand des Primärschlüssels laden
            User user = session.get(User.class, userId);

            if (user != null) {

                // Vornamen ändern
                user.setFirstname(newFirstname);
                System.out.println("Dirty nach Änderung: " + session.isDirty());

                // Änderungen speichern
                transaction.commit();
                System.out.println("Dirty nach Commit: " + session.isDirty());

                System.out.println("Benutzer erfolgreich geändert!");
            } else {
                transaction.rollback();
                System.out.println("Benutzer nicht gefunden!");
            }
        }
    }

    public static User updateDetachedUser(User changedUser) {

        try (Session session = sessionFactory.openSession()) {

            Transaction transaction = session.beginTransaction();

            // Das übergebene Objekt ist nicht in dieser Session verwaltet
            System.out.println("Vor merge: " +
                    session.contains(changedUser));

            // Zustand auf eine Managed-Entity übertragen
            User managedUser = session.merge(changedUser);

            System.out.println("Original nach merge: " +
                    session.contains(changedUser));

            System.out.println("Managed User nach merge: " +
                    session.contains(managedUser));

            transaction.commit();

            return managedUser;
        }
    }

    public static void deleteUserById(int userId) {

        try (Session session = sessionFactory.openSession()) {

            Transaction transaction = session.beginTransaction();

            // Entity laden → Managed
            User user = session.get(User.class, userId);

            if (user != null) {

                System.out.println("Nach get():");
                System.out.println("Contains: " + session.contains(user));
                System.out.println("Dirty: " + session.isDirty());

                // Entity zum Löschen vormerken → Removed
                session.remove(user);

                System.out.println("Nach remove():");
                System.out.println("Contains: " + session.contains(user));
                System.out.println("Dirty: " + session.isDirty());

                // DELETE ausführen
                transaction.commit();

                System.out.println("Benutzer gelöscht!");

            } else {
                transaction.rollback();
                System.out.println("Benutzer nicht gefunden!");
            }
        }
    }


    public static void insertGeschenk(Geschenk newGeschenk) {

        try (Session session = sessionFactory.openSession()) {

            // Transaktion starten
            Transaction transaction = session.beginTransaction();

            // Buch speichern
            session.persist(newGeschenk);

            // Transaktion bestätigen
            transaction.commit();

            // Generierte ID ausgeben
            System.out.println("Geschenk erfolgreich gespeichert!");
            System.out.println("Geschenk-ID: " + newGeschenk.getGeschenksId());
        }
    }

    public static List<Geschenk> getSophiesGeschenke() {

        try (Session session = sessionFactory.openSession()) {

            // 1. HQL-Abfrage erstellen
            SelectionQuery<Geschenk> query = session.createQuery(
                    "select g from Geschenk g where g.geschenksEmpfaenger = :empf",
                    Geschenk.class
            );

            // 2. Parameter setzen
            query.setParameter("empf", "Sophie");

            // 3. Abfrage ausführen und Ergebnis speichern
            List<Geschenk> sophiesGeschenke = query.list();

            // 4. Ergebnis zurückgeben
            return sophiesGeschenke;
        }
    }

}