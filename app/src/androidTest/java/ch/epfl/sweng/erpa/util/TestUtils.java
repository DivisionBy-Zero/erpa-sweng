package ch.epfl.sweng.erpa.util;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Function;

import java.util.HashSet;
import java.util.List;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UuidObject;
import ch.epfl.sweng.erpa.services.DataService;

public class TestUtils {
    public static Game getGame(String gid) {
        return new Game(
                gid,
                "Sapphie",
                new HashSet<String>(),
                "The land of the Sapphie",
                0, 5,
                Game.Difficulty.CHILL,
                "Sapphtopia",
                Game.OneshotOrCampaign.ONESHOT,
                Optional.<Integer>of(-73),
                Optional.<Integer>of(Integer.MAX_VALUE),
                "bepsi is gud"
        );
    }

    public static UserProfile getUserProfile(String uid) {
        return new UserProfile(uid,
                "Sapphie",
                "",
                UserProfile.Experience.Expert,
                false,
                true);
    }

    public static <T extends UuidObject> void unnecessaryCodeClimateMethod(List<T> list, DataService<T> ds, Function<String, T> genfct, int numIter) {
        for (int i = 0; i < numIter; i++) {

            T el = genfct.apply(String.valueOf(i));
            list.add(el);
            ds.saveOne(el);
        }
    }
}
