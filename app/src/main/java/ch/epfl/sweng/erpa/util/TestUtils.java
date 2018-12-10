package ch.epfl.sweng.erpa.util;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Function;

import java.util.List;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UuidObject;
import ch.epfl.sweng.erpa.services.DataService;

/**
 * This class is merely to avoid duplicating util code in the different test suites.
 * It should never be used by the application. In that case consider a top-level class.
 */
public class TestUtils {
    public static final int numTests = 500;

    private TestUtils() {
    }

    public static Game getGame(String gid) {
        return new Game(
            gid,
            "Sapphie",
            "The land of the Sapphie",
            0, 5,
            Game.Difficulty.CHILL,
            "Sapphtopia",
            false,
            Optional.of(Integer.MIN_VALUE),
            Optional.of(Integer.MAX_VALUE),
            "Pepsi is Good",
            0.0, 0.0
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

    public static class Fuse {
        public boolean ignited = false;

        public void ignite() {
            ignited = true;
        }
    }
}
