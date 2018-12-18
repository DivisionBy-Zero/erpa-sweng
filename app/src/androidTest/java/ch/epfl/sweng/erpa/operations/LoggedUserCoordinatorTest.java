package ch.epfl.sweng.erpa.operations;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.activities.DependencyConfigurationAgnosticTest;
import ch.epfl.sweng.erpa.model.UserProfile;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoggedUserCoordinatorTest extends DependencyConfigurationAgnosticTest {
    @Inject OptionalDependencyManager optionalDependencyManager;
    @Inject LoggedUserCoordinator loggedUserCoordinator;

    @Test
    public void testSignUpLoginRunsWithoutException() throws Throwable {
        assertFalse(optionalDependencyManager.get(LoggedUser.class).isPresent());
        AsyncTaskService asyncTaskService = new AsyncTaskService();
        UserProfile up = new UserProfile("", true, true);
        CountDownLatch lock = new CountDownLatch(1);
        loggedUserCoordinator.trySignUp(asyncTaskService,
            "username", "password", up, lock::countDown, exc -> fail());

        lock.await(10000, TimeUnit.MILLISECONDS);
        assertTrue(optionalDependencyManager.get(LoggedUser.class).isPresent());
        loggedUserCoordinator.logout();
        assertFalse(optionalDependencyManager.get(LoggedUser.class).isPresent());
    }
}
