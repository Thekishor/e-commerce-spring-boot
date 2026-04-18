package common.events.utils;
import java.security.SecureRandom;

public class TokenGenerator {

    private TokenGenerator() {
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int USER_TOKEN_LENGTH = 50;
    private static final int EVENT_TOKEN_LENGTH = 20;
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateToken() {
        StringBuilder stringBuilder = new StringBuilder(USER_TOKEN_LENGTH);
        for (int i = 0; i < USER_TOKEN_LENGTH; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static String generateEventId() {
        StringBuilder stringBuilder = new StringBuilder(EVENT_TOKEN_LENGTH);
        for (int i = 0; i < EVENT_TOKEN_LENGTH; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(index));
        }
        return stringBuilder.toString();
    }
}
