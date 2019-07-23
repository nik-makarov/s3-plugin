package info.makarov.s3.ui.window;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import info.makarov.s3.core.entity.NodeModel;
import info.makarov.s3.core.entity.preferences.Preferences;
import info.makarov.s3.core.entity.preferences.Profile;
import info.makarov.s3.core.exception.S3PluginException;
import info.makarov.s3.core.service.BucketService;
import info.makarov.s3.core.service.PreferencesService;
import info.makarov.s3.core.service.TreeService;
import info.makarov.s3.core.utils.FileUtils;
import info.makarov.s3.ui.listener.OnDoubleClickListener;
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

@Slf4j
public class MainWindow {

    private JPanel mainWindowContent;
    private Tree bucketTree;
    private JPanel myPanel;
    private JComboBox cbProfiles;

    final ToolbarDecorator decorator;

    private DefaultTreeModel treeModel = new DefaultTreeModel(new S3TreeNode(new NodeModel("root", NodeModel.Type.ROOT)));

    private final BucketService bucketService = ServiceManager.getService(BucketService.class);
    private final PreferencesService preferencesService = ServiceManager.getService(PreferencesService.class);
    private final TreeService treeService = ServiceManager.getService(TreeService.class);

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
            String bucket = getBucket(path).getName();
            String key = getKey(path).getName();
            String filename = FileUtils.generateTempFilename(bucket, key);
            File downloaded = bucketService.download(profile, bucket, key, filename);
            UiUtils.openFile(downloaded);
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
        new TreeSpeedSearch(bucketTree, x -> {
            Object[] path = x.getPath();
            return path[path.length - 1].toString();
        }, true);
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
        treeModel.setRoot(UiUtils.createTreeModel(treeService.getFullTree(profile)));
        treeModel.reload();
        this.bucketTree.setPaintBusy(false);
    }

    public JPanel getContent() {
        return mainWindowContent;
    }

    private NodeModel getBucket(TreePath path) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPath()[1];
        return (NodeModel) node.getUserObject();
    }

    private NodeModel getKey(TreePath path) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPath()[path.getPath().length - 1];
        return (NodeModel) node.getUserObject();
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
