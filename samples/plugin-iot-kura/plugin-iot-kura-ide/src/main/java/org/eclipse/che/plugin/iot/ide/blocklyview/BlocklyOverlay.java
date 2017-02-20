package org.eclipse.che.plugin.iot.ide.blocklyview;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;


/**
 * JavaScript overlay to inject Blockly
 *
 * @author vasco
 */
public class BlocklyOverlay extends JavaScriptObject {

    protected BlocklyOverlay() {
    }


    public final static native void startBlockly()  /*-{
        $wnd.startBlockly();
        $wnd.onresize();
    }-*/;

    public final static native void startBlocklyResizable()  /*-{
        $wnd.startBlocklyResizable();
    }-*/;


}