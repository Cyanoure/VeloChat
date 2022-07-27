package hu.kozelkaricsi.minecraft.velochat;

import com.velocitypowered.api.proxy.Player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestPlayerData extends BasicPlayerData{
    public TestPlayerData(Player player) {
        super(player);
    }

    @Override
    public String getGroup() {
        return "TestGroup";
    }
}
