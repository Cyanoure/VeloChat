package hu.kozelkaricsi.minecraft.velochat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import hu.kozelkaricsi.minecraft.velochat.BasicPlayerData;
import hu.kozelkaricsi.minecraft.velochat.ServerPlayerData;
import hu.kozelkaricsi.minecraft.velochat.VeloChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.Arrays;
import java.util.Optional;

public class MuteCommand implements SimpleCommand {
    private String formatMuteTime(String format, int seconds){
        String msg = VeloChat.instance.getMutedMessage(format,seconds);
        return msg;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        Boolean usageOK = false;
        Boolean playerFound = false;
        if(sender.hasPermission("velochat.mute")){
            String[] args = invocation.arguments();
            if(args.length > 1){
                String username = args[0];
                String time = args[1];
                Optional<Player> targetOpt = VeloChat.instance.server.getPlayer(username);
                if(targetOpt != null){
                    Player target = targetOpt.get();
                    if(target != null){
                        BasicPlayerData pd = VeloChat.instance.getPlayer(target);
                        if(pd != null){
                            playerFound = true;
                            if(pd.Mute(time)){
                                usageOK = true;
                                BasicPlayerData senderPD;
                                if(sender instanceof Player){
                                    senderPD = VeloChat.instance.getPlayer((Player)sender);
                                    if(senderPD == null){
                                        senderPD = new BasicPlayerData(null);
                                    }
                                }else{
                                    senderPD = new ServerPlayerData();
                                }
                                int seconds = pd.getMuteRemainingSeconds();
                                String muteMSG = VeloChat.instance.haveBeenMuted;
                                String staffMSG = VeloChat.instance.muteStaffMessage;
                                if(args.length > 2){
                                    String reason = String.join(" ",(String[])Arrays.copyOfRange(args,2,args.length));
                                    muteMSG = VeloChat.instance.haveBeenMutedReason.replaceAll("<reason>",reason);
                                    staffMSG = VeloChat.instance.muteStaffMessageReason.replaceAll("<reason>",reason);
                                }
                                target.sendMessage(VeloChat.serializer.deserialize(senderPD.replaceSenderData(pd.replaceReceiverData(formatMuteTime(muteMSG,seconds)))));
                                TextComponent staffMessage = VeloChat.serializer.deserialize(senderPD.replaceSenderData(pd.replaceReceiverData(formatMuteTime(staffMSG,seconds))));
                                VeloChat.instance.server.getAllPlayers().forEach(p->{
                                    if(p.hasPermission("velochat.mute")){
                                        p.sendMessage(staffMessage);
                                    }
                                });
                            }
                        }else{
                            usageOK = true;
                        }
                    }else{
                        usageOK = true;
                    }
                }else{
                    usageOK = true;
                }
            }
            if(!usageOK){
                sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.wrongUsage.replaceAll("<usage>",VeloChat.instance.muteUsage)));
            }else if(!playerFound){
                sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.plrNotFound));
            }
        }else{
            sender.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.noPerm));
        }
    }
}
