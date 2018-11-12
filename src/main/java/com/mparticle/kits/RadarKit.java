package com.mparticle.kits;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import android.support.v4.app.ActivityCompat;
import com.mparticle.MParticle;
import io.radar.sdk.Radar;
import io.radar.sdk.Radar.RadarCallback;
import io.radar.sdk.Radar.RadarPriority;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RadarKit extends KitIntegration implements KitIntegration.ApplicationStateListener, KitIntegration.AttributeListener {

    private static final String KEY_PUBLISHABLE_KEY = "publishableKey";
    private static final String KEY_RUN_AUTOMATICALLY = "runAutomatically";

    private boolean mRunAutomatically = true;

    private void tryStartTracking() {
        boolean hasGrantedPermissions = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasGrantedPermissions) {
            Radar.setTrackingPriority(RadarPriority.EFFICIENCY);
            Radar.startTracking();
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

        Map<MParticle.IdentityType, String> identities = getUserIdentities();
        String customerId = identities.get(MParticle.IdentityType.CustomerId);
        if (customerId != null) {
            Radar.setUserId(customerId);
        }

        if (mRunAutomatically) {
            this.tryTrackOnce();
            this.tryStartTracking();
        }

        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
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
        }
    }

    @Override
    public void onApplicationBackground() {
    }

    @Override
    public void setUserAttribute(String s, String s1) {

    }

    @Override
    public void setUserAttributeList(String s, List<String> list) {

    }

    @Override
    public boolean supportsAttributeLists() {
        return false;
    }

    @Override
    public void setAllUserAttributes(Map<String, String> map, Map<String, List<String>> map1) {

    }

    @Override
    public void removeUserAttribute(String s) {

    }

    @Override
    public void setUserIdentity(MParticle.IdentityType identityType, String id) {
        if (identityType.equals(MParticle.IdentityType.CustomerId)) {
            Radar.setUserId(id);

            if (mRunAutomatically) {
                this.tryTrackOnce();
                this.tryStartTracking();
            }
        }
    }

    @Override
    public void removeUserIdentity(MParticle.IdentityType identityType) {
        if (identityType.equals(MParticle.IdentityType.CustomerId) && mRunAutomatically) {
            Radar.stopTracking();
        }
    }

    @Override
    public List<ReportingMessage> logout() {
        if (mRunAutomatically) {
            Radar.stopTracking();
        }

        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(ReportingMessage.logoutMessage(this));
        return messageList;
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean optedOut) {
        if (mRunAutomatically) {
            Radar.stopTracking();
        }
        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(new ReportingMessage(this, ReportingMessage.MessageType.OPT_OUT, System.currentTimeMillis(), null));
        return messageList;
    }

}
