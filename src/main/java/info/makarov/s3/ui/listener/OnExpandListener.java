package info.makarov.s3.ui.listener;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

public class OnExpandListener implements TreeExpansionListener {

    private OnExpandClickAction onExpandClickAction;

    public OnExpandListener(OnExpandClickAction onExpandClickAction) {
        this.onExpandClickAction = onExpandClickAction;
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        onExpandClickAction.run(event);
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
    }

    public interface OnExpandClickAction {
        void run(TreeExpansionEvent event);
    }
}
