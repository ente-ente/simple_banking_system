package banking;

class Card {
    private int id;
    private String number;
    private String pin;
    private int balance;

    public Card(int id, String cardNumber, String pin, int balance) {
        this.id = id;
        this.number = cardNumber;
        this.pin = pin;
        this.balance = balance;
    }

    public int getBalance() {
        return this.balance;
    }

    public String getPin() {
        return this.pin;
    }

    public int getId() {
        return this.id;
    }

    public void setBalance(int newBalance) {
        this.balance = newBalance;
    }

    public String getNumber() {
        return this.number;
    }
}
