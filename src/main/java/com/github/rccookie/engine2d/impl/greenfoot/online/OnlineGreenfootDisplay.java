package com.github.rccookie.engine2d.impl.greenfoot.online;

import com.github.rccookie.engine2d.Application;
import com.github.rccookie.engine2d.Execute;
import com.github.rccookie.engine2d.impl.greenfoot.GreenfootDisplay;
import com.github.rccookie.engine2d.util.greenfoot.JS;
import com.github.rccookie.geometry.performance.int2;

import org.teavm.jso.JSBody;

@SuppressWarnings("JSUnresolvedVariable")
public class OnlineGreenfootDisplay extends GreenfootDisplay {

    private boolean allowResizing = true;

    OnlineGreenfootDisplay() {
        // Cache some common searches
        JS.eval("window.scenarioContainer = document.getElementById('playButton').parentElement;" +
                "window.scenario = document.getElementById('scenarioCanvas');");
        // Width of the inner div can always be 100%. Height has to be adjusted according to the height
        // of the currently shown world
        JS.eval("scenarioContainer.children[0].style.width = '100%'");
        // Fix focus bug
        JS.eval("window.scenario.onclick = () => window.scenario.focus()");

        updateContainerSize(world.getWidth(), world.getHeight());
        showResizing();

        Execute.repeating(this::updateResolution, 1/60f);
    }

    @Override
    public void setResolution(int2 resolution) {
        super.setResolution(resolution);
        updateContainerSize(resolution.x, resolution.y);
    }

    @Override
    public void allowResizingChanged(boolean allowed) {
        if(this.allowResizing == allowed) return;
        this.allowResizing = allowed;
        if(allowed)
            showResizing();
        else
            hideResizing();
    }

    private void updateResolution() {
        if(!allowResizing) return;
        int2 target = getTargetSize();
        if(world.getWidth() != target.x || world.getHeight() != target.y)
            Application.getDisplayController().setResolution(target);
    }



    private static int2 getTargetSize() {
        return new int2(getTargetSize0());
    }



    @JSBody(script =
            "scenarioContainer.style.overflow = 'auto';\n" +
            "scenarioContainer.style.resize = 'both';" +
            "scenarioContainer.style.border = '2px solid';")
    private static native void showResizing();

    @JSBody(script =
            "scenarioContainer.style.overflow = 'hidden';\n" +
            "scenarioContainer.style.resize = 'none';" +
            "scenarioContainer.style.border = '';")
    private static native void hideResizing();

    @JSBody(script = "return [scenarioContainer.clientWidth-14, scenarioContainer.clientHeight-134];")
    private static native int[] getTargetSize0();

    @JSBody(params = { "width", "height" }, script =
            "scenarioContainer.children[0].style.height = (height+64) + 'px';" +
            "scenarioContainer.style.width = (width+14) + 'px';" +
            "scenarioContainer.style.height = (height+118) + 'px';")
    private static native void updateContainerSize(int width, int height);
}
