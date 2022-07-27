package hu.kozelkaricsi.minecraft.velochat;

import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;

public class LPPlayerData extends BasicPlayerData{
    private LuckPerms lpapi;
    private User user;
    public LPPlayerData(Player player) {
        super(player);
        lpapi = LuckPermsProvider.get();
        user = getUser();
    }
    private PlayerAdapter getAdapter(){
        return lpapi.getPlayerAdapter(Player.class);
    }
    private User getUser(){
        return getAdapter().getUser(p);
    }

    @Override
    public String getGroup() {
        String name = user.getPrimaryGroup();
        String dpname = lpapi.getGroupManager().getGroup(name).getDisplayName();
        return dpname == null ? name : dpname;
    }

    private CachedMetaData getCachedMeta(){
        CachedMetaData meta = user.getCachedData().getMetaData();
        return meta;
    }

    @Override
    public String getPrefix(){
        CachedMetaData meta = getCachedMeta();
        if(meta != null){
            String prefix = meta.getPrefix();
            if(prefix != null){
                return prefix;
            }
        }
        return "";
    }

    @Override
    public String getSuffix(){
        CachedMetaData meta = getCachedMeta();
        if(meta != null){
            String suffix = meta.getSuffix();
            if(suffix != null){
                return suffix;
            }
        }
        return "";
    }
}
