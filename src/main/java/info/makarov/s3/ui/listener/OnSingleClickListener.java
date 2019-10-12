package info.makarov.s3.ui.listener;

import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class OnSingleClickListener implements MouseListener {

    private Tree tree;
    private OnDoubleClickAction action;

    public OnSingleClickListener(Tree tree, OnDoubleClickAction action) {
        this.tree = tree;
        this.action = action;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (action == null) {
            return;
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            return;
        }
        if (e.getClickCount() != 1) {
            return;
        }
        if (tree.getRowForLocation(e.getX(), e.getY()) == -1) {
            return;
        }
        TreePath selectedPath = tree.getPathForLocation(e.getX(), e.getY());
        action.run(e, selectedPath);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public interface OnDoubleClickAction {
        void run(MouseEvent event, TreePath treePath);
    }
}
