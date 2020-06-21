package banking;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.sql.*;

public class CardRepository {
    private SQLiteDataSource dataSource;
    public CardRepository(String fileName) {
        String createTable = "CREATE TABLE IF NOT EXISTS card (\n"
                + "	id INTEGER NOT NULL PRIMARY KEY,\n"
                + "	number TEXT UNIQUE,\n"
                + "	pin TEXT,\n"
                + "	balance INTEGER DEFAULT 0\n"
                + ");";
        this.dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + fileName);
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int transferAmount(Card sender, Card receiver, int amount) {
        String sqlSendMoney = "UPDATE card SET balance=balance-? WHERE id=?";
        String sqlReceiveMoney = "UPDATE card SET balance=balance+? WHERE id=?";
        int sentMoney = 0;

        try (Connection con = dataSource.getConnection();
             PreparedStatement preparedStatement1 = con.prepareStatement(sqlSendMoney);
             PreparedStatement preparedStatement2 = con.prepareStatement(sqlReceiveMoney)) {
            con.setAutoCommit(false);
            preparedStatement1.setInt(1, amount);
            preparedStatement1.setInt(2, sender.getId());
            preparedStatement2.setInt(1, amount);
            preparedStatement2.setInt(2, receiver.getId());
            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();
            try {
                con.commit();
            } catch (SQLException e1) {
                con.rollback();
                e1.printStackTrace();
            }
            con.setAutoCommit(true); // in finally block
        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        return sentMoney;
    }

    public Card getCardByNumber(String cardNumber) {
        String sql = "SELECT id, pin, balance "
                + "FROM card WHERE number = ?";
        Card card = null;
        try (Connection conn = this.dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                card = new Card(rs.getInt("id"), cardNumber, rs.getString("pin"), rs.getInt("balance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return card;
    }

    public String createAccount(String creditCardNumber, String pin) {
        String sql = "INSERT INTO card(number, pin, balance) VALUES(?, ?, ?)";
        try (Connection conn = this.dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, creditCardNumber);
            preparedStatement.setString(2, pin);
            preparedStatement.setInt(3, 0);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return creditCardNumber;
    }

    public int updateBalance(int id, int newBalance) {
        String sql = "UPDATE card SET balance=balance+? WHERE id=?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, newBalance);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return newBalance;
    }

    public void closeAccount(Card card) {
        String sql = "DELETE FROM card WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, card.getId());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
