package banking;

import org.sqlite.SQLiteDataSource;
import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Random;
import java.util.Scanner;

class CreditCard {
    private final int id;
    private final String number;
    private final String pin;
    private int balance;

    public CreditCard(DataSource dataSource) {
        this.number = generateNewCardNumber(dataSource);
        this.pin = createPin();
        this.balance = 0;
        this.id = saveToDatabase(dataSource);
        if (this.id > -1) {
            System.out.printf("\nYour card number has been created\nYour card number:\n%s\nYour card PIN:\n%s" +
                            "\n",
                    this.number, this.pin);
        } else {
            System.out.println("\nYour card could not be saved. Please try again.");
        }
    }

    private CreditCard(int id, String number, String pin, int balance) {
        this.number = number;
        this.id = id;
        this.pin = pin;
        this.balance = balance;
    }

    private String generateNewCardNumber(DataSource dataSource) {
        Random random = new Random(Instant.now().toEpochMilli());
        long leftLimit = 100_000_000L;
        long rightLimit = 1_000_000_000L;
        long accountNumber = leftLimit + (long) (random.nextDouble() * (rightLimit - leftLimit));
        long base = 4_000_000_000_000_000L + accountNumber * 10;
        String work = Long.toString(base);
        int checkSum = 0;
        for (int i = 0; i < 15; i++) {
            int digit = Character.getNumericValue(work.charAt(i));
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            checkSum += digit;
        }
        int lastDigit = 10 - (checkSum % 10);
        String temp = Long.toString(base + lastDigit);
        if (getCardByNumber(temp, dataSource) != null) {
            System.out.println("There is a card with number " + temp + " already in the database.");
            generateNewCardNumber(dataSource);
        }
        return temp;
    }

    public static CreditCard getCardByNumber(String number, DataSource dataSource) {
        String sql = "SELECT id, pin, balance "
                + "FROM card WHERE number = " + number;
        CreditCard card = null;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    card = new CreditCard(rs.getInt("id"), number, rs.getString("pin"), rs.getInt("balance"));
                }
            } catch (SQLException e) {
            e.printStackTrace();
        }
        return card;
    }

    public boolean isPin(String pin) {
        return pin.equals(this.pin);
    }

    public double getBalance() {
        return this.balance;
    }

    public int saveToDatabase(DataSource dataSource) {
        String sql = "INSERT INTO card(number, pin, balance) VALUES(?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, this.number);
            preparedStatement.setString(2, this.pin);
            preparedStatement.setInt(3, this.balance);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        CreditCard card = getCardByNumber(this.number, dataSource);
        if (card != null) {
            return card.id;
        } else {
            return -1;
        }
    }

    private String createPin() {
        Random random = new Random(Instant.now().toEpochMilli());
        int leftLimit = 1_000;
        int rightLimit = 10_000;
        return Integer.toString(leftLimit + (int) (random.nextDouble() * (rightLimit - leftLimit)));
    }

    public void addIncome(int amount, SQLiteDataSource dataSource) {
    }

    public CreditCard announceTransfer(String number, SQLiteDataSource dataSource) {
        if (this.number.equals(number)) {
            System.out.println("You can't transfer money to the same account!");
            return null;
        }
        if (!isValidCardNumber(number)) {
            System.out.println("Probably you made a mistake in card number. Please try again!");
            return null;
        }
        CreditCard other = getCardByNumber(number, dataSource);
        if (other == null) {
            System.out.println("Such a card does not exist.");
            return null;
        }
        return other;
    }

    public void closeAccount(SQLiteDataSource dataSource) {
    }

    private boolean isValidCardNumber(String number) {
        return true;
    }
}

public class Main {
    static CreditCard currentCard = null;
    static protected boolean isLoggedIn() {
        return currentCard != null;
    }

    public static void main(String[] args) {
        String fileName = args[1];
        Scanner scanner = new Scanner(System.in);
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + fileName);
        String createTable = "CREATE TABLE IF NOT EXISTS card (\n"
                + "	id INTEGER NOT NULL PRIMARY KEY,\n"
                + "	number TEXT,\n"
                + "	pin TEXT,\n"
                + "	balance INTEGER DEFAULT 0\n"
                + ");";

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(createTable);
            System.out.println("Table created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        while (true) {
            String menu = isLoggedIn() ? "\n1. Balance\n2. Add income\n3. Do transfer\n4. Close account\n" +
                    "5. Log out\n0. Exit" :
            "\n1. Create an account\n2. Log into account\n0. Exit";

            System.out.println(menu);
            switch (scanner.next().trim()) {
                case "1":
                    if (isLoggedIn()) {
                        System.out.println("\nBalance: " + currentCard.getBalance());
                    } else {
                        new CreditCard(dataSource);
                    }
                    break;
                case "2":
                    if (isLoggedIn()) {
                        System.out.println("Enter income:");
                        int amount = scanner.nextInt();
                        currentCard.addIncome(amount, dataSource);
                        System.out.println("Income was added!");
                    } else {
                        login(scanner, dataSource);
                    }
                    break;
                case "3":
                    System.out.println("Transfer\nEnter card number:");
                    String number = scanner.next().trim();
                    CreditCard other = currentCard.announceTransfer(number, dataSource);
                    if (other != null) {
                        System.out.println("Enter how much money you want to transfer:");
                        int amount = scanner.nextInt();
                        if (currentCard.getBalance() >= amount) {
                            other.addIncome(amount, dataSource);
                            System.out.println("Success!");
                        } else {
                            System.out.println("Not enough money!");
                        }
                    }
                    break;
                case "4":
                    currentCard.closeAccount(dataSource);
                    System.out.println("The account has been closed!");
                    break;
                case "5":
                    logout();
                    break;
                case "0":
                    System.out.println("\nBye!");
                    return;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    private static void logout() {
        currentCard = null;
        System.out.println("\nYou have successfully logged out!");
    }

    private static void login(Scanner scanner, DataSource dataSource) {
        System.out.println("\nEnter your card number:");
        String cardNumber = scanner.next().trim();
        System.out.println("Enter your PIN:");
        String pin = scanner.next().trim();
        try {
                CreditCard card = CreditCard.getCardByNumber(cardNumber, dataSource);
                if (card != null && card.isPin(pin)) {
                    currentCard = card;
                    System.out.println("\nYou have successfully logged in!");
                } else {
                System.out.println("\nWrong card number or PIN!");
            }
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid input.");
        }
    }
}
