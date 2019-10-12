package info.makarov.s3.ui.models;

import info.makarov.s3.core.lazy.LazyNode;
import info.makarov.s3.ui.adapter.LazyNodeAdapter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class S3TreeNode extends DefaultMutableTreeNode {

    public S3TreeNode(LazyNodeAdapter node) {
        super(node, true);
    }

    public S3TreeNode(LazyNodeAdapter node, boolean allowChildren) {
        super(node, allowChildren);
    }

    public void setUserObject(LazyNodeAdapter userObject) {
        super.setUserObject(userObject);
    }

    @Override
    public LazyNodeAdapter getUserObject() {
        return (LazyNodeAdapter) super.getUserObject();
    }

    @Override
    public LazyNodeAdapter[] getUserObjectPath() {
        return (LazyNodeAdapter[]) super.getUserObjectPath();
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
    public boolean isLeaf() {
        return getUserObject().getType() == LazyNode.Type.LEAF;
    }

    @Override
    public S3TreeNode getParent() {
        return (S3TreeNode) super.getParent();
    }

    @Override
    public String toString() {
        return getUserObject().getTitle();
    }
}
