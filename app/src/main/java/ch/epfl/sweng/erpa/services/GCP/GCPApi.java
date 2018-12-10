package ch.epfl.sweng.erpa.services.GCP;

import java.util.List;
import java.util.Map;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.Username;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

class GCPApi {
    public interface GameInterface {
        /**
         * Sends a get request for a number of games to the server
         *
         * @param orderingsAndSortings the different search options
         * @param from                 the first game to show
         * @param count                the number of games to put into the list
         * @return a call to the fetched game list
         */
        @GET("/games")
        Call<List<Game>> getGames(@QueryMap Map<String, String> orderingsAndSortings, @Query("from") int from, @Query("count") int count);

        /**
         * Sends a get request for a single game to the server
         *
         * @param gameUuid            the game for which to look
         * @param authorizationHeader the authorizationHeader, can be the empty string for none
         * @return a call to a game
         */
        @GET("/games/uuid/{uuid}")
        Call<Game> getGame(@Path("uuid") String gameUuid, @Header("Authorization") String authorizationHeader);

        /**
         * Saves a <b>new</b> game to the server
         *
         * @param game                the game to save
         * @param authorizationHeader the authorization
         * @return the game, as it is stored in the server
         */
        @POST("/games")
        Call<Game> saveGame(@Body Game game, @Header("Authorization") String authorizationHeader);


        /**
         * Updates an existing game in the server
         *
         * @param gameUuid            the Uuid of the game
         * @param game                the game to save
         * @param authorizationHeader the authorization. cannot be empty
         * @return the server's response (the game)
         */
        @POST("/games/uuid/{uuid}")
        Call<Game> updateGame(@Path("uuid") String gameUuid, @Body Game game, @Header("Authorization") String authorizationHeader);

        @GET("/games/participants/{gameUuid}")
        Call<List<PlayerJoinGameRequest>> getGamePlayerJoinRequests(@Path("gameUuid") String gameUuid, @Header("Authorization") String authorizationHeader);

        @POST("/games/join/{gameUuid}")
        Call<PlayerJoinGameRequest> joinGame(@Path("gameUuid") String gameUuid, @Header("Authorization") String authorizationHeader);
    }

    public interface UserInterface {
        /**
         * Register username
         *
         * @param username the user's username
         * @return the user's UUID
         */
        @POST("/users/newuser/{username}")
        Call<ResponseBody> registerUsername(@Path("username") String username);

        /**
         * Registers an authentication object
         *
         * @param userAuth the object
         * @return nothing
         */
        @POST("/users/register_auth") Call<Void> registerAuth(@Body UserAuth userAuth);

        /**
         * Saves a <b>new</b> user
         *
         * @param userProfile         the user profile
         * @param authorizationHeader the authorization, cannot be empty
         * @return the user profile, as it is on the server
         */
        @POST("/users")
        Call<UserProfile> saveUser(@Body UserProfile userProfile, @Header("Authorization") String authorizationHeader);

        /**
         * Retrieves the UserUuid of a given username
         * @param username the username, cannot be empty
         * @return the UserUuid
         */
        @GET("/users/user/{username}")
        Call<ResponseBody> getUuidFromUsername(@Path("username") String username);

        /**
         * Retrieves the last username of the given UserUuid
         * @param userUuid the UserUuid, cannot be empty
         * @return the latest Username associated to this UserUuid
         */
        @GET("/users/username/{user_uuid}")
        Call<Username> getUsernameFromUuid(@Path("user_uuid") String userUuid);

        /**
         * Registers a new User profile
         * @param userProfile the new userProfile
         * @return the updated UserProfile, as it is on the server
         */
        @POST("/users")
        Call<UserProfile> registerUser(@Body UserProfile userProfile);

        /**
         * Updates a User profile
         * @param userProfile the updated user profile to apply, cannot be empty
         * @param userUuid the UserUuid of the userProfile, cannot be empty
         * @param authorizationHeader the authorization, cannot be empty
         * @return the updated UserProfile, as it is on the server
         */
        @POST("/users/uuid/{uuid}")
        Call<UserProfile> updateUser(@Body UserProfile userProfile, @Path("uuid") String userUuid, @Header("Authorization") String authorizationHeader);

        /**
         * Gets a user profile
         *
         * @param userUuid the user's uuid
         * @return the user, as is stored on the server
         */
        @GET("/users/uuid/{uuuid}")
        Call<UserProfile> getUser(@Path("uuuid") String userUuid, @Header("Authorization") String authorizationHeader);
    }
}
