package ch.epfl.sweng.erpa.model;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;

public class ModelsTest {
    @Test
    public void testUserAuthToStringExcludesToken() {
        String secret = "casablanca";
        UserAuth ua = new UserAuth("User UUID", secret);
        assertFalse(ua.toString().contains(secret));
    }
}
