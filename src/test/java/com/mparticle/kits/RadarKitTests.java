package com.mparticle.kits;


import android.content.Context;

import com.mparticle.MParticle;
import com.mparticle.identity.MParticleUser;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RadarKitTests {

    private KitIntegration getKit() {
        return new RadarKit();
    }

    @Test
    public void testGetName() throws Exception {
        String name = getKit().getName();
        assertTrue(name != null && name.length() > 0);
    }

    /**
     * Kit *should* throw an exception when they're initialized with the wrong settings.
     *
     */
    @Test
    public void testOnKitCreate() throws Exception{
        Exception e = null;
        try {
            KitIntegration kit = getKit();
            Map settings = new HashMap<>();
            settings.put("fake setting", "fake");
            kit.onKitCreate(settings, Mockito.mock(Context.class));
        }catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
    }

    @Test
    public void testClassName() throws Exception {
        KitIntegrationFactory factory = new KitIntegrationFactory();
        Map<Integer, String> integrations = factory.getKnownIntegrations();
        String className = getKit().getClass().getName();
        for (Map.Entry<Integer, String> entry : integrations.entrySet()) {
            if (entry.getValue().equals(className)) {
                return;
            }
        }
        fail(className + " not found as a known integration.");
    }

    @Test
    public void testSetUser() throws Exception {
        RadarKit kit = new RadarKit();
        kit.mRunAutomatically = false;
        JSONObject o = new JSONObject();
        assertFalse(kit.setUserAndTrack(null, "foo", o));
        MParticleUser user = Mockito.mock(MParticleUser.class);
        Map<MParticle.IdentityType, String> identities = new HashMap<>();
        identities.put(MParticle.IdentityType.CustomerId, "foo");
        JSONObject radarMetadata = new JSONObject();
        try {
            radarMetadata.put("mParticleId",
                    Long.toString(user.getId()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Mockito.when(user.getUserIdentities()).thenReturn(identities);
        assertFalse(kit.setUserAndTrack(user, "foo", o, true));
        assertTrue(kit.setUserAndTrack(user, "bar", radarMetadata, true));
        assertTrue(kit.setUserAndTrack(user, null, o, true));
        identities.clear();
        assertTrue(kit.setUserAndTrack(user, "foo", radarMetadata, true));
    }
}