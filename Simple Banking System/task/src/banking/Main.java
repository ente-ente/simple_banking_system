package banking;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String fileName = args[1];
        CardRepository cardRepository = new CardRepository(fileName);
        Scanner scanner = new Scanner(System.in);
        BankService bankManager = new BankService(scanner, cardRepository);

        while (true) {
            bankManager.showMenu();
            switch (scanner.next().trim()) {
                case "1":
                    if (bankManager.isUserLoggedIn()) {
                        bankManager.showBalance();
                    } else {
                        bankManager.createAccount();
                    }
                    break;
                case "2":
                    if (bankManager.isUserLoggedIn()) {
                        bankManager.addIncome();
                    } else {
                        bankManager.logIn();
                    }
                    break;
                case "3":
                    bankManager.doTransfer();
                    break;
                case "4":
                    bankManager.closeAccount();
                    break;
                case "5":
                    bankManager.logOut();
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
