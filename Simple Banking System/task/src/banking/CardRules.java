package banking;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Random;

public class CardRules {
    public String generateCreditCardNumber() {
        Random random = new Random(Instant.now().toEpochMilli());
        long leftLimit = 100_000_000L;
        long rightLimit = 1_000_000_000L;
        long accountNumber = leftLimit + (long) (random.nextDouble() * (rightLimit - leftLimit));
        long base = 4_000_000_000_000_000L + accountNumber * 10;
        int lastDigit = 10 - (calculateChecksum(Long.toString(base)) % 10);
        return Long.toString(base + lastDigit);
    }

    public boolean isLuhnValid(String number) {
        return (calculateChecksum(number) + Character.getNumericValue(number.charAt(15))) % 10 == 0;
    }

    private int calculateChecksum(String number) {
        int checkSum = 0;
        for (int i = 0; i < 15; i++) {
            int digit = Character.getNumericValue(number.charAt(i));
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            checkSum += digit;
        }
        return checkSum;
    }

    public String generatePin() {
        Random random = new Random(Instant.now().toEpochMilli());
        int leftLimit = 1_000;
        int rightLimit = 10_000;
        return Integer.toString(leftLimit + (int) (random.nextDouble() * (rightLimit - leftLimit)));
    }
}
