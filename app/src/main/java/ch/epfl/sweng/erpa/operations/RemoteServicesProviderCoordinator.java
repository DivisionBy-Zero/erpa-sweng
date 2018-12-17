package ch.epfl.sweng.erpa.operations;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.annimon.stream.Optional;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.SelectRemoteServicesProviderActivity;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;


/**
 * This is responsible of memoizing and proxying the selected RemoteServicesProvider.
 * This class' life cycle is managed by Toothpick.
 * <p>
 * Hereafter rsp = Remote Services Provider.
 * <p>
 * This class implements the Proxy Pattern for RemoteServicesProvider.class.
 */
@Singleton
public class RemoteServicesProviderCoordinator implements DependencyCoordinator<RemoteServicesProvider> {
    /*
     * A word or wary: This singleton may be called prior to an activity creation and may start
     * SelectRemoteServicesProviderActivity. Be conservative about injecting stuff here since it
     * may lead to an infinite dependency loop.
     */
    private final String REMOTE_PROVIDER_KEY;
    private final Context applicationContext;
    @Inject @Named(ErpaApplication.RES_APPLICATION_SCOPE) Scope applicationScope;
    @Inject SharedPreferences sharedPreferences;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<RemoteServicesProvider> currentProvider = Optional.empty();
    private final Intent configurationIntent;

    @Inject
    public RemoteServicesProviderCoordinator(@Named(ErpaApplication.RES_APPLICATION_SCOPE) Scope scope, Context appCtx) {
        this.applicationContext = appCtx;
        Toothpick.inject(this, scope);
        REMOTE_PROVIDER_KEY = appCtx.getString(R.string.prop_key_remote_provider);
        rspClassFromApplicationPreferences().ifPresent(this::bindRemoteServicesProvider);
        configurationIntent = new Intent(applicationContext, SelectRemoteServicesProviderActivity.class);
    }

    public void bindRemoteServicesProvider(@Nullable Class<? extends RemoteServicesProvider> rspClass) {
        currentProvider.ifPresent(RemoteServicesProvider::terminate);
        currentProvider = Optional.empty();
        String rspClassName = null;

        if (rspClass != null) {
            try {
                Constructor<?> instanceConstructor = rspClass.getConstructor();
                RemoteServicesProvider instance = (RemoteServicesProvider) instanceConstructor.newInstance(new Object[]{});
                Toothpick.inject(instance, applicationScope);
                this.currentProvider = Optional.of(instance);
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), String.format("Could not construct RemoteServicesProvider instance: %s", e.getMessage()));
            }
            rspClassName = rspClass.getName();
        }

        sharedPreferences.edit().putString(REMOTE_PROVIDER_KEY, rspClassName).apply();
    }

    Optional<Class<? extends RemoteServicesProvider>> rspClassFromApplicationPreferences() {
        String remoteServicesProviderClassName = sharedPreferences.getString(REMOTE_PROVIDER_KEY, null);
        return rspClassFromFullyQualifiedName(remoteServicesProviderClassName);
    }

    public Optional<Class<? extends RemoteServicesProvider>> rspClassFromFullyQualifiedName(String rspClassName) {
        if (rspClassName == null || rspClassName.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            Class referencedClass = Class.forName(rspClassName);
            if (RemoteServicesProvider.class.isAssignableFrom(referencedClass)) {
                //noinspection unchecked
                return Optional.of((Class<? extends RemoteServicesProvider>) referencedClass);
            }
        } catch (Exception e) {
            // we couldn't find the target class, no problem.
        }
        return Optional.empty();
    }

    Optional<RemoteServicesProvider> getCurrentProvider() {
        return currentProvider;
    }

    @Override
    public boolean dependencyIsConfigured() {
        return currentProvider.isPresent();
    }

    @Override
    public Intent dependencyConfigurationIntent() {
        return configurationIntent;
    }

    @Override
    public Class<RemoteServicesProvider> configuredDependencyClass() {
        return RemoteServicesProvider.class;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!currentProvider.isPresent())
            throw new IllegalStateException("RemoteServicesProvider not loaded");
        return method.invoke(currentProvider.get(), args);
    }
}
