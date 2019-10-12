package info.makarov.s3.ui.window.main;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import info.makarov.s3.core.entity.preferences.Preferences;
import info.makarov.s3.core.entity.preferences.Profile;
import info.makarov.s3.core.exception.S3PluginException;
import info.makarov.s3.core.service.BucketService;
import info.makarov.s3.core.service.LazyTreeService;
import info.makarov.s3.core.service.PreferencesService;
import info.makarov.s3.ui.adapter.LazyNodeAdapter;
import info.makarov.s3.ui.adapter.LazyNodeAdapterType;
import info.makarov.s3.ui.adapter.LazyTreeAdapter;
import info.makarov.s3.ui.factory.ContextMenuItemsFactory;
import info.makarov.s3.ui.listener.OnDoubleClickListener;
import info.makarov.s3.ui.listener.OnExpandListener;
import info.makarov.s3.ui.listener.OnRightClickListener;
import info.makarov.s3.ui.models.S3TreeNode;
import info.makarov.s3.ui.utils.UiUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MainWindow {

    private JPanel mainWindowContent;
    private Tree bucketTree;
    private JPanel myPanel;
    private JComboBox cbProfiles;

    private final JPopupMenu popup = new JPopupMenu();

    final ToolbarDecorator decorator;

    private DefaultTreeModel treeModel = new DefaultTreeModel(null);

    private Map<String, LazyTreeAdapter> trees = new HashMap<>();

    private final BucketService bucketService = ServiceManager.getService(BucketService.class);
    private final PreferencesService preferencesService = ServiceManager.getService(PreferencesService.class);
    private final LazyTreeService treeService = ServiceManager.getService(LazyTreeService.class);
    private final ContextMenuItemsFactory contextMenuItemsFactory = ServiceManager.getService(ContextMenuItemsFactory.class);

    public MainWindow(ToolWindow toolWindow) {

        updateProfiles();

        cbProfiles.addActionListener(x -> updateBuckets(getActiveProfile()));

        bucketTree.setModel(treeModel);
        bucketTree.addMouseListener(new OnDoubleClickListener(bucketTree, (event, path) -> {
            Profile profile = getActiveProfile();
            if (profile == null) {
                log.warn("Selected profile is null");
                return;
            }
            S3TreeNode uiNode = (S3TreeNode) path.getLastPathComponent();
            LazyNodeAdapter node = uiNode.getUserObject();
            node.onPreviewClick(getActiveProfile(), trees.get(getActiveProfile().getName()));
            if (node.getNodeAdapterType() == LazyNodeAdapterType.LOAD_MORE_ITEMS) {
                uiNode = uiNode.getParent();
                UiUtils.updateTree(uiNode);
                treeModel.reload(uiNode);
            }
        }));
        bucketTree.addMouseListener(new OnRightClickListener(bucketTree, (event, path) -> {
            popup.removeAll();
            S3TreeNode uiNode = (S3TreeNode) path.getLastPathComponent();
            LazyNodeAdapter node = uiNode.getUserObject();
            if (node.getNodeAdapterType() != LazyNodeAdapterType.DEFAULT) {
                return;
            }
            Profile profile = getActiveProfile();
            switch (node.getType()) {
                case LEAF:
                    contextMenuItemsFactory.createNodeContextMenuItems(node, profile, trees.get(profile.getName()))
                            .forEach(menuItem -> popup.add(menuItem));
                    break;
                case BRANCH:
                    JMenuItem expand = new JMenuItem("Expand");
                    JMenuItem collapse = new JMenuItem("Collapse");
                    popup.add(expand);
                    popup.add(collapse);
                    break;
                case BUCKET:
                    JMenuItem addObject = new JMenuItem("Add new object...");
                    JMenuItem countObjects = new JMenuItem("Count...");
                    JMenuItem copyBucketName = new JMenuItem("Copy bucket name");
                    popup.add(addObject);
                    popup.add(countObjects);
                    popup.add(copyBucketName);
                    break;
                default:
                    return;
            }
            popup.show(bucketTree, event.getX(), event.getY());
        }));
        bucketTree.setRootVisible(false);
        decorator = ToolbarDecorator.createDecorator(bucketTree)
                .setAddAction(x -> {
                    String selectedBucket = getSelectedBucket();
                    if (selectedBucket == null) {
                        return;
                    }
                    UiUtils.chooseFile(virtualFile -> {
                        try (InputStream is = virtualFile.getInputStream()) {
                            bucketService.addObject(getActiveProfile(), selectedBucket, virtualFile.getName(), is, virtualFile.getLength());
                            updateBuckets(getActiveProfile());
                        } catch (IOException e) {
                            throw new S3PluginException("Cannot to add file " + virtualFile, e);
                        }
                    });
                })
                .setAddActionUpdater(x -> getSelectedBucket() != null)
//                TODO Uncomment when ui config is complete
//                .addExtraAction(new AnActionButton() {
//                    @Override
//                    public void actionPerformed(@NotNull AnActionEvent e) {
//                        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), S3Configurable.class);
//                    }
//                })
                .addExtraAction(new AnActionButton(null, "Settings", AllIcons.General.Settings) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        preferencesService.getPreferences();
                        File prefFile = preferencesService.getPrefFile();
                        UiUtils.openFile(prefFile);
                    }
                })
                .addExtraAction(new AnActionButton(null, "Update buckets", AllIcons.Actions.Refresh) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        updateProfiles();
                        Profile selectedItem = getActiveProfile();
                        if (selectedItem == null) {
                            log.warn("Selected profile is null");
                            return;
                        }
                        updateBuckets(selectedItem);
                    }
                })
                .setToolbarPosition(ActionToolbarPosition.TOP);
        JPanel panel = decorator.createPanel();
        myPanel.add(panel, BorderLayout.CENTER);
        new TreeSpeedSearch(bucketTree, x -> x.getLastPathComponent().toString(), true);
        bucketTree.addTreeExpansionListener(new OnExpandListener(event -> {
            S3TreeNode node = (S3TreeNode) event.getPath().getLastPathComponent();
            log.debug("Expanded: " + node.getUserObject().getName());
            // TODO убрать и добавить очистку чилдов при коллапсе, это даст обновление нод
            if (node.getUserObject().isChildrenLoading()) {
                return;
            }
            loadChildren(node);
        }));
    }

    private void loadChildren(S3TreeNode node) {
        node.getUserObject().loadChildren(getActiveProfile(), trees.get(getActiveProfile().getName()));
        UiUtils.updateTree(node);
        treeModel.reload(node);
    }

    @Nullable
    private Profile getActiveProfile() {
        return (Profile) cbProfiles.getSelectedItem();
    }

    public void updateProfiles() {
        Preferences preferences = preferencesService.getPreferences();
        ListComboBoxModel<Profile> model = new ListComboBoxModel<>(preferences.getProfiles());
        cbProfiles.setModel(model);
    }

    public void updateBuckets(Profile profile) {
        this.bucketTree.setPaintBusy(true); // TODO not works yet
        LazyTreeAdapter lazyTree = new LazyTreeAdapter(treeService.getLazyTree(profile));
        trees.put(getActiveProfile().getName(), lazyTree);
        treeModel.setRoot(UiUtils.createTreeModel(lazyTree.getRoot()));
        treeModel.reload();
        this.bucketTree.setPaintBusy(false);
    }

    public JPanel getContent() {
        return mainWindowContent;
    }

    private LazyNodeAdapter getBucket(TreePath path) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPath()[1];
        return (LazyNodeAdapter) node.getUserObject();
    }

    private LazyNodeAdapter getKey(TreePath path) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPath()[path.getPath().length - 1];
        return (LazyNodeAdapter) node.getUserObject();
    }

    private String getSelectedBucket() {
        S3TreeNode[] selectedNodes = bucketTree.getSelectedNodes(S3TreeNode.class, null);
        if (selectedNodes.length != 1) {
            return null;
        }
        if (selectedNodes[0].getParent() == null) {
            return null;
        }
        if (selectedNodes[0].getParent().getParent() != null) {
            return null;
        }
        return selectedNodes[0].getUserObject().getName();
    }
}
