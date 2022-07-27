package hu.kozelkaricsi.minecraft.velochat;

import com.velocitypowered.api.proxy.Player;

public class ServerPlayerData extends BasicPlayerData{

    public ServerPlayerData() {
        super(null);
    }

    @Override
    public String getUsername() {
        return VeloChat.instance.serverString;
    }

    @Override
    public String getGroup() {
        return "";
    }

    @Override
    public String getServer() {
        return null;
    }

    @Override
    public String getPrefix(){
        return "";
    }

    @Override
    public String getSuffix(){
        return "";
    }
}
