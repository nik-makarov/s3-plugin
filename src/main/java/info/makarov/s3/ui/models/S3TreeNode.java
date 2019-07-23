package info.makarov.s3.ui.models;

import info.makarov.s3.core.entity.NodeModel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class S3TreeNode extends DefaultMutableTreeNode {

    public S3TreeNode(NodeModel node) {
        super(node, true);
    }

    public S3TreeNode(NodeModel node, boolean allowChildren) {
        super(node, allowChildren);
    }

    public void setUserObject(NodeModel userObject) {
        super.setUserObject(userObject);
    }

    @Override
    public NodeModel getUserObject() {
        return (NodeModel) super.getUserObject();
    }

    @Override
    public NodeModel[] getUserObjectPath() {
        return (NodeModel[]) super.getUserObjectPath();
    }

    @Override
    public S3TreeNode getFirstChild() {
        return (S3TreeNode) super.getFirstChild();
    }

    @Override
    public S3TreeNode getLastChild() {
        return (S3TreeNode) super.getLastChild();
    }

    @Override
    public S3TreeNode getChildAfter(TreeNode aChild) {
        return (S3TreeNode) super.getChildAfter(aChild);
    }

    @Override
    public S3TreeNode getChildAt(int index) {
        return (S3TreeNode) super.getChildAt(index);
    }

    @Override
    public String toString() {
        return getUserObject().getName();
    }
}
