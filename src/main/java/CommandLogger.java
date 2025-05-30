/*
Код написан для открытого использования!
+ Поддерживает префиксы от LuckPerms
Написан: localhost (дс: only_localhost | тг: @local_explorer)
Версия кода: v1 (31.05.2025)
*/

import net.luckperms.api.LuckPermsProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.luckperms.api.LuckPerms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public final class CommandLogger extends JavaPlugin implements Listener
{
    private SimpleDateFormat dateFormat;
    private Path logFile;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        reloadConfig();
        reloadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void reloadConfigValues()
    {
        dateFormat = new SimpleDateFormat(getConfig().getString("logging.date-format", "dd.MM.yyyy HH:mm:ss"));
        logFile = getDataFolder().toPath().resolve(getConfig().getString("logging.file", "commands.log"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e)
    {
        Player player = e.getPlayer();
        String command = e.getMessage().replaceFirst("/", "").replace("cmi", "").replaceFirst("^\\s+", "");

        if (getConfig().getBoolean("log-all"))
        {
            List<String> excluded = getConfig().getStringList("excluded-commands");
            if (excluded.isEmpty() || excluded.stream()
                    .noneMatch(s -> s.equalsIgnoreCase(command)))
            {
                logCmd(player, e.getMessage());
            }
        }
        else {
            for (String string : getConfig().getStringList("enabled-commands"))
            {
                if (string.equalsIgnoreCase(command.split(" ")[0]))
                {
                    logCmd(player, e.getMessage());
                }
            }
        }
    }

    private void logCmd(Player player, String cmd)
    {
        // создание лога
        String cleanCmd = cmd.startsWith("/") ? cmd.substring(1) : cmd;
        LuckPerms api = LuckPermsProvider.get();
        String prefix = Objects.requireNonNull(api.getUserManager().getUser(player.getUniqueId())).getCachedData().getMetaData().getPrefix();
        assert prefix != null;
        String logEntry = getConfig().getString("logging.format", "[{date}] {player}: /{command}")
                .replace("{date}", dateFormat.format(new Date()))
                .replace("{prefix}", prefix)
                .replace("{player}", player.getName())
                .replace("{command}", cleanCmd);

        // запись в файл
        try {
            Files.write(logFile, (logEntry + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            getLogger().warning("Error write log!\n[cmdlog->info] - " + e.getMessage());
        }
    }

}