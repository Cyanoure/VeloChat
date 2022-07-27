package hu.kozelkaricsi.minecraft.velochat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import hu.kozelkaricsi.minecraft.velochat.BasicPlayerData;
import hu.kozelkaricsi.minecraft.velochat.ServerPlayerData;
import hu.kozelkaricsi.minecraft.velochat.VeloChat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PMCommand implements SimpleCommand {
    public Boolean isReply(String cmd){
        cmd = cmd.toLowerCase();
        return cmd.startsWith("r") || cmd.startsWith("er");
    }
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        if(sender.hasPermission("velochat.msg")){
            String cmd = invocation.alias();
            String[] args = invocation.arguments();

            int muted = 0;
            Player senderPlayer = null;
            BasicPlayerData pd = new ServerPlayerData();
            if(sender instanceof Player){
                senderPlayer = (Player)sender;
                pd = VeloChat.instance.getPlayer(senderPlayer);
                if(pd == null){
                    pd = new BasicPlayerData(null);
                }
            }

            Boolean warnSpam = !pd.spamFilter(cmd+String.join(" ",args));
            if(pd.isMuted()){
                muted = pd.getMuteRemainingSeconds();
            }else if(warnSpam){
                sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.spamWarning));
            }

            BasicPlayerData tpd = null;

            Boolean reply = isReply(cmd);

            if(muted == 0 && !warnSpam) {
                if (reply) {
                    reply = true;
                    if (args.length > 0) {
                        if(pd.prevPM != null && pd.prevPM.p != null && VeloChat.instance.getPlayer(pd.prevPM.p) != null){
                            tpd = pd.prevPM;
                        }else{
                            sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.noReply));
                        }
                    } else {
                        sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.wrongUsage.replaceAll("<usage>", VeloChat.instance.replyUsage)));
                    }
                } else if (args.length > 1) {
                    Boolean found = false;
                    Optional<Player> po = VeloChat.instance.server.getPlayer(args[0]);
                    if(po != null){
                        Player p = po.get();
                        if(p != null){
                            BasicPlayerData tpd2 = VeloChat.instance.getPlayer(p);
                            if(tpd2 != null){
                                found = true;
                                tpd = tpd2;
                            }
                        }
                    }
                    if(!found){
                        sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.plrNotFound));
                    }
                } else {
                    sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.wrongUsage.replaceAll("<usage>", VeloChat.instance.pmUsage)));
                }
                if(tpd != null){
                    String msg = "";
                    if(reply){
                        msg = String.join(" ",args);
                    }else{
                        msg = String.join(" ",(String[]) Arrays.copyOfRange(args,1,args.length));
                    }
                    if(senderPlayer != null && pd != null){
                        VeloChat.instance.sendPM(pd.p,tpd.p,msg);
                        pd.prevPM = tpd;
                        tpd.prevPM = pd;
                    }else{
                        VeloChat.instance.sendPM(tpd.p,msg);
                        tpd.prevPM = null;
                    }
                }
            }else if(muted > 0){
                sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.getMutedMessage(VeloChat.instance.mutedMessage, muted)));
            }
        }else{
            sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.noPerm));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation){
        ArrayList<String> out = new ArrayList<String>();
        if(!isReply(invocation.alias()) && invocation.arguments().length == 0){
            VeloChat.instance.server.getAllPlayers().forEach(p->{
                out.add(p.getUsername());
            });
        }
        return out;
    }
}
