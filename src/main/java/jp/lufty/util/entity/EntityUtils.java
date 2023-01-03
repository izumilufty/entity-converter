package jp.lufty.util.entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityUtils {

    public static final EntityMapper defaultFactory = new EntityMapper.Builder().build();

    public static final String snakeToCamel(String snake) {

        int pos = snake.indexOf('_');
        if (pos < 0) {
            return snake;
        } else if (snake.length() == pos + 1) {
            return snake.substring(0, pos);
        } else {
            char c = snake.charAt(pos + 1);
            if (isLowerAlpha(c)) {
                c = (char) (c - 0x20);
            }
            snake = snake.substring(0, pos) + c + snake.substring(pos + 2);
            return snakeToCamel(snake);
        }
    }

    public static final String camelToSnake(String camel) {

        StringBuilder snake = new StringBuilder();

        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (isUpperAlpha(c)) {
                snake.append("_").append(Character.valueOf((char) (c + 0x20)));
            } else {
                snake.append(c);
            }
        }

        return snake.toString();
    }

    private static final boolean isUpperAlpha(char c) {
        return c > 0x40 && c < 0x5b;
    }

    private static final boolean isLowerAlpha(char c) {
        return c > 0x60 && c < 0x7b;
    }
}
