package com.mparticle.kits;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.mparticle.MParticle;
import com.mparticle.identity.MParticleUser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.radar.sdk.Radar;
import io.radar.sdk.Radar.RadarTrackCallback;
import io.radar.sdk.RadarTrackingOptions;

public class RadarKit extends KitIntegration implements KitIntegration.ApplicationStateListener, KitIntegration.IdentityListener {

    private static final String KEY_PUBLISHABLE_KEY = "publishableKey";
    private static final String KEY_RUN_AUTOMATICALLY = "runAutomatically";

    boolean mRunAutomatically = true;

    private void tryStartTracking() {
        boolean hasGrantedPermissions = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasGrantedPermissions) {
            Radar.startTracking(RadarTrackingOptions.EFFICIENT);
        }
    }

    private void tryTrackOnce() {
        boolean hasGrantedPermissions = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasGrantedPermissions) {
            Radar.trackOnce((RadarTrackCallback)null);
        }
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        String publishableKey = settings.get(KEY_PUBLISHABLE_KEY);
        mRunAutomatically = settings.containsKey(KEY_RUN_AUTOMATICALLY) && Boolean.parseBoolean(settings.get(KEY_RUN_AUTOMATICALLY));

        Radar.initialize(context, publishableKey);
        Radar.setAdIdEnabled(true);
        MParticleUser user = getCurrentUser();
        if (user != null) {
            Map<MParticle.IdentityType, String> identities = user.getUserIdentities();
            String customerId = identities.get(MParticle.IdentityType.CustomerId);
            if (customerId != null) {
                Radar.setUserId(customerId);
            }
        }
        if (mRunAutomatically) {
            tryStartTracking();
        } else {
            Radar.stopTracking();
        }

        List<ReportingMessage> messageList = new LinkedList<>();
        messageList.add(new ReportingMessage(this, ReportingMessage.MessageType.APP_STATE_TRANSITION, System.currentTimeMillis(), null));
        return messageList;
    }

    @Override
    public String getName() {
        return "RadarKit";
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

    boolean setUserAndTrack(MParticleUser user, String currentRadarId) {
        return setUserAndTrack(user, currentRadarId, false);
    }

    boolean setUserAndTrack(MParticleUser user, String currentRadarId, boolean unitTesting) {
        if (user == null) {
            return false;
        }
        String newId = user.getUserIdentities().get(MParticle.IdentityType.CustomerId);
        boolean updatedId = newId == null ? currentRadarId != null : !newId.equals(currentRadarId);
        if (updatedId && !unitTesting) {
            Radar.setUserId(newId);
            if (mRunAutomatically) {
                tryTrackOnce();
                tryStartTracking();
            }
        }
        return updatedId;
    }

    @Override
    public void onIdentifyCompleted(MParticleUser mParticleUser,
        FilteredIdentityApiRequest filteredIdentityApiRequest) {
        setUserAndTrack(mParticleUser, Radar.getUserId());
    }

    @Override
    public void onLoginCompleted(MParticleUser mParticleUser,
        FilteredIdentityApiRequest filteredIdentityApiRequest) {
        setUserAndTrack(mParticleUser, Radar.getUserId());
    }

    @Override
    public void onLogoutCompleted(MParticleUser mParticleUser,
        FilteredIdentityApiRequest filteredIdentityApiRequest) {
        setUserAndTrack(mParticleUser, Radar.getUserId());
    }

    @Override
    public void onModifyCompleted(MParticleUser mParticleUser,
        FilteredIdentityApiRequest filteredIdentityApiRequest) {
        setUserAndTrack(mParticleUser, Radar.getUserId());
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
