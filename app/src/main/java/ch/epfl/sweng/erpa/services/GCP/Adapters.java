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
    private abstract static class AbstractDictBasedTypeAdapter<T, F> extends TypeAdapter<T> {
        @NonNull final TypeAdapter<F> converter;
        private final BiMap<F, T> translationTable = mkTranslationTable();

        abstract BiMap<F, T> mkTranslationTable();

        @Override public void write(JsonWriter out, T value) throws IOException {
            converter.write(out, translationTable.inverse().get(value));
        }

        @Override public T read(JsonReader in) throws IOException {
            return translationTable.get(converter.read(in));
        }
    }

    public static class PlayerJoinRequestStatusTypeAdapter extends AbstractDictBasedTypeAdapter<PlayerJoinGameRequest.RequestStatus, Integer> {
        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() == PlayerJoinGameRequest.RequestStatus.class) {
                    return (TypeAdapter<T>) new PlayerJoinRequestStatusTypeAdapter(gson.getAdapter(Integer.class));
                }
                return null;
            }
        };

        PlayerJoinRequestStatusTypeAdapter(TypeAdapter<Integer> converter) {
            super(converter);
        }

        BiMap<Integer, PlayerJoinGameRequest.RequestStatus> mkTranslationTable() {
            BiMap<Integer, PlayerJoinGameRequest.RequestStatus> result = HashBiMap.create();
            result.put(1, PlayerJoinGameRequest.RequestStatus.REQUEST_TO_JOIN);
            result.put(2, PlayerJoinGameRequest.RequestStatus.CONFIRMED);
            result.put(3, PlayerJoinGameRequest.RequestStatus.REJECTED);
            result.put(4, PlayerJoinGameRequest.RequestStatus.REMOVED);
            result.put(5, PlayerJoinGameRequest.RequestStatus.HAS_QUIT);
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    public static class GameDifficultyTypeAdapter extends AbstractDictBasedTypeAdapter<Game.Difficulty, Integer> {
        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() == Game.Difficulty.class) {
                    return (TypeAdapter<T>) new GameDifficultyTypeAdapter(gson.getAdapter(Integer.class));
                } else {
                    return null;
                }
            }
        };

        GameDifficultyTypeAdapter(TypeAdapter<Integer> converter) {
            super(converter);
        }

        protected BiMap<Integer, Game.Difficulty> mkTranslationTable() {
            BiMap<Integer, Game.Difficulty> result = HashBiMap.create();
            result.put(10, Game.Difficulty.NOOB);
            result.put(20, Game.Difficulty.CHILL);
            result.put(30, Game.Difficulty.HARD);
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    public static class GameStatusTypeAdapter extends AbstractDictBasedTypeAdapter<Game.GameStatus, Integer> {
        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() == Game.GameStatus.class) {
                    return (TypeAdapter<T>) new GameStatusTypeAdapter(gson.getAdapter(Integer.class));
                } else {
                    return null;
                }
            }
        };

        GameStatusTypeAdapter(TypeAdapter<Integer> converter) {
            super(converter);
        }

        protected BiMap<Integer, Game.GameStatus> mkTranslationTable() {
            BiMap<Integer, Game.GameStatus> result = HashBiMap.create();
            result.put(1, Game.GameStatus.CREATED);
            result.put(2, Game.GameStatus.CONFIRMED);
            result.put(3, Game.GameStatus.CANCELED);
            result.put(4, Game.GameStatus.IN_PROGRESS);
            result.put(5, Game.GameStatus.FINISHED);
            return result;
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
