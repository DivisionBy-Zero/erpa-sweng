package ch.epfl.sweng.erpa.services.dummy.database;


import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import ch.epfl.sweng.erpa.model.Game;

@TypeConverters({Converters.class})
class Converters
{
    @TypeConverter
    public static Game fromString(String val)
    {
        Type gameType = new TypeToken<Game>(){}.getType();
        Gson gson = new Gson();
        return gson.fromJson(val, gameType);
    }

    @TypeConverter
    public static String fromGame(Game g)
    {
        return (new Gson()).toJson(g);
    }
}