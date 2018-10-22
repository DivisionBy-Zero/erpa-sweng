package ch.epfl.sweng.erpa.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_REMOTE_SERVICES_PROVIDERS;

public class SelectRemoteServicesProviderActivity extends Activity {
    @Inject @Named(RES_REMOTE_SERVICES_PROVIDERS) Set<Class<? extends RemoteServicesProvider>> rsps;
    @Inject RemoteServicesProviderCoordinator rspCoordinator;

    @BindView(R.id.rspSelectionRadioGroup) RadioGroup rspSelectionRadioGroup;

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

    @OnClick(R.id.rspSelectionSubmit)
    public void rspSelect() {
        @SuppressLint("FindViewByIdCast")  // Linter is wrong. This will always be a RadioButton
        Optional<RadioButton> maybeSelection = Optional.ofNullable(
        findViewById(rspSelectionRadioGroup.getCheckedRadioButtonId()));
        maybeSelection.ifPresent(selection -> {
            String selectedRspClassName = selection.getText().toString();
            Log.i("RSP Selection", String.format("Selected Remote Storage Provider %s", selectedRspClassName));
            rspCoordinator.rspClassFromFullyQualifiedName(selectedRspClassName)
                    .ifPresent(rspCoordinator::bindRemoteServicesProvider);
            onBackPressed();
        });
    }
}
