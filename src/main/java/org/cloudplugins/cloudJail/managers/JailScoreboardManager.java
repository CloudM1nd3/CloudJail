package org.cloudplugins.cloudJail.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.ColorService;
import org.cloudplugins.cloudJail.utils.TimeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JailScoreboardManager {
    private static final int MAX_LINES = 15;
    private static JailScoreboardManager instance;

    private boolean enabled;
    private String titleTemplate;
    private List<String> lineTemplates = List.of();

    private final Map<UUID, BoardSession> sessions = new ConcurrentHashMap<>();

    private JailScoreboardManager() {}

    public static void init() {
        if (instance != null) {
            throw new IllegalStateException("JailScoreboardManager already initialized");
        }
        instance = new JailScoreboardManager();
        instance.loadSettings();
        instance.showOnlineJailed();
    }

    public static JailScoreboardManager getInstance() {
        return instance;
    }

    public void loadSettings() {
        enabled = Config.getBoolean("scoreboard.enable", true);
        titleTemplate = Config.getString("scoreboard.title", "&6&lТюрьма");
        List<String> lines = Config.getStringList("scoreboard.lines");
        int size = Math.min(MAX_LINES, lines.size());
        lineTemplates = List.copyOf(lines.subList(0, size));

        if (!enabled) {
            hideAll();
            return;
        }
        showOnlineJailed();
    }

    public void show(Player player, Punishment punishment) {
        if (!enabled || player == null || punishment == null) return;

        hide(player);

        ScoreboardManager bukkit = Bukkit.getScoreboardManager();
        if (bukkit == null) return;

        Scoreboard previous = player.getScoreboard();
        Scoreboard board = bukkit.getNewScoreboard();
        Objective objective = board.registerNewObjective("cjail", Criteria.DUMMY, renderTitle(punishment));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<Team> teams = new ArrayList<>(lineTemplates.size());
        for (int i = 0; i < lineTemplates.size(); i++) {
            String entry = dummyEntry(i);
            Team team = board.registerNewTeam("cjl" + i);
            team.addEntry(entry);
            team.prefix(renderLine(lineTemplates.get(i), punishment));
            objective.getScore(entry).setScore(lineTemplates.size() - i);
            teams.add(team);
        }

        sessions.put(player.getUniqueId(), new BoardSession(board, previous, objective, teams));
        player.setScoreboard(board);
    }

    public void hide(Player player) {
        BoardSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;

        if (player.getScoreboard() == session.board) {
            Scoreboard restore = session.previous;
            ScoreboardManager bukkit = Bukkit.getScoreboardManager();
            if (restore == null || restore == session.board) {
                restore = bukkit != null ? bukkit.getMainScoreboard() : player.getScoreboard();
            }
            player.setScoreboard(restore);
        }
    }

    public void drop(UUID uuid) {
        sessions.remove(uuid);
    }

    public void hideAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
        }
        sessions.clear();
    }

    public void tick() {
        if (!enabled || sessions.isEmpty()) return;

        PunishmentManager punishments = PunishmentManager.getInstance();
        for (Player player : Bukkit.getOnlinePlayers()) {
            BoardSession session = sessions.get(player.getUniqueId());
            if (session == null || player.getScoreboard() != session.board) continue;

            Punishment punishment = punishments.getActivePunishment(player.getUniqueId());
            if (punishment == null) {
                hide(player);
                continue;
            }
            session.objective.displayName(renderTitle(punishment));
            for (int i = 0; i < session.teams.size(); i++) {
                session.teams.get(i).prefix(renderLine(lineTemplates.get(i), punishment));
            }
        }
    }

    private void showOnlineJailed() {
        PunishmentManager punishments = PunishmentManager.getInstance();
        if (punishments == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Punishment punishment = punishments.getActivePunishment(player.getUniqueId());
            if (punishment != null) {
                show(player, punishment);
            }
        }
    }

    private Component renderTitle(Punishment punishment) {
        return ColorService.processLegacy(apply(titleTemplate, punishment));
    }

    private Component renderLine(String template, Punishment punishment) {
        String text = apply(template, punishment);
        if (text.isEmpty()) {
            return Component.text(" ");
        }
        return ColorService.processLegacy(text);
    }

    private String apply(String template, Punishment punishment) {
        String reason = punishment.getReason() == null ? "" : punishment.getReason();
        String remaining = TimeParser.format(punishment.getRemainingMillis());
        return template
                .replace("%punisher%", punishment.getPunisherName())
                .replace("%reason%", reason)
                .replace("%time%", TimeParser.format(punishment.getDuration()))
                .replace("%remaining%", remaining)
                .replace("%jail%", punishment.getJailName())
                .replace("%player%", punishment.getPlayerName());
    }

    private static String dummyEntry(int index) {
        return "\u00A7" + Integer.toHexString(index);
    }

    private record BoardSession(Scoreboard board, Scoreboard previous, Objective objective, List<Team> teams) {}
}
