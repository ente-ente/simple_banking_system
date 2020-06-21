package banking;

import java.util.Scanner;

public class BankService {

    private Scanner scanner;
    private CardRepository cardRepository;
    private CardRules cardRules;

    static Card currentCard = null;

    static protected boolean isUserLoggedIn() {
        return currentCard != null;
    }

    private static final String MENU_LOGGED_IN = "\n1. Balance\n2. Add income\n3. Do transfer\n4. Close account\n" +
            "5. Log out\n0. Exit";

    private static final String MENU_LOGGED_OUT = "\n1. Create an account\n2. Log into account\n0. Exit";


    public BankService(Scanner scanner, CardRepository cardRepository) {
        this.scanner = scanner;
        this.cardRepository = cardRepository;
        this.cardRules = new CardRules();
    }

    public void createAccount(){
        String number = cardRules.generateCreditCardNumber();
        String pin = cardRules.generatePin();
        if(cardRepository.createAccount(number, pin) != null) {
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
            Card card = cardRepository.getCardByNumber(cardNumber);
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

    public void showBalance() {
        System.out.println("Balance: " + currentCard.getBalance());
    }

    public void addIncome() {
        System.out.println("Enter income:");
        int amount = scanner.nextInt();
        int newBalance = currentCard.getBalance() + amount;
        if (cardRepository.updateBalance(currentCard.getId(), newBalance) > -1) {
            currentCard.setBalance(newBalance);
        }
        System.out.println("Income was added!");
    }

    public void doTransfer() {
        System.out.println("Transfer\nEnter card number:");
        String number = scanner.next().trim();
        if (number.length() < 16) {
            System.out.println("Such a card doesn't exist.");
            return;
        }
        if (currentCard.getNumber().equals(number)) {
            System.out.println("You can't transfer money to the same account!");
            return;
        }
        if (!cardRules.isLuhnValid(number)) {
            System.out.println("Probably you made mistake in card number. Please try again!");
            return;
        }
        Card other = cardRepository.getCardByNumber(number);
        if (other == null) {
            System.out.println("Such a card does not exist.");
            return;
        }
        System.out.println("Enter how much money you want to transfer:\n");
        int amount = scanner.nextInt();
        if (currentCard.getBalance() < amount) {
            System.out.println("Not enough money!");
            return;
        }
        int result = cardRepository.transferAmount(currentCard, other, amount);
        if (result == amount) {
            System.out.println("Success!");
        } else {
            System.out.println(result);
        }
    }

    public void closeAccount() {
        cardRepository.closeAccount(currentCard);
        System.out.println("The account has been closed!");
    }

    public void exit() {
        System.exit(0);
    }

    public void showMenu() {
        System.out.println(isUserLoggedIn() ? MENU_LOGGED_IN : MENU_LOGGED_OUT);
    }

    public void logOut() {
        currentCard = null;
        System.out.println("\nYou have successfully logged out!");
    }
}
