package info.makarov.s3.ui.factory;

import com.google.common.collect.Lists;
import info.makarov.s3.core.entity.preferences.Profile;
import info.makarov.s3.ui.adapter.LazyNodeAdapter;
import info.makarov.s3.ui.adapter.LazyNodeAdapterType;
import info.makarov.s3.ui.adapter.LazyTreeAdapter;
import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public class ContextMenuItemsFactory {

    public List<JMenuItem> createNodeContextMenuItems(LazyNodeAdapter nodeAdapter, Profile profile, LazyTreeAdapter treeAdapter) {
        if (nodeAdapter.getNodeAdapterType() != LazyNodeAdapterType.DEFAULT) {
            return Collections.emptyList();
        }
        switch (nodeAdapter.getType()) {
            case LEAF:
                return createObjectMenuItems(nodeAdapter, profile, treeAdapter);
            default:
                return Collections.emptyList();
        }
    }

    private List<JMenuItem> createObjectMenuItems(LazyNodeAdapter nodeAdapter, Profile profile, LazyTreeAdapter treeAdapter) {
        JMenuItem preview = new JMenuItem("Preview");
        preview.setAction(new OnContextMenuItemClick("Preview", nodeAdapter) {
            @Override
            public void actionPerformed(ActionEvent e) {
                nodeAdapter.onPreviewClick(profile, treeAdapter);
            }
        });
        JMenuItem download = new JMenuItem("Download to...");
        JMenuItem copyObjectName = new JMenuItem("Copy object name");
        JMenuItem delete = new JMenuItem("Delete...");

        return Lists.newArrayList(preview, download, copyObjectName, delete);
    }

    abstract static class OnContextMenuItemClick extends AbstractAction {

        OnContextMenuItemClick(String name, LazyNodeAdapter lazyNodeAdapter) {
            super(name);
            this.lazyNodeAdapter = lazyNodeAdapter;
        }

        @Getter
        private final LazyNodeAdapter lazyNodeAdapter;

    }

}
