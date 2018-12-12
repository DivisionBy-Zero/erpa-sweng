package ch.epfl.sweng.erpa.services.GCP;

import com.annimon.stream.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

class Adapters {
    private Adapters() {
    }

    @SuppressWarnings("unchecked")
    @RequiredArgsConstructor
    public static class GameDifficultyTypeAdapter extends TypeAdapter<Game.Difficulty> {
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

    @SuppressWarnings("unchecked")
    @RequiredArgsConstructor
    public static class OptionalTypeAdapter<T> extends TypeAdapter<Optional<T>> {
        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override public <U> TypeAdapter<U> create(Gson gson, TypeToken<U> type) {
                Class<U> raw = (Class<U>) type.getRawType();
                if (raw != Optional.class) {
                    return null;
                } else {
                    ParameterizedType parameterizedType = (ParameterizedType) type.getType();
                    Type innerType = parameterizedType.getActualTypeArguments()[0];
                    TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(innerType));
                    return new OptionalTypeAdapter(adapter);
                }
            }
        };

        @android.support.annotation.NonNull private TypeAdapter<T> innerAdapter;

        @Override public void write(JsonWriter out, Optional<T> value) throws IOException {
            if (value.isPresent()) {
                innerAdapter.write(out, value.get());
            } else {
                out.nullValue();
            }
        }

        @Override public Optional<T> read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.skipValue();
                return Optional.empty();
            } else {
                return Optional.ofNullable(innerAdapter.read(in));
            }
        }
    }
}
