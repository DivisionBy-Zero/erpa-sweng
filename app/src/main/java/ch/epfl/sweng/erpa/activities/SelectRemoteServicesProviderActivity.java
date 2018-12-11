package ch.epfl.sweng.erpa.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.GCP.GCPRemoteServicesProvider;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_REMOTE_SERVICES_PROVIDERS;
import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;

public class SelectRemoteServicesProviderActivity extends Activity {
    // FIXME(@Roos): Find a better way to express this: Ideally, I want to store this along with the class def.
    private static final Map<Class, String[]> REQUIRED_PERMISSIONS =
        Collections.unmodifiableMap(new HashMap<Class, String[]>() {{
            put(GCPRemoteServicesProvider.class, new String[]{Manifest.permission.INTERNET});
        }});
    private static final int REQUEST_PERMISSIONS_RESULT_CODE = 22345;

    @Inject @Named(RES_REMOTE_SERVICES_PROVIDERS) Set<Class<? extends RemoteServicesProvider>> rsps;
    @Inject RemoteServicesProviderCoordinator rspCoordinator;

    @BindView(R.id.rspSelectionRadioGroup) RadioGroup rspSelectionRadioGroup;
    Optional<Class<? extends RemoteServicesProvider>> currentSelection = Optional.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application application = getApplication();
        Scope scope = Toothpick.openScopes(application, this);
        Toothpick.inject(this, scope); // Inject without RemoteServicesProvider
        setContentView(R.layout.activity_select_remote_services_provider);
        ButterKnife.bind(this);

        Stream.of(rsps).map(Class::getCanonicalName).forEach(rsp -> {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(rsp);
            rspSelectionRadioGroup.addView(radioButton);
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (tryBind()) finish();
    }

    @OnClick(R.id.rspSelectionSubmit)
    public void rspSelect() {
        @SuppressLint("FindViewByIdCast")  // Linter is wrong. This will always be a RadioButton
            Optional<RadioButton> maybeSelection = Optional.ofNullable(
            findViewById(rspSelectionRadioGroup.getCheckedRadioButtonId()));

        currentSelection = maybeSelection
            .map(radioButton -> radioButton.getText().toString())
            .flatMap(rspCoordinator::rspClassFromFullyQualifiedName)
            .executeIfPresent(clsName -> Log.i("RSP Selection",
                String.format("Selected Remote Services Provider %s", clsName)));

        if (tryBind()) finish();
    }

    private boolean tryBind() {
        if (!currentSelection.isPresent())
            return false;

        Class<? extends RemoteServicesProvider> rspClass = currentSelection.get();
        String[] missingPermissions =
            Optional.ofNullable(REQUIRED_PERMISSIONS.get(rspClass)).stream().flatMap(Stream::of)
                .map(p -> ContextCompat.checkSelfPermission(this, p))
                .filter(p -> p != PackageManager.PERMISSION_GRANTED)
                .toArray(String[]::new);

        if (missingPermissions.length > 0) {
            ActivityCompat.requestPermissions(this,
                missingPermissions, REQUEST_PERMISSIONS_RESULT_CODE);
            return false;
        }

        rspCoordinator.bindRemoteServicesProvider(rspClass);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (tryBind()) {
            finish();
            return;
        }
        createPopup("Please grant the requested permissions to use the selected provider", this);
        rspSelectionRadioGroup.clearCheck();
    }
}
