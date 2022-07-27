package hu.kozelkaricsi.minecraft.velochat.commands;

import com.velocitypowered.api.command.SimpleCommand;
import hu.kozelkaricsi.minecraft.velochat.VeloChat;

public class AlertCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if(invocation.source().hasPermission("velochat.alert")) {
            String[] args = invocation.arguments();
            if (args.length > 0) {
                VeloChat.instance.server.sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.alertFormat.replaceAll("<message>", String.join(" ", args))));
            } else {
                invocation.source().sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.wrongUsage.replaceAll("<usage>", VeloChat.instance.alertUsage)));
            }
        }else{
            invocation.source().sendMessage(VeloChat.serializer.deserialize(VeloChat.instance.noPerm));
        }
    }

    /*@Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velochat.alert");
    }*/
}
