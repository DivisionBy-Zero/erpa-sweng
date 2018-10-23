package ch.epfl.sweng.erpa.util;

import com.annimon.stream.Optional;

import java.util.HashSet;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.UserProfile;

public class TestUtils {
    public static Game getGame(String gid) {
        return new Game(
                gid,
                "Sapphie",
                new HashSet<String>(),
                "The land of the Sapphie",
                0,5,
                Game.Difficulty.CHILL,
                "Sapphtopia",
                Game.OneshotOrCampaign.ONESHOT,
                Optional.<Integer>of(-73),
                Optional.<Integer>of(Integer.MAX_VALUE),
                "bepsi is gud"
        );
    }
    public static UserProfile getUserProfile(String uid)
    {
        return new UserProfile(uid,
                "Sapphie",
                "",
                UserProfile.Experience.Expert,
                false,
                true);
    }
}
