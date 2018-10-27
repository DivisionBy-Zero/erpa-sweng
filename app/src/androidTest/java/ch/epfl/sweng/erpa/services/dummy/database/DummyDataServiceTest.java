package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import ch.epfl.sweng.erpa.model.UuidObject;
import lombok.Data;

import static ch.epfl.sweng.erpa.util.TestUtils.populateUUIDObjects;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DummyDataServiceTest {
    private DummyDataTester dts;

    @Before
    public void initDataService() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        dts = new DummyDataTester(ctx);
    }

    @After
    public void removeStuff() {
        dts.removeAll();
    }

    @Test
    public void testAddedPersists() {
        TestData td = new TestData();
        dts.saveOne(td);

        Optional<TestData> res = dts.getOne(td.getUuid());
        assertTrue(res.isPresent());
        assertEquals(td, res.get());
    }

    @Test
    public void testAddedAll() {
        final int numTests = 500;
        List<TestData> list = new ArrayList<>(numTests);
        populateUUIDObjects(list, dts, (str) -> new TestData());
        Set<TestData> all = dts.getAll();
        assertTrue(all.containsAll(list));
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionOnIllegalSave() {
        TestData evilData = new TestData();
        File newFolder = new File(dts.getDataDir(), evilData.getUuid() + DummyDataService.SAVED_DATA_FILE_EXTENSION);
        newFolder.delete();
        newFolder.mkdir();
        dts.saveOne(evilData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnGet() {
        File evilFile = new File(dts.getDataDir(), "Evil");
        evilFile.delete();
        dts.getFileFetcher().apply(evilFile);
    }

    @Test
    public void testDeletion() {
        assertTrue(dts.removeAll());
    }

    @Data
    static class TestData implements UuidObject {
        private Integer x;
        private Integer y;

        TestData() {
            Random rnd = new Random();
            x = rnd.nextInt();
            y = rnd.nextInt();
        }

        @Override
        public String getUuid() {
            return String.valueOf(hashCode());
        }
    }

    static class DummyDataTester extends DummyDataService<TestData> {

        DummyDataTester(Context ctx) {
            super(ctx, TestData.class);
        }

        @Override
        public String dataFolder() {
            return "test_data";
        }
    }
}
