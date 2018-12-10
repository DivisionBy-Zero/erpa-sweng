package ch.epfl.sweng.erpa.services.dummy.database;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.activities.DependencyConfigurationAgnosticTest;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.UserManagementService;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DummyUserManagementServiceTest extends DependencyConfigurationAgnosticTest {
    @Inject UserManagementService underTest;

    @Test
    public void testUserProfilePersistance() throws IOException, ServerException {
        List<UserProfile> userProfiles = new ArrayList<>();
        for (int i = 0; i < 100; ++i) {
            String uuid = underTest.registerNewUsername("User " + Integer.toString(i));
            UserProfile up = new UserProfile(uuid, true, true);
            userProfiles.add(underTest.registerUserProfile(up));
        }

        for (UserProfile up : userProfiles)
            assertTrue(underTest.getUserProfile(up.getUuid()).isPresent());
    }

    @Test(expected = ServerException.class)
    public void registeringUnknownUserUuidThrows() throws Throwable {
        try {
            underTest.registerUserProfile(new UserProfile("Unexistant UUID", true, true));
        } catch (Throwable exc) {
            unwrapException(exc);
        }
    }

    @Test
    public void uuidAndUsername() throws IOException, ServerException {
        String username = "Kokiri";
        String uuid = underTest.registerNewUsername(username);
        assertEquals(username, underTest.getUsernameFromUserUuid(uuid).get().getUsername());
        assertEquals(uuid, underTest.getUuidForUsername(username));
    }

    private void unwrapException(Throwable exc) throws Throwable {
        if (UndeclaredThrowableException.class.isInstance(exc))
            this.unwrapException(exc.getCause());
        else if (InvocationTargetException.class.isInstance(exc))
            this.unwrapException(exc.getCause());
        else
            throw exc;
    }
}
