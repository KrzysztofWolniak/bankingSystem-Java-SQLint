package banking;


import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:" + args[1];
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        CardLogic.createDatabase(dataSource);
        int id = 0;
        Scanner scanner = new Scanner(System.in);

        boolean powerOn = true;
        boolean isLogged = false;
        String choice;
        while (powerOn) {
            CardLogic.printMenu(isLogged);
            choice = scanner.next();
            if (!isLogged) {
                switch (choice) {
                    case "1" -> CardLogic.addCardToDatabase(dataSource);
                    case "2" -> {
                        id = CardLogic.isValid(dataSource, scanner);
                        isLogged = id > 0;
                    }
                    case "0" -> {
                        powerOn = false;
                        System.out.println("Bye!");
                    }

                    default -> System.out.println("Wrong choice!");
                }
            } else {
                switch (choice) {
                    case "1" -> System.out.println("Balance: " + CardLogic.currentBalance(dataSource, id));
                    case "2" -> CardLogic.addIncome(dataSource, id, scanner);
                    case "3" -> CardLogic.doTransfer(dataSource, id, scanner);
                    case "4" -> {
                        CardLogic.deleteAccount(dataSource, id);
                        isLogged = false;
                    }
                    case "5" -> {
                        //LOG OUT
                        System.out.println("You have successfully logged out!");
                        isLogged = false;
                    }
                    case "0" -> {
                        //TURN OFF
                        powerOn = false;
                        System.out.println("Bye!");
                    }
                    default -> System.out.println("Wrong choice!");
                }
            }
        }
    }



}