package project.game;

import com.google.gson.Gson;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import project.game.dto.GameSessionResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuWindow {

    private static final int CARD_WIDTH = 52;
    private static final int CARD_HEIGHT = 20;

    private static class Snapshot {
        final long gameVotesCount;
        final List<String> expectedPlayers;
        final List<String> joinedPlayers;

        Snapshot(long gameVotesCount, List<String> expectedPlayers, List<String> joinedPlayers) {
            this.gameVotesCount = gameVotesCount;
            this.expectedPlayers = new ArrayList<>(expectedPlayers);
            this.joinedPlayers = new ArrayList<>(joinedPlayers);
        }

        static Snapshot of(long gameVotesCount, List<String> expectedPlayers, List<String> joinedPlayers) {
            return new Snapshot(gameVotesCount, expectedPlayers, joinedPlayers);
        }
    }

    private final Screen screen;
    private final int sessionId;
    private final List<String> menuItems = new ArrayList<>(List.of("Ханойская башня", "Выход"));

    private TextGraphics tg;
    private int screenWidth = 80;
    private int selected = 0;
    private String statusMessage = "Выберите игру и нажмите Enter";

    private long gameVotesCount = 0;
    private final List<String> expectedPlayers = new ArrayList<>();
    private final List<String> joinedPlayers = new ArrayList<>();

    private final String BASE_URL = "http://localhost:8080/api/game-sessions";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String nickname;

    MenuWindow(Screen screen, int sessionId, String nickname) {
        this.screen = screen;
        this.sessionId = sessionId;
        this.nickname = nickname;
    }

    public void renderMenu() {
        try {
            tg = screen.newTextGraphics();
            boolean running = true;
            boolean needsRedraw = true;
            boolean hanoiStarted = false;

            long lastFetch = 0;
            Snapshot lastSnapshot = Snapshot.of(0, List.of(), List.of());

            refreshLobbyState();
            lastFetch = System.currentTimeMillis();

            while (running) {
                long now = System.currentTimeMillis();

                if (now - lastFetch >= 500) {
                    refreshLobbyState();
                    lastFetch = now;
                }

                if (needsRedraw || stateChanged(lastSnapshot)) {
                    screenWidth = screen.getTerminalSize().getColumns();
                    draw();
                    screen.refresh();
                    lastSnapshot = Snapshot.of(gameVotesCount, expectedPlayers, joinedPlayers);
                    needsRedraw = false;
                }

                if (!hanoiStarted
                        && !expectedPlayers.isEmpty()
                        && gameVotesCount >= expectedPlayers.size()) {
                    hanoiStarted = true;
                    Hanoi hanoiGame = new Hanoi(screen, sessionId, nickname);
                    if (hanoiGame.start() == Hanoi.ExitAction.QUIT_APPLICATION) {
                        disconnect();
                        running = false;
                        break;
                    }
                    statusMessage = "С возвращением в меню";
                    needsRedraw = true;
                    refreshLobbyState();
                    lastFetch = System.currentTimeMillis();
                }

                KeyStroke key = screen.pollInput();
                if (key != null) {
                    KeyType type = key.getKeyType();
                    if (type == KeyType.ArrowDown) {
                        selected = (selected + 1) % menuItems.size();
                        needsRedraw = true;
                    } else if (type == KeyType.ArrowUp) {
                        selected = (selected - 1 + menuItems.size()) % menuItems.size();
                        needsRedraw = true;
                    } else if (type == KeyType.Enter) {
                        if (menuItems.get(selected).equals("Ханойская башня")) {
                            statusMessage = "Голос учтён, ждём остальных...";
                            vote();
                            refreshLobbyState();
                            lastFetch = System.currentTimeMillis();
                            needsRedraw = true;
                        } else {
                            disconnect();
                            running = false;
                        }
                    } else if (type == KeyType.Escape) {
                        disconnect();
                        running = false;
                    }
                }

                Thread.sleep(30);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean stateChanged(Snapshot lastSnapshot) {
        return gameVotesCount != lastSnapshot.gameVotesCount
                || !expectedPlayers.equals(lastSnapshot.expectedPlayers)
                || !joinedPlayers.equals(lastSnapshot.joinedPlayers);
    }

    private void refreshLobbyState() {
        try {
            String url = BASE_URL + "/" + sessionId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                GameSessionResponse session = gson.fromJson(response.body(), GameSessionResponse.class);
                if (session == null) {
                    return;
                }
                gameVotesCount = session.getReady();
                joinedPlayers.clear();
                if (session.getJoinedPlayers() != null) {
                    joinedPlayers.addAll(session.getJoinedPlayers());
                }
                expectedPlayers.clear();
                if (session.getExpectedPlayers() != null) {
                    expectedPlayers.addAll(session.getExpectedPlayers());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void disconnect() {
        try {
            String url = BASE_URL + "/" + sessionId + "/disconnect/" + nickname;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("disconnect: " + response.statusCode() + " " + response.body());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void vote() {
        try {
            String url = BASE_URL + "/" + sessionId + "/ready";
            String jsonBody = "{\"player\":\"" + escapeJson(nickname) + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return;
            }
            statusMessage = "Не удалось проголосовать";
            System.out.println("vote: " + response.statusCode() + " " + response.body());
        } catch (Exception e) {
            statusMessage = "Ошибка сети при голосовании";
            System.out.println(e.getMessage());
        }
    }

    private void draw() {
        screen.clear();
        drawMenuCard();
    }

    private void drawMenuCard() {
        int cardX = (screenWidth - CARD_WIDTH) / 2;
        int terminalHeight = screen.getTerminalSize().getRows();
        int cardY = Math.max(0, (terminalHeight - CARD_HEIGHT) / 2);
        int innerWidth = CARD_WIDTH - 6;
        int optionWidth = CARD_WIDTH - 6;
        int innerX = cardX + 3;

        drawDoubleBox(cardX, cardY, CARD_WIDTH, CARD_HEIGHT);
        drawTextInBox(cardX, cardY, CARD_WIDTH, 1, "МЕНЮ ИГРЫ", ANSI.YELLOW);

        tg.setForegroundColor(ANSI.WHITE);
        tg.putString(innerX, cardY + 2, "Сессия: " + sessionId);

        int expectedCount = expectedPlayers.size();
        String lobbyLine = "В лобби (" + joinedPlayers.size() + "/" + expectedCount + "): "
                + String.join(", ", joinedPlayers);
        drawTextInBox(cardX, cardY, CARD_WIDTH, 3, lobbyLine, ANSI.WHITE);

        String waitingLine = formatWaitingPlayers();
        if (!waitingLine.isEmpty()) {
            drawTextInBox(cardX, cardY, CARD_WIDTH, 4, waitingLine, ANSI.WHITE);
        }

        drawTextInBox(cardX, cardY, CARD_WIDTH, 5,
                "Игра начнется, когда все выберут одну игру", ANSI.YELLOW);

        int firstOptionRow = cardY + 7;
        for (int i = 0; i < menuItems.size(); i++) {
            drawMenuOption(innerX, firstOptionRow + i * 4, optionWidth, menuItemLabel(i), selected == i);
        }

        drawTextInBox(cardX, cardY, CARD_WIDTH, 15, truncate(statusMessage, innerWidth), ANSI.CYAN);
        drawTextInBox(cardX, cardY, CARD_WIDTH, 16, "↑ ↓ — выбор   Enter — подтвердить", ANSI.CYAN);
        drawTextInBox(cardX, cardY, CARD_WIDTH, 17, "Esc — выход", ANSI.CYAN);
    }

    private String formatWaitingPlayers() {
        List<String> waiting = new ArrayList<>();
        for (String name : expectedPlayers) {
            if (!joinedPlayers.contains(name)) {
                waiting.add(name);
            }
        }
        if (waiting.isEmpty()) {
            return "";
        }
        return "Ожидаем: " + String.join(", ", waiting);
    }

    private String menuItemLabel(int index) {
        String base = menuItems.get(index);
        if ("Ханойская башня".equals(base)) {
            int total = Math.max(1, expectedPlayers.size());
            return base + "  " + gameVotesCount + "/" + total;
        }
        return base;
    }

    private void drawMenuOption(int x, int y, int outerWidth, String label, boolean focused) {
        int inner = outerWidth - 2;
        TextColor border = focused ? ANSI.GREEN : ANSI.CYAN;
        TextColor text = focused ? ANSI.WHITE_BRIGHT : ANSI.WHITE;
        String marker = focused ? "▶ " : "  ";
        String content = marker + label;
        String padded = padRight(truncate(content, inner), inner);

        tg.setForegroundColor(border);
        tg.putString(x, y, "┌" + "─".repeat(inner) + "┐");
        tg.setForegroundColor(text);
        tg.putString(x, y + 1, "│" + padded + "│");
        tg.setForegroundColor(border);
        tg.putString(x, y + 2, "└" + "─".repeat(inner) + "┘");
    }

    private void drawTextInBox(int boxX, int boxY, int boxWidth, int rowInsideBox, String text, TextColor color) {
        int innerWidth = boxWidth - 4;
        String line = "  " + padRight(truncate(text, innerWidth), innerWidth);
        tg.setForegroundColor(color);
        tg.putString(boxX + 1, boxY + rowInsideBox, line);
    }

    private void drawDoubleBox(int x, int y, int width, int height) {
        tg.setForegroundColor(ANSI.WHITE);
        tg.putString(x, y, "╔" + "═".repeat(width - 2) + "╗");
        for (int row = 1; row < height - 1; row++) {
            tg.putString(x, y + row, "║" + " ".repeat(width - 2) + "║");
        }
        tg.putString(x, y + height - 1, "╚" + "═".repeat(width - 2) + "╝");
    }

    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 3) + "...";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String padRight(String value, int width) {
        char[] buffer = new char[width];
        Arrays.fill(buffer, ' ');
        for (int i = 0; i < value.length() && i < width; i++) {
            buffer[i] = value.charAt(i);
        }
        return new String(buffer);
    }
}
