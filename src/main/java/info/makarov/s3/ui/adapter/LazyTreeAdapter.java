package info.makarov.s3.ui.adapter;

import info.makarov.s3.core.lazy.LazyTree;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LazyTreeAdapter {

    private final LazyTree tree;

    private LazyNodeAdapter rootAdapter;

    public LazyNodeAdapter getRoot() {
        if (rootAdapter == null) {
            rootAdapter = new LazyNodeAdapter(tree.getRoot());
        }
        return rootAdapter;
    }

    public int getChildrenLoadLimit() {
        return tree.getChildrenLoadLimit();
    }

}
