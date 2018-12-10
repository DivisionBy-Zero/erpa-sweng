package ch.epfl.sweng.erpa.adapters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import ch.epfl.sweng.erpa.model.Game;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class GameDifficultyTypeAdapter extends TypeAdapter<Game.Difficulty> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() == Game.Difficulty.class) {
                return (TypeAdapter<T>) new GameDifficultyTypeAdapter(gson.getAdapter(Integer.class));
            } else {
                return null;
            }
        }
    };

    @NonNull final TypeAdapter<Integer> intConverter;
    private final BiMap<Integer, Game.Difficulty> integerDifficultyBiMap = constructDifficultyToIntegerBiMap();

    private static BiMap<Integer, Game.Difficulty> constructDifficultyToIntegerBiMap() {
        BiMap<Integer, Game.Difficulty> result = HashBiMap.create();
        result.put(10, Game.Difficulty.NOOB);
        result.put(20, Game.Difficulty.CHILL);
        result.put(30, Game.Difficulty.HARD);
        return result;
    }

    @Override public void write(JsonWriter out, Game.Difficulty value) throws IOException {
        //noinspection ConstantConditions
        int intVal = integerDifficultyBiMap.inverse().get(value);

        intConverter.write(out, intVal);
    }

    @Override public Game.Difficulty read(JsonReader in) throws IOException {
        int intVal = intConverter.read(in);
        return integerDifficultyBiMap.get(intVal);
    }
}
