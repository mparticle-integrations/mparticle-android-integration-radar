package com.mparticle.kits;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import com.mparticle.MParticle;
import com.mparticle.MParticle.IdentityType;
import com.mparticle.identity.MParticleUser;
import io.radar.sdk.Radar;
import io.radar.sdk.Radar.RadarCallback;
import io.radar.sdk.Radar.RadarTrackingPriority;
import io.radar.sdk.RadarTrackingOptions;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RadarKit extends KitIntegration implements KitIntegration.ApplicationStateListener, KitIntegration.IdentityListener {

    private static final String KEY_PUBLISHABLE_KEY = "publishableKey";
    private static final String KEY_RUN_AUTOMATICALLY = "runAutomatically";

    private boolean mRunAutomatically = true;

    private void tryStartTracking() {
        boolean hasGrantedPermissions = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasGrantedPermissions) {
            Radar.startTracking(
                new RadarTrackingOptions.Builder()
                    .priority(RadarTrackingPriority.EFFICIENCY)
                    .build()
            );
        }
    }

    private void tryTrackOnce() {
        boolean hasGrantedPermissions = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasGrantedPermissions) {
            Radar.trackOnce((RadarCallback)null);
        }
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        String publishableKey = settings.get(KEY_PUBLISHABLE_KEY);
        mRunAutomatically = settings.containsKey(KEY_RUN_AUTOMATICALLY) && Boolean.parseBoolean(settings.get(KEY_RUN_AUTOMATICALLY));

        Radar.initialize(publishableKey);

        Map<MParticle.IdentityType, String> identities = getCurrentUser().getUserIdentities();
        String customerId = identities.get(MParticle.IdentityType.CustomerId);
        if (customerId != null) {
            Radar.setUserId(customerId);
        }

        if (mRunAutomatically) {
            this.tryTrackOnce();
            this.tryStartTracking();
        } else {
            Radar.stopTracking();
        }

        List<ReportingMessage> messageList = new LinkedList<>();
        messageList.add(new ReportingMessage(this, ReportingMessage.MessageType.APP_STATE_TRANSITION, System.currentTimeMillis(), null));
        return messageList;
    }

    @Override
    public String getName() {
        return "Radar";
    }

    @Override
    public void onApplicationForeground() {
        if (mRunAutomatically) {
            this.tryTrackOnce();
        } else {
            Radar.stopTracking();
        }
    }

    @Override
    public void onApplicationBackground() {
    }

    @Override
    public void onIdentifyCompleted(MParticleUser mParticleUser,
        FilteredIdentityApiRequest filteredIdentityApiRequest) {
        String customerId = filteredIdentityApiRequest.getNewIdentities().get(IdentityType.CustomerId);
        if (customerId != null) {
            Radar.setUserId(customerId);

            if (mRunAutomatically) {
                tryTrackOnce();
                tryStartTracking();
            }
        }
    }

    @Override
    public void onLoginCompleted(MParticleUser mParticleUser,
        FilteredIdentityApiRequest filteredIdentityApiRequest) {
    }

    @Override
    public void onLogoutCompleted(MParticleUser mParticleUser,
        FilteredIdentityApiRequest filteredIdentityApiRequest) {
        if (mRunAutomatically) {
            Radar.setUserId(null);
            Radar.stopTracking();
        }
    }

    @Override
    public void onModifyCompleted(MParticleUser mParticleUser,
        FilteredIdentityApiRequest filteredIdentityApiRequest) {
        boolean oldHasId = filteredIdentityApiRequest.getOldIdentities().containsKey(IdentityType.CustomerId);
        boolean newHasId = filteredIdentityApiRequest.getNewIdentities().containsKey(IdentityType.CustomerId);
        if (oldHasId && !newHasId && mRunAutomatically) {
            Radar.setUserId(null);
            Radar.stopTracking();
        }
    }

    @Override
    public void onUserIdentified(MParticleUser mParticleUser) {
    }


    @Override
    public List<ReportingMessage> setOptOut(boolean optedOut) {
        if (mRunAutomatically) {
            Radar.stopTracking();
        }
        List<ReportingMessage> messageList = new LinkedList<>();
        messageList.add(new ReportingMessage(this, ReportingMessage.MessageType.OPT_OUT, System.currentTimeMillis(), null));
        return messageList;
    }
}
