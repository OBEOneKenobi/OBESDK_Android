package machina.com.obe;

/**
 * Created by Henry Serrano on 3/21/16.
 * Copyright (c) 2016
 */
public interface OBEListener {
    public void onOBEConnected();
    public void onQuaternionUpdated();
    public void onButtonsUpdated();
}
