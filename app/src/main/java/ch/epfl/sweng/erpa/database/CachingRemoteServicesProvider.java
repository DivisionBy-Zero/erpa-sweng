package ch.epfl.sweng.erpa.database;

import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.annimon.stream.Optional;
import com.annimon.stream.function.BiConsumer;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.ThrowableSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import ch.epfl.sweng.erpa.services.GCP.GCPRemoteServicesProvider;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.UserManagementService;
import lombok.Getter;

@Singleton
public class CachingRemoteServicesProvider implements RemoteServicesProvider {

    @Inject Context appCtx;
    private final GCPRemoteServicesProvider wrappedRSP;
    @Getter private final CachingGameService gameService;
    @Getter private final CachingUserService userProfileService;


    private final static String databaseName = "ERPADatabase";
    @Getter private Optional<ERPADatabase> cached;
    @Getter private final AsyncTaskService asyncTaskService;

    @Inject public CachingRemoteServicesProvider(GCPRemoteServicesProvider rsp) {
        cached = Optional.empty();
        this.wrappedRSP = rsp;
        this.gameService = new CachingGameService(rsp.getGameService(), this);
        this.userProfileService = new CachingUserService(rsp.getUserProfileService(), this);

        asyncTaskService = new AsyncTaskService();
        asyncTaskService.run(
                () -> Room.databaseBuilder(appCtx, ERPADatabase.class, databaseName).build(),
                db -> this.cached = Optional.of(db));
    }

    // This function does everything
    // Maybe even making coffee
    // Naturally, it's super h*cking complex
    <MethodResult, DatabaseEntry> MethodResult proxyAndCache(ThrowableSupplier<MethodResult, Throwable> delegatedCall,
                                                             BiConsumer<ERPADatabase, DatabaseEntry> inserter,
                                                             Function<MethodResult, DatabaseEntry> methodResultToDatabaseEntryConverter,
                                                             Function<DatabaseEntry, MethodResult> databaseEntryToMethodResultConverter,
                                                             Function<ERPADatabase, DatabaseEntry> databaseEntrySupplier) throws Throwable {
        if (cached.isPresent()) {
            // Get the database entry, and update it
            DatabaseEntry dbEntry = databaseEntrySupplier.apply(cached.get());
            if (dbEntry != null) {
                asyncTaskService.run(
                        delegatedCall,
                        result -> inserter.accept(cached.get(), methodResultToDatabaseEntryConverter.apply(result)),
                        throwable -> Log.e("updateCacheDatabase", "Error occurred when updating database", throwable)
                );
                return databaseEntryToMethodResultConverter.apply(dbEntry);
            } else {
                // Get the response, and then add it to the database
                MethodResult result = delegatedCall.get();
                inserter.accept(cached.get(), methodResultToDatabaseEntryConverter.apply(result));
                return result;
            }
        } else {
            // Database not available, just delegate the call
            return delegatedCall.get();
        }
    }

    @Override public UserManagementService getUserProfileService() {
        return userProfileService;
    }

    @Override public String getFriendlyProviderName() {
        return wrappedRSP.getFriendlyProviderName();
    }

    @Override public String getFriendlyProviderDescription() {
        return wrappedRSP.getFriendlyProviderDescription();
    }

    @Override public void terminate() {
        wrappedRSP.terminate();
        if (cached.isPresent()) {
            cached.get().close();
        }
    }

    boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) appCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
