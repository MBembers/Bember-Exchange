package com.example.bemberexchange;

public class Helpers {
    public static String countryCodeToEmoji(String code) {

        // offset between uppercase ascii and regional indicator symbols
        int OFFSET = 127397;

        code = code.substring(0, 2);

        // convert code to uppercase
        code = code.toUpperCase();

        StringBuilder emojiStr = new StringBuilder();

        //loop all characters
        for (int i = 0; i < code.length(); i++) {
            emojiStr.appendCodePoint(code.charAt(i) + OFFSET);
        }

        // return emoji
        return emojiStr.toString();
    }
}
