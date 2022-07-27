package hu.kozelkaricsi.minecraft.velochat;

import com.velocitypowered.api.proxy.Player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class BasicPlayerData{
    public BasicPlayerData prevPM = null;
    public Player p;
    public UUID uuid;
    private LocalDateTime muteDate = LocalDateTime.now().minusSeconds(1);
    private int spamWarnings = 0;
    private LocalDateTime latestMessage = LocalDateTime.now();
    private String prevMessage = "";
    public BasicPlayerData(Player player){
        this.p = player;
        if(p != null) this.uuid = p.getUniqueId();
    }

    public Boolean spamFilter(String msg){
        if(!VeloChat.instance.spamFilter) return true;
        LocalDateTime now = LocalDateTime.now();
        int delay = (int)Math.round(ChronoUnit.SECONDS.between(latestMessage,now));
        latestMessage = now;
        if(delay*1000 <= VeloChat.instance.spamTimeoutMillis || (VeloChat.instance.spamFilterRepeat && msg != null && msg.toLowerCase().equals(this.prevMessage.toLowerCase()))){
            spamWarnings++;
            prevMessage = msg == null ? "" : msg;
        }else{
            spamWarnings = 0;
            prevMessage = msg == null ? "" : msg;
            return true;
        }
        if(spamWarnings >= VeloChat.instance.maxSpamWarnings){
            spamWarnings = 0;
            Mute(VeloChat.instance.spamMuteSeconds);
        }
        return false;
    }

    public Boolean spamFilter(){
        return spamFilter(null);
    }

    public String getUsername() {
        return p != null ? p.getUsername().replaceAll("&","") : "???";
    }

    public String getGroup() {
        return "";
    }

    public String getServer() {
        return p != null ? p.getCurrentServer().toString() : null;
    }

    public String getPrefix(){
        return "";
    }

    public String getSuffix(){
        return "";
    }

    protected String replaceData(String pre, String text){
        return text.replaceAll("<"+pre+">",this.getUsername())
                .replaceAll("<"+pre+"_group>",this.getGroup())
                .replaceAll("<"+pre+"_server>",this.getServer())
                .replaceAll("<"+pre+"_prefix>",this.getPrefix())
                .replaceAll("<"+pre+"_suffix>",this.getSuffix())
                ;
    }

    public String replaceSenderData(String text) {
        return this.replaceData("player",this.replaceData("sender",text));
    }

    public String replaceReceiverData(String text) {
        return this.replaceData("player",this.replaceData("receiver",text));
    }

    public Boolean isMuted() {
        return getMuteRemainingSeconds() > 0;
    }

    public Boolean canSend(String message) {
        return true;
    }

    public LocalDateTime mutedUntil() {
        return muteDate;
    }

    public int getMuteRemainingSeconds(){
        LocalDateTime now = LocalDateTime.now();
        return (int)Math.round(ChronoUnit.SECONDS.between(now,mutedUntil()));
    }

    public void Mute(int seconds) {
        this.muteDate = LocalDateTime.now().plusSeconds(seconds);
    }

    public Boolean Mute(String time) {
        //ArrayList<String> snumbers = new ArrayList<>(List.of("0","1","2","3","4","5","6","7","8","9"));
        int seconds = -1;
        //String unit = time.substring(time.length()-1,time.length()-2).toLowerCase(); // wtf? fáradt vagyok
        String unit = time.substring(time.length()-1).toLowerCase();
        switch (unit){
            case "m":
                try{
                    seconds = Integer.parseInt(time.substring(0,time.length()-1))*60;
                }catch (Exception e){
                    seconds = -1;
                }
                break;
            case "h":
                try{
                    seconds = Integer.parseInt(time.substring(0,time.length()-1))*60*60;
                }catch (Exception e){
                    seconds = -1;
                }
                break;
            default:
                if(unit.equals("s")){
                    try{
                        seconds = Integer.parseInt(time.substring(0,time.length()-1));
                    }catch (Exception e){
                        seconds = -1;
                    }
                }/*else if(snumbers.contains(unit)){

                }*/
                else {
                    try{
                        seconds = Integer.parseInt(time);
                    }catch (Exception e){
                        seconds = -1;
                    }
                }
                break;
        }

        if(seconds >= 0){
            this.Mute(seconds);
            return true;
        }

        return false;
    }
}
