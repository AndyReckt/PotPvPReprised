package net.frozenorb.potpvp.adapter.scoreboard;


import net.frozenorb.potpvp.util.scoreboard.config.ScoreboardConfiguration;
import net.frozenorb.potpvp.util.scoreboard.construct.TitleGetter;
import org.apache.commons.lang.StringEscapeUtils;

public class ScoreboardAdapter extends ScoreboardConfiguration {

    public ScoreboardAdapter() {
        this.setTitleGetter(
<<<<<<< HEAD
                new TitleGetter("&c&lRefine &7" + StringEscapeUtils.unescapeJava("\u2758") +" &fPractice"));
=======
                new TitleGetter("&bCausal &7" + StringEscapeUtils.unescapeJava("\u2758") +" &fPractice"));
>>>>>>> master
        this.setScoreGetter(
                new MultiplexingScoreGetter(
                        new MatchScoreGetter(),
                        new LobbyScoreGetter(),
                        new GameScoreGetter()
                ));
    }

}
