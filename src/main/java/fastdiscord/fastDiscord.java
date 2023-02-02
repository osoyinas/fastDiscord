package fastdiscord;

import fastdiscord.Connect.CheckIp;
import fastdiscord.events.PlayerEvents;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class fastDiscord extends JavaPlugin {
    final private String botToken = getConfig().getString("token");
    final private String statusChannelId = getConfig().getString("channel_id");
    final private boolean customIp = getConfig().getBoolean("custom_ip");
    private JDA jda;
    private TextChannel statusChannel;
    private boolean serverState;
    private PlayerEvents playerEvents;
    private boolean connected;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        serverState = true; //Active server
        activateEvents();
        if (!connectJDA()) {
            return;
        }
        updateStatus();
    }

    @Override
    public void onDisable() {
        if(!connected)
            return;
        serverState = false; //Inactive  server
        updateStatus();
        jda.shutdown();
    }

    /**
     * Connects to the discord bot
     * @return true if success
     */
    private boolean connectJDA() {
        connected = false;
        try {
            jda = JDABuilder.createDefault(botToken).build();
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Incorrect token, please paste it in config.yml");
            return connected;
        }
        if (statusChannelId != null) {
            this.statusChannel = jda.getTextChannelById(statusChannelId);
        }

        if (this.statusChannel == null) {
            Bukkit.getLogger().severe("Incorrect channel ID, please paste it in config.yml");
            return connected;
        }
        connected = true;
        return true;
    }

    private void activateEvents() {
        playerEvents = new PlayerEvents(this);
    }

    public void updateStatus() {
        String message = statusFormatMessage();
        MessageHistory messageHistory = new MessageHistory(statusChannel);
        List<Message> messages = messageHistory.retrievePast(1).complete();

        if (messageHistory.isEmpty()) {
            sendMessage(message);
        } else {
            editMessage(messages.get(0).getId(), message);
        }
    }

    private void sendMessage(String messageContent) {
        try {
            statusChannel.sendMessage(messageContent).complete();
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    private void editMessage(String messageId, String messageContent) {
        try {
            statusChannel.editMessageById(messageId, messageContent).complete();
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    /**
     * Format the message tha will be sent to discord, obtaining the ip, number of players...
     * @return String
     */
    private String statusFormatMessage() {
        StringBuilder message;
        if (serverState) {
            message = new StringBuilder(":white_check_mark: **SERVER ACTIVE** :white_check_mark:\n\n");
            int maxPlayers = Bukkit.getMaxPlayers();
            if (!customIp)
                message.append("> **IP:     **" + "`").append(CheckIp.getPublicIp()).append(":").append(Bukkit.getPort()).append("`\n");
            else
                message.append("> **IP:     **" + "`").append(Bukkit.getIp()).append(":").append(Bukkit.getPort()).append("`\n");

            message.append("> **Players:    **" + "`").append(playerEvents.getOnlinePlayers().size()).append("/").append(maxPlayers).append("`\n");
            if (playerEvents.getOnlinePlayers().size() > 0) {
                message.append("```");
                for (Player player : playerEvents.getOnlinePlayers()) {
                    message.append(player.getDisplayName());
                }
                message.append("```");
            }
        } else
            message = new StringBuilder(":octagonal_sign: **SERVER INACTIVE** :octagonal_sign:\n");

        return message.toString();
    }
}
