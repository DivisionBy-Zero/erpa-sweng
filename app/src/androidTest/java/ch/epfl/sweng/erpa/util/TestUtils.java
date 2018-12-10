package ch.epfl.sweng.erpa.util;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Function;

import java.util.List;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UuidObject;
import ch.epfl.sweng.erpa.services.DataService;

public class TestUtils {
    public static final int numTests = 500;
    public static Game getGame(String gameUuid) {
        return new Game(
                gameUuid,
                "Sapphie",
                "The land of the Sapphie",
                0, 5,
                Game.Difficulty.CHILL,
                "Sapphtopia",
                false,
                Optional.of(-73),
                Optional.of(Integer.MAX_VALUE),
                "bepsi is gud",
                0.0,
                0.0
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

    public static <T extends UuidObject> void populateUUIDObjects(List<T> list, DataService<T> ds, Function<String, T> genfct) {
        for (int i = 0; i < numTests; i++) {

            T el = genfct.apply(String.valueOf(i));
            list.add(el);
            ds.saveOne(el);
        }
    }
}
