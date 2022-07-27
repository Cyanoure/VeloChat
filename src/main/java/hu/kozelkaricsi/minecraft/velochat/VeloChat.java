package hu.kozelkaricsi.minecraft.velochat;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import hu.kozelkaricsi.minecraft.velochat.commands.AlertCommand;
import hu.kozelkaricsi.minecraft.velochat.commands.MuteCommand;
import hu.kozelkaricsi.minecraft.velochat.commands.PMCommand;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Plugin(
        id = "velochat",
        name = "VeloChat",
        version = BuildConstants.VERSION,
        description = "Chat plugin for Velocity. Messages, PM, StaffChat, ...",
        url = "kozelkaricsi.hu",
        authors = {"Cyanoure"}
)
public class VeloChat {

    public String mutedMessage = "&cYou're muted for &6<amount> <unit>&c.";
    public String spamWarning = "&cDo not spam!";
    public String haveBeenMuted = "&c<sender> muted you for &6<amount> <unit>&c.";
    public String haveBeenMutedReason = "&c<sender> muted you for &6<amount> <unit>&c, reason: <reason>";
    public String muteStaffMessage = "&e<sender> &6muted &e<receiver> &6for &e<amount> <unit>&6.";
    public String muteStaffMessageReason = "&e<sender> &6muted &e<receiver> &6for &e<amount> <unit>&6, reason: <reason>";
    public String noReply = "&cThere is no player to reply to.";
    public String plrNotFound = "&cPlayer not found.";
    public String wrongUsage = "&cWrong usage. Usage: &6<usage>";
    public String noPerm = "&cYou do not have permission for that.";
    public String pmUsage = "/msg <playername> <message>";
    public String replyUsage = "/r <message>";
    public String alertUsage = "/alert <message>";
    public String muteUsage = "/mute <?s/?m/?h>";
    public String chatFormat = "&8>> &9<sender> &7[<sender_group>&7] &6: &r<message>";
    //private String chatFormat = "&8>> &7[<sender_prefix>&7] &9<sender> &7[<sender_suffix>&7] &6: &r<message>";
    public String pmSendFormat = "&7>> &8me &7-> &8<receiver> &6: &7<message>";
    public String pmReceiveFormat = "&7>> &8<sender> &7-> &8me &6: &7<message>";
    public String alertFormat = "&c>> &e<message>";

    public String second = "second";
    public String seconds = "seconds";
    public String minute = "minute";
    public String minutes = "minutes";
    public String hour = "hour";
    public String hours = "hours";

    public String serverString = "Server";

    public Boolean globalChat = false;

    public Boolean luckpermsIntegration = true;
    public Boolean spamFilter = true;
    public Boolean spamFilterRepeat = true;
    public int spamTimeoutMillis = 500;
    public int spamMuteSeconds = 60;
    public int maxSpamWarnings = 3;

    public final ProxyServer server;
    public final Logger logger;

    public static LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().character('&').hexCharacter('#').hexColors().build();


    private ArrayList<BasicPlayerData> players = new ArrayList<BasicPlayerData>();

    public String getMutedMessage(String format, int mutedSeconds){
        String msg = format;
        int amount = mutedSeconds;
        String unit = seconds;
        if(amount == 0 || amount == 1){
            unit = second;
        }
        if(amount >= 60){
            amount = (int)Math.round(amount/60);
            if(amount == 0 || amount == 1){
                unit = minute;
            }else{
                unit = minutes;
            }
        }
        if(amount >= 60){
            amount = (int)Math.round(amount/60);
            if(amount == 0 || amount == 1){
                unit = hour;
            }else{
                unit = hours;
            }
        }
        return msg.replaceAll("<amount>",String.valueOf(amount)).replaceAll("<unit>",unit);
    }

    public BasicPlayerData getPlayer(Player p){
        BasicPlayerData pd = null;
        int i = 0;
        while(pd == null && i < players.size()){
            BasicPlayerData d = players.get(i);
            if(d.p.getUniqueId().equals(p.getUniqueId())){
                pd = d;
            }
            i++;
        }
        return pd;
    }
    public void removePlayer(Player p){
        BasicPlayerData pd = getPlayer(p);
        do{
            players.remove(pd);
            pd = getPlayer(p);
        }while(pd != null);
    }
    public void addPlayer(Player p){
        BasicPlayerData pd = getPlayer(p);
        if(pd == null){
            //pd = new TestPlayerData(p);
            if(luckpermsIntegration){
                pd = new LPPlayerData(p);
            }else{
                pd = new BasicPlayerData(p);
            }
            players.add(pd);
        }
        logger.info("Players stored: "+String.valueOf(players.size()));
    }

    public static VeloChat instance;
    @Inject
    public VeloChat(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;
        this.server = server;
        this.logger = logger;

        new ConfigManager(dataDirectory);

        logger.info("I'm ready to the mission!");
    }

