package ch.epfl.sweng.erpa.services.GCP;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.mockito.Mockito.mock;

public class LazyGameStreamTest {
    @Test
    public void testConstructor() {
        new LazyGameStream(50, new HashMap<>()).loadAhead(0);
    }
}
