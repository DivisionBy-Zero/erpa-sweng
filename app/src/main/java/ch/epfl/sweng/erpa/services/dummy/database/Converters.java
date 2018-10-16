package ch.epfl.sweng.erpa.services.dummy.database;


import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import com.annimon.stream.Optional;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.GameParticipants;

@TypeConverters({Converters.class})
class Converters
{

    @TypeConverter
    public GameParticipants fromStringGp(String val) {return fromString(val);}
    @TypeConverter
    public String toStringGp(GameParticipants gp) {return fromObject(gp);}
    @TypeConverter
    public String toStringDiff(Game.Difficulty diff) { return  fromObject(diff);}
    @TypeConverter
    public Game.Difficulty fromStringDiff(String val) {return  fromString(val);}
    @TypeConverter
    public String toStringGameType(Game.OneshotOrCampaign type){return fromObject(type);}
    @TypeConverter
    public Game.OneshotOrCampaign fromStringType(String val){return fromString(val);}
    @TypeConverter
    public Optional<Integer> fromStringOpt(String val){return  fromString(val);}
    @TypeConverter
    public String toStringOpt(Optional<Integer> val){return fromObject(val);}

    static <T> String fromObject(T val)
    {
        return (new Gson()).toJson(val);
    }
    private static <T> T fromString(String val)
    {
        Type t = new TypeToken<T>(){}.getType();
        return (new Gson()).fromJson(val,t);
    }

}