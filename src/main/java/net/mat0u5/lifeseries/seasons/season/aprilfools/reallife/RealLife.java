package net.mat0u5.lifeseries.seasons.season.aprilfools.reallife;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.seasons.season.thirdlife.ThirdLife;

public class RealLife extends ThirdLife {
    @Override
    public Seasons getSeason() {
        return Seasons.REAL_LIFE;
    }

    @Override
    public ConfigManager getConfig() {
        return new RealLifeConfig();
    }
}