    public static boolean classCheck(String cl) {
        try {
            Class.forName(cl);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    private void addCommand(SimpleCommand cmd, String... aliases){
        CommandMeta commandMeta = commandManager.metaBuilder(aliases[0])
                .aliases((String[]) Arrays.copyOfRange(aliases,1,aliases.length))
                .plugin(this)
                .build();
        commandManager.register(commandMeta,cmd);
    }

    private CommandManager commandManager;
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        luckpermsIntegration = classCheck("net.luckperms.api.LuckPerms");
        commandManager = server.getCommandManager();

        addCommand(new MuteCommand(), "mute");
        addCommand(new PMCommand(),"msg","m","message","w","n","t","pm","emsg","epm","etell","whisper","ewhisper","r","reply","er","ereply");
        addCommand(new AlertCommand(), "alert");
    }

    @Subscribe
    public void onPlayerJoin(LoginEvent event){
        Player p = event.getPlayer();
        addPlayer(p);
    }

    @Subscribe
    public void onPlayerLeave(DisconnectEvent event){
        Player p = event.getPlayer();
        BasicPlayerData data = getPlayer(p);
        if(data != null && !data.isMuted()){
            removePlayer(p);
        }
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        Player p = event.getPlayer();
        String msg = event.getMessage();
        //p.sendMessage(Component.text(p.getUsername()+" --> "+msg));
        msg = msg.trim();
        if(msg != "") sendChat(p,msg);
        event.setResult(PlayerChatEvent.ChatResult.denied());
        /*p.getTabList().removeEntry(p.getUniqueId());
        p.getTabList().addEntry( TabListEntry.builder()
                .displayName(Component.text(">> "+p.getUsername()))
                .profile(p.getGameProfile())
                .gameMode(0)
                .tabList(p.getTabList())
                .build());*/
    }

    private String replaceMessage(String text, String message, Boolean colorsEnabled){
        if(!colorsEnabled){
            message = message.replaceAll("&","");
        }
        return text.replaceAll("<message>",message);
    }

    private String replaceMessage(String text, String message){
        return this.replaceMessage(text,message,false);
    }

    public void sendChat(Player p, String message){
        BasicPlayerData plrData;
        if(p != null){
            plrData = getPlayer(p);
        }else{
            plrData = new ServerPlayerData();
        }
        if(plrData != null && !plrData.isMuted() && plrData.p.hasPermission("velochat.chat")) {
            if(plrData.spamFilter(message)) {
                String msgOut = plrData.replaceSenderData(chatFormat);
                msgOut = replaceMessage(msgOut, message);
                TextComponent tc = VeloChat.serializer.deserialize(msgOut);
                //p.sendMessage(VeloChat.serializer.deserialize(msgOut));

                if (globalChat || p == null) {
                    server.sendMessage(tc);
                } else {
                    Collection<Player> players = p.getCurrentServer().get().getServer().getPlayersConnected();
                    players.forEach(plr -> {
                        plr.sendMessage(tc);
                    });
                }
            }else if(plrData.isMuted()){
                p.sendMessage(serializer.deserialize(getMutedMessage(mutedMessage,plrData.getMuteRemainingSeconds())));
            }else{
                p.sendMessage(serializer.deserialize(spamWarning));
            }
        }else if(plrData == null){
            logger.info("plrData is NULL!");
        }else if(plrData.isMuted()){
            p.sendMessage(serializer.deserialize(getMutedMessage(mutedMessage,plrData.getMuteRemainingSeconds())));
        }else if(!p.hasPermission("velocity.chat")){
            p.sendMessage(serializer.deserialize(noPerm));
        }
    }

    public Boolean sendPM(Player p1, Player p2, String message, Boolean returnNotFound){
        Boolean found = false;
        if(p1 != null && p2 != null){
            BasicPlayerData p1d;
            if(p1 != null){
                p1d = getPlayer(p1);
            }else{
                p1d = new ServerPlayerData();
            }
            BasicPlayerData p2d = getPlayer(p2);
            if(p1d != null && p2d != null){
                found = true;
                String msg1 = pmSendFormat;
                String msg2 = pmReceiveFormat;
                msg1 = p2d.replaceReceiverData(p1d.replaceSenderData(msg1));
                msg2 = p2d.replaceReceiverData(p1d.replaceSenderData(msg2));
                Boolean colored = p1.hasPermission("velochat.format") || p1 == null;
                msg1 = replaceMessage(msg1,message,colored);
                msg2 = replaceMessage(msg2,message,colored);
                if(p1 != null) p1.sendMessage(serializer.deserialize(msg1));
                p2.sendMessage(serializer.deserialize(msg2));
            }
        }
        if(returnNotFound && !found && p1 != null){
            if(p1 != null) p1.sendMessage(serializer.deserialize(plrNotFound));
        }
        return found;
    }
    public Boolean sendPM(Player p1, Player p2, String message){
        return sendPM(p1,p2,message,true);
    }
    public Boolean sendPM(Player p2, String message, Boolean returnNotFound){
        return sendPM(null,p2,message,returnNotFound);
    }
    public Boolean sendPM(Player p2, String message){
        return sendPM(p2, message, true);
    }
}
