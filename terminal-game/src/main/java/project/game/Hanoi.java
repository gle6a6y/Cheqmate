package project.game;

import com.google.gson.Gson;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import project.game.dto.GameSessionResponse;
import project.game.dto.PlayerProgressResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Hanoi {

    enum ExitAction {
        RETURN_TO_MENU,
        QUIT_APPLICATION
    }

    private static final int DISK_COUNT = 5;
    private static final int TOWER_SLOT_WIDTH = 11;
    private static final int TOWER_GAP = 8;
    private static final int MARKER_OFFSET_BELOW_DISKS = 2;
    private static final int CARD_WIDTH = 72;
    private static final int CARD_HEIGHT = 20;
    private static final int TOWER_TOP_INSIDE_CARD = 9;
    private static final long PROGRESS_SYNC_MS = 1000;
    private static final long LOOP_SLEEP_MS = 30;
    private static final String BASE_URL = "http://localhost:8080/api/game-sessions";

    private final Screen screen;
    private final TextGraphics tg;
    private final int sessionId;
    private final String nickname;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    private final List<Stack<Integer>> towers;
    private final List<String> positions;

    private int screenWidth = 80;
    private int screenHeight = 24;
    private int selected = 0;
    private boolean taken = false;
    private int taken_disk = 0;
    private int taken_disk_from = -1;
    private int moves = 0;

    private List<PlayerProgressResponse> sessionPlayers = new ArrayList<>();
    private String sessionLoser = null;
    private long lastSync = 0;
    private boolean needsRedraw = true;
    private boolean localFinished = false;

    Hanoi(Screen screen, int sessionId, String nickname) {
        this.screen = screen;
        this.sessionId = sessionId;
        this.nickname = nickname;
        this.tg = screen.newTextGraphics();

        towers = new ArrayList<>();
        towers.add(new Stack<>());
        towers.getFirst().push(5);
        towers.getFirst().push(4);
        towers.getFirst().push(3);
        towers.getFirst().push(2);
        towers.getFirst().push(1);
        towers.add(new Stack<>());
        towers.add(new Stack<>());

        positions = new ArrayList<>(List.of("1", "2", "3"));
    }

    ExitAction start() throws IOException, InterruptedException {
        boolean running = true;
        ExitAction exitAction = ExitAction.RETURN_TO_MENU;
        syncProgress(false);
        lastSync = System.currentTimeMillis();

        while (running) {
            screenWidth = screen.getTerminalSize().getColumns();
            screenHeight = screen.getTerminalSize().getRows();

            long now = System.currentTimeMillis();
            if (now - lastSync >= PROGRESS_SYNC_MS) {
                syncProgress(localFinished || isEnd());
                lastSync = now;
            }

            if (!localFinished && isEnd()) {
                localFinished = true;
                syncProgress(true);
                lastSync = System.currentTimeMillis();
            }

            if (needsRedraw) {
                if (hasLoser()) {
                    drawLoserResult();
                } else if (localFinished) {
                    drawWaitingScreen();
                } else {
                    draw();
                }
                screen.refresh();
                needsRedraw = false;
            }

            KeyStroke key = screen.pollInput();
            if (key != null) {
                KeyType type = key.getKeyType();
                if (hasLoser() && type == KeyType.Escape) {
                    exitAction = ExitAction.QUIT_APPLICATION;
                    running = false;
                } else if (type == KeyType.Escape) {
                    syncProgress(localFinished || isEnd());
                    running = false;
                } else if (!localFinished && !hasLoser()) {
                    handlePlayingInput(type);
                }
            }

            Thread.sleep(LOOP_SLEEP_MS);
        }
        return exitAction;
    }

    private boolean hasLoser() {
        return sessionLoser != null && !sessionLoser.isEmpty();
    }

    private void handlePlayingInput(KeyType type) {
        if (type == KeyType.ArrowRight) {
            selected = (selected + 1) % 3;
            needsRedraw = true;
        } else if (type == KeyType.ArrowLeft) {
            selected = (selected + 2) % 3;
            needsRedraw = true;
        } else if (type == KeyType.Enter) {
            if (!taken) {
                if (!towers.get(selected).isEmpty()) {
                    taken_disk = towers.get(selected).peek();
                    taken_disk_from = selected;
                    taken = true;
                    needsRedraw = true;
                }
            } else if (towers.get(selected).isEmpty() || taken_disk <= towers.get(selected).peek()) {
                towers.get(taken_disk_from).pop();
                towers.get(selected).push(taken_disk);
                taken_disk = 0;
                taken_disk_from = -1;
                taken = false;
                moves++;
                syncProgress(isEnd());
                lastSync = System.currentTimeMillis();
                needsRedraw = true;
            }
        }
    }

    private void syncProgress(boolean finished) {
        pushProgress(finished);
        fetchSessionPlayers();
        needsRedraw = true;
    }

    private void pushProgress(boolean finished) {
        try {
            String url = BASE_URL + "/" + sessionId + "/progress";
            String json = "{\"player\":\"" + escapeJson(nickname) + "\",\"moves\":" + moves
                    + ",\"finished\":" + finished + "}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.out.println("progress: " + e.getMessage());
        }
    }

    private void fetchSessionPlayers() {
        try {
            String url = BASE_URL + "/" + sessionId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                GameSessionResponse session = gson.fromJson(response.body(), GameSessionResponse.class);
                if (session != null) {
                    if (session.getPlayers() != null) {
                        sessionPlayers = session.getPlayers();
                    }
                    sessionLoser = session.getLoser();
                }
            }
        } catch (Exception e) {
            System.out.println("fetch: " + e.getMessage());
        }
    }

    private int countFinishedInLobby() {
        int count = 0;
        for (PlayerProgressResponse player : sessionPlayers) {
            if (player.isJoined() && player.isFinished()) {
                count++;
            }
        }
        return count;
    }

    private int countInLobby() {
        int count = 0;
        for (PlayerProgressResponse player : sessionPlayers) {
            if (player.isJoined()) {
                count++;
            }
        }
        return count;
    }

    public boolean isEnd() {
        return towers.get(1).size() == DISK_COUNT || towers.get(2).size() == DISK_COUNT;
    }

    private void drawWaitingScreen() {
        screen.clear();
        drawCentered(2, "Вы закончили за " + moves + " ходов — ждём остальных", ANSI.YELLOW);

        int cardX = Math.max(0, (screenWidth - CARD_WIDTH) / 2);
        int cardY = 4;
        drawDoubleBox(cardX, cardY, CARD_WIDTH, CARD_HEIGHT);
        drawPlayersPanel(cardX, cardY);

        int finished = countFinishedInLobby();
        int inLobby = countInLobby();
        String waitLine = "Готово: " + finished + " / " + inLobby;
        drawTextInBox(cardX, cardY, CARD_WIDTH, 10, waitLine, ANSI.CYAN);
        drawTextInBox(cardX, cardY, CARD_WIDTH, CARD_HEIGHT - 2, "Esc — выход", ANSI.CYAN);
    }

    private void drawLoserResult() {
        screen.clear();

        int cardX = Math.max(0, (screenWidth - CARD_WIDTH) / 2);
        int cardY = Math.max(0, (screenHeight - 14) / 2);
        drawDoubleBox(cardX, cardY, CARD_WIDTH, 14);
        drawTextInBox(cardX, cardY, CARD_WIDTH, 1, "  ИТОГ  ", ANSI.YELLOW);

        String headline = sessionLoser + " платит за всех!";
        TextColor headlineColor = sessionLoser.equals(nickname) ? ANSI.RED : ANSI.GREEN;
        drawTextInBox(cardX, cardY, CARD_WIDTH, 3, headline, headlineColor);
        drawTextInBox(cardX, cardY, CARD_WIDTH, 4, "(больше всех ходов)", ANSI.CYAN);

        int row = 6;
        for (PlayerProgressResponse player : sessionPlayers) {
            if (!player.isJoined()) {
                continue;
            }
            String name = player.getName();
            if (nickname.equals(name)) {
                name = name + " (ты)";
            }
            boolean isLoser = sessionLoser.equals(player.getName());
            String line = padRight(truncate(name, 16), 16)
                    + padLeft(String.valueOf(player.getMoves()), 5)
                    + " ходов"
                    + (isLoser ? "  ← платит" : "");
            drawTextInBox(cardX, cardY, CARD_WIDTH, row, line, isLoser ? ANSI.RED : ANSI.WHITE);
            row++;
            if (row > 10) {
                break;
            }
        }

        drawTextInBox(cardX, cardY, CARD_WIDTH, 12, "Esc — выход из игры", ANSI.CYAN);
    }

    public void draw() {
        screen.clear();

        drawStatusLine(2);

        int cardX = Math.max(0, (screenWidth - CARD_WIDTH) / 2);
        int cardY = 4;
        drawDoubleBox(cardX, cardY, CARD_WIDTH, CARD_HEIGHT);

        drawPlayersPanel(cardX, cardY);

        int[] towerX = towerColumnXs(cardX);
        int towerTop = cardY + TOWER_TOP_INSIDE_CARD;

        for (int i = 0; i < 3; i++) {
            drawTower(towerX[i], towerTop, i);
        }

        drawTowerMarkers(towerX, towerTop + DISK_COUNT + MARKER_OFFSET_BELOW_DISKS);

        drawTextInBox(cardX, cardY, CARD_WIDTH, CARD_HEIGHT - 3,
                "← → — башня   Enter — взять / положить", ANSI.CYAN);
        drawTextInBox(cardX, cardY, CARD_WIDTH, CARD_HEIGHT - 2, "Esc — выход", ANSI.CYAN);
    }

    private void drawPlayersPanel(int cardX, int cardY) {
        int x = cardX + 3;
        int y = cardY + 1;

        tg.setForegroundColor(ANSI.YELLOW);
        tg.putString(x, y, "Участники");

        int row = y + 1;
        int maxRows = 6;
        int shown = 0;

        for (PlayerProgressResponse player : sessionPlayers) {
            if (shown >= maxRows) {
                tg.setForegroundColor(ANSI.CYAN);
                tg.putString(x, row, "...");
                break;
            }

            String name = player.getName();
            if (nickname.equals(name)) {
                name = name + " (ты)";
            }

            String status;
            TextColor color;
            if (hasLoser() && sessionLoser.equals(player.getName())) {
                status = "платит";
                color = ANSI.RED;
            } else if (!player.isJoined()) {
                status = "ожид.";
                color = ANSI.BLACK_BRIGHT;
            } else if (player.isFinished()) {
                status = "готов";
                color = ANSI.GREEN;
            } else {
                status = "играет";
                color = ANSI.CYAN;
            }

            String line = padRight(truncate(name, 14), 14)
                    + padLeft(String.valueOf(player.getMoves()), 4)
                    + "  " + status;

            tg.setForegroundColor(color);
            tg.putString(x, row, truncate(line, 28));
            row++;
            shown++;
        }

        if (sessionPlayers.isEmpty()) {
            tg.setForegroundColor(ANSI.BLACK_BRIGHT);
            tg.putString(x, row, "загрузка...");
        }
    }

    private void drawStatusLine(int row) {
        String status;
        if (taken) {
            status = "Диск " + taken_disk + " в руке — Enter на башню " + (selected + 1);
        } else {
            status = "Ходов: " + moves + "   Enter — взять с башни " + (selected + 1);
        }
        drawCentered(row, status, ANSI.CYAN);
    }

    private int[] towerColumnXs(int cardX) {
        int innerWidth = CARD_WIDTH - 6;
        int blockWidth = 3 * TOWER_SLOT_WIDTH + 2 * TOWER_GAP;
        int startX = cardX + 3 + Math.max(0, (innerWidth - blockWidth) / 2);
        return new int[]{
                startX,
                startX + TOWER_SLOT_WIDTH + TOWER_GAP,
                startX + 2 * (TOWER_SLOT_WIDTH + TOWER_GAP)
        };
    }

    private void drawTower(int x, int y, int towerIndex) {
        int[] diskRows = buildTowerDiskRows(towerIndex);
        for (int i = 0; i < diskRows.length; i++) {
            if (diskRows[i] == 0) {
                drawPoleLine(x, y + i);
            } else {
                drawColoredDiskLine(x, y + i, diskRows[i]);
            }
        }

        if (taken && towerIndex == taken_disk_from) {
            tg.setForegroundColor(ANSI.YELLOW);
            tg.putString(x + TOWER_SLOT_WIDTH / 2, y - 1, "▼");
        }
    }

    private void drawPoleLine(int x, int y) {
        tg.setForegroundColor(ANSI.BLACK_BRIGHT);
        tg.putString(x, y, poleLine());
    }

    private void drawColoredDiskLine(int x, int y, int disk) {
        String line = diskLine(disk);
        TextColor diskClr = diskColor(disk);

        for (int col = 0; col < line.length(); col++) {
            char ch = line.charAt(col);
            if (ch == '|') {
                tg.setForegroundColor(ANSI.BLACK_BRIGHT);
            } else if (ch == '█') {
                tg.setForegroundColor(diskClr);
            } else {
                tg.setForegroundColor(ANSI.BLACK);
            }
            tg.putString(x + col, y, String.valueOf(ch));
        }
    }

    private TextColor diskColor(int disk) {
        return switch (disk) {
            case 1 -> ANSI.RED;
            case 2 -> ANSI.YELLOW;
            case 3 -> ANSI.CYAN;
            case 4 -> ANSI.GREEN;
            case 5 -> ANSI.MAGENTA;
            default -> ANSI.WHITE;
        };
    }

    private void drawTowerMarkers(int[] towerX, int row) {
        for (int i = 0; i < positions.size(); i++) {
            String label = selected == i ? "▶ " + (i + 1) + " ◀" : "  " + (i + 1) + "  ";
            int x = towerX[i] + (TOWER_SLOT_WIDTH - label.length()) / 2;
            tg.setForegroundColor(selected == i ? ANSI.GREEN : ANSI.CYAN);
            tg.putString(x, row, label);
        }
    }

    private int[] buildTowerDiskRows(int towerIndex) {
        int[] rows = new int[DISK_COUNT];
        Stack<Integer> tower = towers.get(towerIndex);
        int empty = DISK_COUNT - tower.size();

        for (int i = 0; i < empty; i++) {
            rows[i] = 0;
        }

        for (int i = tower.size() - 1; i >= 0; i--) {
            rows[empty + (tower.size() - 1 - i)] = tower.get(i);
        }
        return rows;
    }

    private String poleLine() {
        int pad = (TOWER_SLOT_WIDTH - 1) / 2;
        return " ".repeat(pad) + "|" + " ".repeat(TOWER_SLOT_WIDTH - pad - 1);
    }

    private String diskLine(int disk) {
        int side = (TOWER_SLOT_WIDTH - 1) / 2 - disk;
        return " ".repeat(Math.max(0, side))
                + "█".repeat(disk)
                + "|"
                + "█".repeat(disk)
                + " ".repeat(Math.max(0, side));
    }

    private void drawDoubleBox(int x, int y, int width, int height) {
        tg.setForegroundColor(ANSI.WHITE);
        tg.putString(x, y, "╔" + "═".repeat(width - 2) + "╗");
        for (int row = 1; row < height - 1; row++) {
            tg.putString(x, y + row, "║" + " ".repeat(width - 2) + "║");
        }
        tg.putString(x, y + height - 1, "╚" + "═".repeat(width - 2) + "╝");
    }

    private void drawTextInBox(int boxX, int boxY, int boxWidth, int rowInsideBox, String text, TextColor color) {
        int innerWidth = boxWidth - 4;
        String line = "  " + padRight(truncate(text, innerWidth), innerWidth);
        tg.setForegroundColor(color);
        tg.putString(boxX + 1, boxY + rowInsideBox, line);
    }

    private void drawCentered(int y, String text, TextColor color) {
        int x = Math.max(0, (screenWidth - text.length()) / 2);
        tg.setForegroundColor(color);
        tg.putString(x, y, text);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 3) + "...";
    }

    private String padRight(String value, int width) {
        if (value.length() >= width) {
            return value.substring(0, width);
        }
        return value + " ".repeat(width - value.length());
    }

    private String padLeft(String value, int width) {
        if (value.length() >= width) {
            return value.substring(0, width);
        }
        return " ".repeat(width - value.length()) + value;
    }
}
