package ch.epfl.sweng.erpa.database;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ERPADatabaseTest {

    private ERPADatabase database;
    @Before
    public void createDB() {
        Context ctx = InstrumentationRegistry.getContext();
        database = Room.inMemoryDatabaseBuilder(ctx, ERPADatabase.class).build();
    }

    @After
    public void deleteDB() {
        database.close();
    }

}
