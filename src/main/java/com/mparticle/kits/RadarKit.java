package com.mparticle.kits;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.mparticle.MParticle;
import com.onradar.sdk.Radar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RadarKit extends KitIntegration implements KitIntegration.ActivityListener, KitIntegration.AttributeListener {

    private static final String KEY_PUBLISHABLE_KEY = "publishableKey";
    private static final String KEY_RUN_AUTOMATICALLY = "runAutomatically";

    private boolean mRunAutomatically = true;

    private void tryStartTracking() {
        boolean hasGrantedPermissions = Radar.checkSelfPermissions();

        if (hasGrantedPermissions) {
            Radar.startTracking();
        }
    }

    private void tryTrackOnce() {
        boolean hasGrantedPermissions = Radar.checkSelfPermissions();

        if (hasGrantedPermissions) {
            Radar.trackOnce(null);
        }
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        String publishableKey = settings.get(KEY_PUBLISHABLE_KEY);
        mRunAutomatically = settings.containsKey(KEY_RUN_AUTOMATICALLY) && Boolean.parseBoolean(settings.get(KEY_RUN_AUTOMATICALLY));

        Radar.initialize(context, publishableKey);

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
    public List<ReportingMessage> onActivityCreated(Activity activity, Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityStarted(Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityResumed(Activity activity) {
        if (mRunAutomatically) {
            this.tryTrackOnce();
        }

        List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
        messageList.add(new ReportingMessage(this, ReportingMessage.MessageType.APP_STATE_TRANSITION, System.currentTimeMillis(), null));
        return messageList;
    }

    @Override
    public List<ReportingMessage> onActivityPaused(Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityStopped(Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityDestroyed(Activity activity) {
        return null;
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
