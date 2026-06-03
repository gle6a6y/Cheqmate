package project.game;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;

public class StartWindow {

    private enum JoinResult {
        OK,
        SESSION_NOT_FOUND,
        PLAYER_NOT_EXPECTED,
        PLAYER_ALREADY_IN_SESSION,
        CONNECTION_ERROR
    }

    private final int SESSION_MAX_LEN = 6;
    private final int NICKNAME_MAX_LEN = 16;
    private final int SESSION_INNER_WIDTH = 10;
    private final int NICKNAME_INNER_WIDTH = 18;
    private Screen screen;
    private TextGraphics tg;
    private int screenWidth = 80;
    private final String BASE_URL = "http://localhost:8080/api/game-sessions";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void start() {
        try {
            screen = (new DefaultTerminalFactory()).createScreen();
            screen.startScreen();
            screen.setCursorPosition((TerminalPosition) null);
            tg = screen.newTextGraphics();
            StringBuilder inputSession = new StringBuilder();
            StringBuilder inputNickname = new StringBuilder();
            int selected = 0;
            String statusMessage = "Введите номер сессии с экрана Cheqmate";
            String errorMessage = null;
            int joined = 0;

            while (true) {
                screenWidth = screen.getTerminalSize().getColumns();
                drawForm(inputSession, inputNickname, selected, statusMessage, errorMessage);
                KeyStroke key = screen.readInput();
                if (key != null) {
                    KeyType type = key.getKeyType();
                    if (type == KeyType.Character) {
                        errorMessage = null;
                        char c = key.getCharacter();
                        if (selected == 0) {
                            if (inputSession.length() < SESSION_MAX_LEN && Character.isDigit(c)) {
                                inputSession.append(c);
                                statusMessage = "Нажмите Enter, чтобы перейти к нику";
                            }
                        } else if (inputNickname.length() < NICKNAME_MAX_LEN && !Character.isWhitespace(c)) {
                            inputNickname.append(c);
                            statusMessage = "Enter — войти в лобби";
                        }
                    } else if (type == KeyType.Backspace) {
                        errorMessage = null;
                        if (selected == 0) {
                            if (!inputSession.isEmpty()) {
                                inputSession.deleteCharAt(inputSession.length() - 1);
                            }
                        } else if (!inputNickname.isEmpty()) {
                            inputNickname.deleteCharAt(inputNickname.length() - 1);
                        }

                        statusMessage = selected == 0
                                ? "Введите номер сессии с экрана Cheqmate"
                                : "Введите ваш ник в приложении";
                    } else if (type == KeyType.Enter) {
                        if (selected == 0) {
                            if (!inputSession.isEmpty()) {
                                selected = 1;
                                statusMessage = "Введите ваш никнейм";
                            } else {
                                statusMessage = "Сначала укажите номер сессии";
                            }
                        } else if (!inputSession.isEmpty() && !inputNickname.isEmpty()) {
                            statusMessage = "Подключение...";
                            errorMessage = null;
                            drawForm(inputSession, inputNickname, selected, statusMessage, errorMessage);

                            JoinResult result = join(inputSession.toString(), inputNickname.toString());
                            if (result == JoinResult.OK) {
                                joined = 1;
                                break;
//                                MenuWindow menu = new MenuWindow(screen, Integer.parseInt(inputSession.toString()));
//                                menu.renderMenu();
                            }
                            errorMessage = joinErrorMessage(result);
                            statusMessage = "Исправьте данные и нажмите Enter";
                        } else {
                            statusMessage = "Заполните никнейм";
                        }
                    } else if (isFieldSwitchKey(type)) {
                        errorMessage = null;
                        selected = 1 - selected;
                        statusMessage = selected == 0 ? "Номер сессии" : "Ваш никнейм";
                    } else if (type == KeyType.Escape) {
                        break;
                    }
                }
            }

            if (joined == 1) {
                MenuWindow menu = new MenuWindow(screen, Integer.parseInt(inputSession.toString()), inputNickname.toString());
                menu.renderMenu();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (screen != null) {
                try {
                    screen.stopScreen();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private JoinResult join(String sessionId, String nickname) {
        try {
            String url = BASE_URL + "/" + sessionId + "/join";
            String jsonBody = "{\"player\":\"" + escapeJson(nickname) + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            int code = response.statusCode();
            if (code >= 200 && code < 300) {
                return JoinResult.OK;
            }
            if (code == 404) {
                return JoinResult.SESSION_NOT_FOUND;
            }
            if (code == 400) {
                return JoinResult.PLAYER_NOT_EXPECTED;
            }
            if (code == 409) {
                return JoinResult.PLAYER_ALREADY_IN_SESSION;
            }
            return JoinResult.CONNECTION_ERROR;
        } catch (Exception e) {
            return JoinResult.CONNECTION_ERROR;
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String joinErrorMessage(JoinResult result) {
        return switch (result) {
            case SESSION_NOT_FOUND -> "Сессия не найдена — проверьте номер";
            case PLAYER_NOT_EXPECTED -> "Имя не ожидалось — введите ник из списка";
            case PLAYER_ALREADY_IN_SESSION -> "Вы уже в этой сессии";
            case CONNECTION_ERROR -> "Не удалось подключиться к серверу";
            default -> "";
        };
    }

    private boolean isFieldSwitchKey(KeyType type) {
        return type == KeyType.ArrowLeft || type == KeyType.ArrowRight
                || type == KeyType.ArrowUp || type == KeyType.ArrowDown || type == KeyType.Tab;
    }

    private void drawForm(StringBuilder session, StringBuilder nickname, int selected,
                          String status, String error) throws IOException {
        screen.clear();
        drawHeader();
        drawCard(session, nickname, selected, status, error);
        screen.refresh();
    }

    private void drawHeader() {
        drawCentered(1, "Cheqmate — игровое лобби", ANSI.YELLOW);
        drawCentered(2, "Подключение к сессии", ANSI.CYAN);
    }

    private void drawCard(StringBuilder session, StringBuilder nickname, int selected,
                          String status, String error) {
        int cardWidth = 52;
        int cardHeight = 17;
        int cardX = (screenWidth - cardWidth) / 2;
        int cardY = 5;
        int innerWidth = cardWidth - 6;
        drawDoubleBox(cardX, cardY, cardWidth, cardHeight);
        drawTextInBox(cardX, cardY, cardWidth, 1, "ПОДКЛЮЧЕНИЕ", ANSI.YELLOW);
        int innerX = cardX + 3;
        int rowSession = cardY + 3;
        int rowNickname = cardY + 8;
        tg.setForegroundColor(ANSI.WHITE);
        tg.putString(innerX, rowSession, "1. Номер сессии");
        tg.putString(innerX, rowNickname, "2. Ваш никнейм");
        drawInputBox(innerX, rowSession + 1, SESSION_INNER_WIDTH + 2, session.toString(), selected == 0);
        drawInputBox(innerX, rowNickname + 1, NICKNAME_INNER_WIDTH + 2, nickname.toString(), selected == 1);
        String step = selected == 0 ? "●○" : "○●";
        tg.setForegroundColor(ANSI.GREEN);
        tg.putString(cardX + cardWidth - 5, cardY + 2, step);

        if (error != null && !error.isEmpty()) {
            drawTextInBox(cardX, cardY, cardWidth, cardHeight - 5, error, ANSI.RED);
            drawTextInBox(cardX, cardY, cardWidth, cardHeight - 4, truncate(status, innerWidth), ANSI.CYAN);
        } else {
            drawTextInBox(cardX, cardY, cardWidth, cardHeight - 5, truncate(status, innerWidth), ANSI.CYAN);
            drawTextInBox(cardX, cardY, cardWidth, cardHeight - 4, "← → ↑ ↓  Tab — сменить поле", ANSI.CYAN);
        }

        drawTextInBox(cardX, cardY, cardWidth, cardHeight - 3, "Enter — продолжить", ANSI.CYAN);
        drawTextInBox(cardX, cardY, cardWidth, cardHeight - 2, "Esc — выход", ANSI.CYAN);
    }

    private void drawTextInBox(int boxX, int boxY, int boxWidth, int rowInsideBox, String text, TextColor color) {
        int innerWidth = boxWidth - 4;
        String line = "  " + padRight(truncate(text, innerWidth), innerWidth);
        tg.setForegroundColor(color);
        tg.putString(boxX + 1, boxY + rowInsideBox, line);
    }

    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 3) + "...";
    }

    private void drawDoubleBox(int x, int y, int width, int height) {
        tg.setForegroundColor(ANSI.WHITE);
        tg.putString(x, y, "╔" + "═".repeat(width - 2) + "╗");

        for (int row = 1; row < height - 1; ++row) {
            tg.putString(x, y + row, "║" + " ".repeat(width - 2) + "║");
        }

        tg.putString(x, y + height - 1, "╚" + "═".repeat(width - 2) + "╝");
    }

    private void drawInputBox(int x, int y, int width, String value, boolean focused) {
        int inner = width - 2;
        String padded = padRight(value, inner);
        TextColor border = focused ? ANSI.GREEN : ANSI.CYAN;
        TextColor text = focused ? ANSI.WHITE_BRIGHT : ANSI.WHITE;
        tg.setForegroundColor(border);
        tg.putString(x, y, "┌" + "─".repeat(inner) + "┐");
        tg.setForegroundColor(text);
        tg.putString(x, y + 1, "│" + padded + "│");
        tg.setForegroundColor(border);
        tg.putString(x, y + 2, "└" + "─".repeat(inner) + "┘");
    }

    private void drawCentered(int y, String text, TextColor color) {
        int x = Math.max(0, (screenWidth - text.length()) / 2);
        tg.setForegroundColor(color);
        tg.putString(x, y, text);
    }

    private String padRight(String value, int width) {
        char[] buffer = new char[width];
        Arrays.fill(buffer, ' ');

        for (int i = 0; i < value.length() && i < width; ++i) {
            buffer[i] = value.charAt(i);
        }

        return new String(buffer);
    }
}
