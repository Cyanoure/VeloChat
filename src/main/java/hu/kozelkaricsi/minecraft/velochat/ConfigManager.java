package hu.kozelkaricsi.minecraft.velochat;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private final Path dataFolder;
    private final Path file;
    private Toml toml;

    public ConfigManager(@DataDirectory @NotNull Path dataFolder){
        this.dataFolder = dataFolder;
        this.file = this.dataFolder.resolve("config.toml");

        saveDefaultConfig();
        toml = loadConfig();

        applyLoadedConfig();
    }

    public void applyLoadedConfig(){
        // Settings
        VeloChat.instance.luckpermsIntegration = toml.getBoolean("Settings.luckermsIntegration");
        VeloChat.instance.globalChat = toml.getBoolean("Settings.globalChat");

        // Spam Filter
        VeloChat.instance.spamFilter = toml.getBoolean("SpamFilter.enabled");
        VeloChat.instance.spamFilterRepeat = toml.getBoolean("SpamFilter.spamFilterRepeat");
        VeloChat.instance.spamTimeoutMillis = toml.getLong("SpamFilter.spamTimeoutMillis").intValue();
        VeloChat.instance.maxSpamWarnings = toml.getLong("SpamFilter.maxWarningsBeforeMute").intValue();
        VeloChat.instance.spamMuteSeconds = toml.getLong("SpamFilter.spamMuteSeconds").intValue();

        // PM Formats
        VeloChat.instance.pmSendFormat = toml.getString("Format.MSG.sendFormat");
        VeloChat.instance.pmReceiveFormat = toml.getString("Format.MSG.receiveFormat");

        // Chat Format
        VeloChat.instance.chatFormat = toml.getString("Format.Chat.format");

        // Alert Format
        VeloChat.instance.alertFormat = toml.getString("Format.Alert.format");

        // Usages
        VeloChat.instance.alertUsage = toml.getString("Usages.alert");
        VeloChat.instance.muteUsage = toml.getString("Usages.mute");
        VeloChat.instance.pmUsage = toml.getString("Usages.msg");
        VeloChat.instance.replyUsage = toml.getString("Usages.reply");

        // Messages
        VeloChat.instance.wrongUsage = toml.getString("Messages.wrongUsage");
        VeloChat.instance.spamWarning = toml.getString("Messages.spamWarning");
        VeloChat.instance.haveBeenMuted = toml.getString("Messages.haveBeenMuted");
        VeloChat.instance.haveBeenMutedReason = toml.getString("Messages.haveBeenMutedReason");
        VeloChat.instance.mutedMessage = toml.getString("Messages.muted");
        VeloChat.instance.muteStaffMessage = toml.getString("Messages.muteStaffMessage");
        VeloChat.instance.muteStaffMessageReason = toml.getString("Messages.muteStaffMessageReason");
        VeloChat.instance.plrNotFound = toml.getString("Messages.playerNotFound");
        VeloChat.instance.noPerm = toml.getString("Messages.noPerm");
        VeloChat.instance.noReply = toml.getString("Messages.noReply");
        VeloChat.instance.second = toml.getString("Messages.second");
        VeloChat.instance.seconds = toml.getString("Messages.seconds");
        VeloChat.instance.minute = toml.getString("Messages.minute");
        VeloChat.instance.minutes = toml.getString("Messages.minutes");
        VeloChat.instance.hour = toml.getString("Messages.hour");
        VeloChat.instance.hours = toml.getString("Messages.hours");
        VeloChat.instance.serverString = toml.getString("Messages.server");
    }

    private void saveDefaultConfig() {
        //noinspection ResultOfMethodCallIgnored
        if (!Files.exists(dataFolder)) {
            try {
                Files.createDirectory(dataFolder);
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        }

        if(!Files.exists(file)) {
            try (InputStream in = ConfigManager.class.getResourceAsStream("/config.toml")) {
                assert in != null;
                Files.copy(in, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Toml loadConfig() {
        try {
            return new Toml().read(Files.newInputStream(this.file));
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
