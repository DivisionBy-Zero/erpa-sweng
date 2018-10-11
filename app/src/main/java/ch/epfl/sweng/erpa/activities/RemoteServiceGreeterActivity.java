package ch.epfl.sweng.erpa.activities;

import android.os.Bundle;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;

public class RemoteServiceGreeterActivity extends DependencyConfigurationAgnosticActivity {
    @Inject RemoteServicesProvider remoteServicesProvider;
    @Inject RemoteServicesProviderCoordinator remoteServicesProviderCoordinator;

    @BindView(R.id.tw1) TextView tw1;
    @BindView(R.id.tw2) TextView tw2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_remote_service_greeter);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tw1.setText(String.format("Hello %s!", remoteServicesProvider.getFriendlyProviderName()));
        tw2.setText(String.format("Hello %s", remoteServicesProvider.getFriendlyProviderDescription()));
    }

    @OnClick(R.id.disconnectRspButton)
    protected void onDisconnectRspButton() {
        remoteServicesProviderCoordinator.bindRemoteServicesProvider(null);
        recreate();
    }
}
