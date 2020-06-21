package banking;

import org.sqlite.SQLiteDataSource;

import java.util.Scanner;

public class Menu {

    private Scanner scanner;
    private CRUDRepository crudRepository;
    private NumberDaemon numberDaemon;

    public Menu(Scanner scanner, CRUDRepository crudRepository) {
        this.scanner = scanner;
        this.crudRepository = crudRepository;
        this.numberDaemon = new NumberDaemon();
    }

    public void createAccount(){
        String number = numberDaemon.generateCreditCardNumber();
        String pin = numberDaemon.generatePin();
        if(crudRepository.createAccount(number, pin) != null) {
            System.out.printf("\nYour card number has been created\nYour card number:\n%s\nYour card PIN:\n%s" +
                            "\n",
                    number, pin);
        }
    }

    public Card logIn(){
        System.out.println("\nEnter your card number:");
        String cardNumber = scanner.next().trim();
        System.out.println("Enter your PIN:");
        String pin = scanner.next().trim();
        try {
            Card card = crudRepository.getCardByNumber(cardNumber);
            if (card != null && card.getPin().equals(pin)) {
                System.out.println("\nYou have successfully logged in!");
                return card;
            } else {
                System.out.println("\nWrong card number or PIN!");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid input.");
            return null;
        }
    }

    public void showBalance(Card card) {
        System.out.println("Balance: " + card.getBalance());
    }

    public void addIncome(Card card) {
        System.out.println("Enter income:");
        int amount = scanner.nextInt();
        int newBalance = card.getBalance() + amount;
        if (crudRepository.updateBalance(card.getId(), newBalance) > -1) {
            card.setBalance(newBalance);
        }
        System.out.println("Income was added!");
    }

    public void doTransfer(Card card) {
        System.out.println("Transfer\nEnter card number:");
        String number = scanner.next().trim();
        if (number.length() < 16) {
            System.out.println("Such a card doesn't exist.");
            return;
        }
        if (card.getNumber().equals(number)) {
            System.out.println("You can't transfer money to the same account!");
            return;
        }
        if (!numberDaemon.isLuhnValid(number)) {
            System.out.println("Probably you made mistake in card number. Please try again!");
            return;
        }
        Card other = crudRepository.getCardByNumber(number);
        if (other == null) {
            System.out.println("Such a card does not exist.");
            return;
        }
        System.out.println("Enter how much money you want to transfer:\n");
        int amount = scanner.nextInt();
        if (card.getBalance() < amount) {
            System.out.println("Not enough money!");
            return;
        }
        int result = crudRepository.transferAmount(card, other, amount);
        if (result == amount) {
            System.out.println("Success!");
        } else {
            System.out.println(result);
        }
    }

    public void closeAccount(Card card) {
        crudRepository.closeAccount(card);
    }

    public void exit() {
        System.exit(0);
    }
}
