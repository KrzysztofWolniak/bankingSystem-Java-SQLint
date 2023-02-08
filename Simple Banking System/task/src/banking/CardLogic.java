package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Scanner;

public class CardLogic {
    public static int isValid(SQLiteDataSource dataSource, Scanner scanner) {
        System.out.println("Enter your card number:");
        String cardNumber = scanner.next();
        System.out.println("Enter your PIN:");
        String pinNumber = scanner.next();
        String receivedCardNumber = "";
        String receivedPinNumber = "";
        int id = -1;
        try (Connection connection = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = connection.createStatement()) {
                String sql = String.format("SELECT * FROM card where number='%s';", cardNumber);
                try (ResultSet card = statement.executeQuery(sql)) {
                    if (card.next()) {
                        receivedCardNumber = (card.getString("number"));
                        receivedPinNumber = (card.getString("pin"));
                        id = card.getInt("id");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (cardNumber.equals(receivedCardNumber) && pinNumber.equals(receivedPinNumber)) {
            System.out.println("You have successfully logged in!");
            return id;
        }

        System.out.println("Wrong card number or PIN!");
        return -1;


    }

    public static void addCardToDatabase(SQLiteDataSource dataSource) {
        String cardNumber = createCardNumber();
        String pinNumber = createPinNumber();
        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                String sql = String.format("INSERT INTO card(number,pin) VALUES('%s','%s')", cardNumber, pinNumber);
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void printMenu(boolean isLogged) {
        if (isLogged) {
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");
        } else {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
        }

    }

    private static String createCardNumber() {
        Random random = new Random();
        int sum = 0;
        StringBuilder cardNumber = new StringBuilder("400000");
        for (int i = 0; i < 9; i++) {
            cardNumber.append(random.nextInt(10));
        }
        int[] card = new int[16];
        for (int i = 0; i < cardNumber.length(); i++) {
            card[i] = Integer.parseInt(String.valueOf(cardNumber.charAt(i)));
            if (i % 2 == 0) card[i] *= 2;
            if (card[i] > 9) card[i] -= 9;
            sum += card[i];
        }

        if (sum % 10 != 0) card[15] = (10 - (sum % 10)) % 10;
        else card[15] = 0;

        cardNumber.append(card[cardNumber.length()]);
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        return cardNumber.toString();
    }

    private static String createPinNumber() {
        Random random = new Random();
        StringBuilder pinNumber = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            pinNumber.append(random.nextInt(9));
        }
        System.out.println("Your card PIN:");
        System.out.println(pinNumber);
        return pinNumber.toString();
    }

    public static int currentBalance(SQLiteDataSource dataSource, int id) {
        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                String sql = String.format("SELECT * FROM card WHERE id='%d'", id);
                try (ResultSet card = statement.executeQuery(sql)) {
                    if (card.next()) {
                        return card.getInt("balance");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void createDatabase(SQLiteDataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INTEGER DEFAULT 0)");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addIncome(SQLiteDataSource dataSource, int id, Scanner scanner) {
        System.out.println("Enter income:");
        String income = scanner.next();
        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                String sql = String.format("UPDATE card SET balance = balance + %d WHERE id='%d'", Integer.parseInt(income), id);
                statement.executeUpdate(sql);
                System.out.println("Income was added!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void doTransfer(SQLiteDataSource dataSource, int id, Scanner scanner) {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String cardToTransfer = scanner.next();
        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                String sql = String.format("SELECT * FROM card WHERE number='%s'", cardToTransfer);
                try (ResultSet card = statement.executeQuery(sql)) {
                    if (card.next()) {
                        if (id == card.getInt("id")) {
                            System.out.println("You can't transfer money to the same account!");
                        } else {
                            System.out.println("Enter how much money you want to transfer:");
                            String balanceToTransfer = scanner.next();
                            if (Integer.parseInt(balanceToTransfer) > currentBalance(dataSource, id)) {
                                System.out.println("Not enough money!");
                            } else {
                                con.setAutoCommit(false);
                                String transferSQL = String.format("UPDATE card SET balance = balance + %d WHERE number = %s", Integer.parseInt(balanceToTransfer), cardToTransfer);
                                statement.executeUpdate(transferSQL);
                                transferSQL = String.format("UPDATE card SET balance = balance - %d WHERE id = %d", Integer.parseInt(balanceToTransfer), id);
                                statement.executeUpdate(transferSQL);
                                con.commit();
                                System.out.println("Success!");
                            }
                        }
                    } else {
                        if (checkLuhnAlgorithm(cardToTransfer)) {
                            System.out.println("Such a card does not exist.");

                        } else {
                            System.out.println("Probably you made a mistake in the card number. Please try again!");

                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void deleteAccount(SQLiteDataSource dataSource, int id) {
        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                String sql = String.format("DELETE FROM card WHERE id='%d'", id);
                statement.executeUpdate(sql);
                System.out.println("The account has been closed!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkLuhnAlgorithm(String string) {
        int currentDigit = 0;
        int sum = 0;
        for (int i = 0; i < string.length() - 1; i++) {
            currentDigit = Integer.parseInt(String.valueOf(string.charAt(i)));
            if (i % 2 == 0) {
                currentDigit *= 2;
            }
            if (currentDigit > 9) {
                currentDigit -= 9;
            }
            sum += currentDigit;
        }
        sum += Integer.parseInt(String.valueOf(string.charAt(string.length() - 1)));
        if (sum % 10 == 0) return true;
        return false;
    }
}
