package ch.epfl.sweng.erpa.services;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.dummy.database.DummyUserService;

import static ch.epfl.sweng.erpa.util.TestUtils.getUserProfile;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DummyUserProfileServiceTest {

    DataService<UserProfile> ups;

    @Before
    public void initDB() {
        ups = new DummyUserService(InstrumentationRegistry.getTargetContext());
    }


    @Test
    public void testAddedPersists() {
        String uid = "-1";
        UserProfile up = getUserProfile(uid);
        ups.saveOne(up);
        Optional<UserProfile> optUp = ups.getOne(uid);
        assertTrue(optUp.isPresent());
        assertEquals(up, optUp.get());
    }

    @Test
    public void testAllAdded() {
        int numTests = 500;
        List<UserProfile> userProfiles = new ArrayList<>(numTests);
        for (int i = 0; i < numTests; i++) {
            userProfiles.add(getUserProfile(String.valueOf(i)));
        }
        userProfiles.forEach(p -> ups.saveOne(p));

        assertTrue("Contains all added elements", ups.getAll().containsAll(userProfiles));
    }
}
