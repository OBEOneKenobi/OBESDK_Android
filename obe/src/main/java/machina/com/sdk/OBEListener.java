package machina.com.sdk;

/**
 * Created by Henry Serrano on 3/21/16.
 * Copyright (c) 2016
 */
public interface OBEListener {
    public void onOBEConnected();
    public void onQuaternionsUpdated(OBEQuaternion left, OBEQuaternion right, OBEQuaternion center);
    public void onButtonsUpdated();
}
