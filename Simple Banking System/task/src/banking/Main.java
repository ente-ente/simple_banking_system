package banking;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static Card currentCard = null;

    static protected boolean isLoggedIn() {
        return currentCard != null;
    }

    private static final String MENU_LOGGED_IN = "\n1. Balance\n2. Add income\n3. Do transfer\n4. Close account\n" +
            "5. Log out\n0. Exit";

    private static final String MENU_LOGGED_OUT = "\n1. Create an account\n2. Log into account\n0. Exit";

    public static void main(String[] args) {
        String fileName = args[1];
        CRUDRepository crudRepository = new CRUDRepository(fileName);
        Scanner scanner = new Scanner(System.in);
        Menu menu = new Menu(scanner, crudRepository);

        while (true) {
            System.out.println(isLoggedIn() ? MENU_LOGGED_IN : MENU_LOGGED_OUT);
            switch (scanner.next().trim()) {
                case "1":
                    if (isLoggedIn()) {
                        menu.showBalance(currentCard);
                    } else {
                        menu.createAccount();
                    }
                    break;
                case "2":
                    if (isLoggedIn()) {
                        menu.addIncome(currentCard);
                    } else {
                        currentCard = menu.logIn();
                    }
                    break;
                case "3":
                    menu.doTransfer(currentCard);
                    break;
                case "4":
                    menu.closeAccount(currentCard);
                    System.out.println("The account has been closed!");
                    break;
                case "5":
                    currentCard = null;
                    System.out.println("\nYou have successfully logged out!");
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
}
