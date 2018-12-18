package ch.epfl.sweng.erpa.database.converter;

import android.arch.persistence.room.TypeConverter;

import com.annimon.stream.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;

public class Converters {

    @TypeConverter
    public static Integer toInteger(Optional<Integer> optional) {
        return optional.orElse(null);
    }

    @TypeConverter
    public static Optional<Integer> fromInteger(Integer value) {
        return Optional.ofNullable(value);
    }


    @TypeConverter
    public static PlayerJoinGameRequest.RequestStatus toRequestStatus(Integer value) {
        return requestStatusConversionMap.get(value);
    }

    @TypeConverter
    public static Integer fromRequestStatus(PlayerJoinGameRequest.RequestStatus requestStatus) {
        return requestStatusConversionMap.inverse().get(requestStatus);
    }


    @TypeConverter
    public static Game.GameStatus toGameStatus(Integer value) {
        return gameStatusConversionMap.get(value);
    }
    @TypeConverter
    public static Integer fromGameStatus(Game.GameStatus gameStatus) {
        return gameStatusConversionMap.inverse().get(gameStatus);
    }

    @TypeConverter
    public static Integer fromDifficulty(Game.Difficulty difficulty) {
        return difficultyConversionMap.inverse().get(difficulty);
    }

    @TypeConverter
    public static Game.Difficulty toDifficulty(Integer value) {
        return difficultyConversionMap.get(value);
    }


    private static final BiMap<Integer, PlayerJoinGameRequest.RequestStatus> requestStatusConversionMap = genRequestStatusConversionMap();
    private static final BiMap<Integer, Game.GameStatus> gameStatusConversionMap = genGameStatusConversionMap();
    private static final BiMap<Integer, Game.Difficulty> difficultyConversionMap = genDifficultyConversionMap();

    private static BiMap<Integer, Game.Difficulty> genDifficultyConversionMap() {
        BiMap<Integer, Game.Difficulty> result = HashBiMap.create();
        result.put(10, Game.Difficulty.NOOB);
        result.put(20, Game.Difficulty.CHILL);
        result.put(30, Game.Difficulty.HARD);
        assert conversionMapHasAll(result, Game.Difficulty.values());
        return result;
    }

    private static BiMap<Integer, Game.GameStatus> genGameStatusConversionMap() {
        BiMap<Integer, Game.GameStatus> result = HashBiMap.create();
        result.put(10, Game.GameStatus.CANCELED);
        result.put(20, Game.GameStatus.CONFIRMED);
        result.put(30, Game.GameStatus.CREATED);
        result.put(40, Game.GameStatus.FINISHED);
        result.put(50, Game.GameStatus.IN_PROGRESS);
        assert conversionMapHasAll(result, Game.GameStatus.values());
        return result;
    }

    private static BiMap<Integer, PlayerJoinGameRequest.RequestStatus> genRequestStatusConversionMap() {
        BiMap<Integer, PlayerJoinGameRequest.RequestStatus> result = HashBiMap.create();
        result.put(10, PlayerJoinGameRequest.RequestStatus.CONFIRMED);
        result.put(20, PlayerJoinGameRequest.RequestStatus.HAS_QUIT);
        result.put(30, PlayerJoinGameRequest.RequestStatus.REJECTED);
        result.put(40, PlayerJoinGameRequest.RequestStatus.REMOVED);
        result.put(50, PlayerJoinGameRequest.RequestStatus.REQUEST_TO_JOIN);
        assert conversionMapHasAll(result, PlayerJoinGameRequest.RequestStatus.values());
        return result;
    }

    private static <T> boolean conversionMapHasAll(BiMap<?, T> result, T[] values) {
        for (T t : values) {
            if (!result.inverse().containsKey(t)) {
                return false;
            }
        }

        return false;
    }
}
