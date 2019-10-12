package info.makarov.s3.ui.utils;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import info.makarov.s3.ui.adapter.LazyNodeAdapter;
import info.makarov.s3.ui.models.S3TreeNode;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.io.File;
import java.io.IOException;

@UtilityClass
public class UiUtils {

    public S3TreeNode createTreeModel(LazyNodeAdapter root) {
        S3TreeNode treeNode = new S3TreeNode(root);
        for (int i = 0; i < root.getChildrenCount(); i++) {
            treeNode.add(new S3TreeNode(root.getChildAt(i)));
        }
        return treeNode;
    }

    public void updateTree(S3TreeNode uiNode) {
        LazyNodeAdapter root = uiNode.getUserObject();
        uiNode.removeAllChildren();
        for (int i = 0; i < root.getChildrenCount(); i++) {
            uiNode.add(new S3TreeNode(root.getChildAt(i)));
        }
    }

    public void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            Messages.showWarningDialog("Default application for " + file.getName() + " not found", "Sorry");
        }
    }

    public void chooseFile(Consumer<? super VirtualFile> callback) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
        FileChooser.chooseFile(descriptor, null, null, callback);
    }
}
