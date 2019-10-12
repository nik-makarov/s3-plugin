package info.makarov.s3.ui.adapter;

import com.intellij.openapi.components.ServiceManager;
import info.makarov.s3.core.entity.preferences.Profile;
import info.makarov.s3.core.lazy.LazyNode;
import info.makarov.s3.core.service.BucketService;
import info.makarov.s3.core.service.LazyTreeService;
import info.makarov.s3.core.utils.FileUtils;
import info.makarov.s3.ui.utils.UiUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;

@Accessors(chain = true)
@RequiredArgsConstructor
public class LazyNodeAdapter {

    private final LazyNode source;

    private LazyNode loadMoreItemsNode;

    @Setter
    @Getter
    private LazyNodeAdapterType nodeAdapterType = LazyNodeAdapterType.DEFAULT;

    public String getName() {
        return source.getName();
    }

    public LazyNodeAdapter getChildAt(int index) {
        if (0 <= index && index < source.getChildren().size()) {
            return new LazyNodeAdapter(source.getChildren().get(index));
        }
        int size = source.getChildren().size() + (isChildrenLoading() ? 1 : 0);
        if (index == size - 1 && isChildrenLoading()) {
            return new LazyNodeAdapter(createLoadMoreItemsNode()).setNodeAdapterType(LazyNodeAdapterType.LOAD_MORE_ITEMS);
        }
        throw new IndexOutOfBoundsException(MessageFormat.format("Index: {0}, size: {1}", index, size));
    }

    public int getChildrenCount() {
        return source.getChildren().size() + (source.getChildrenStatus() == LazyNode.ChildrenStatus.LOADING ? 1 : 0);
    }

    public LazyNode.Type getType() {
        return source.getType();
    }

    public boolean isChildrenLoading() {
        return source.getChildrenStatus() == LazyNode.ChildrenStatus.LOADING;
    }

    public String getTitle() {
        switch (source.getType()) {
            case BRANCH:
                int prefixLength = 0;
                if (source.getParent().getType() == LazyNode.Type.BRANCH) {
                    prefixLength += source.getParent().getName().length();
                }
                return source.getName().substring(prefixLength);
            default:
                return source.getName();
        }
    }

    public void loadChildren(Profile profile, LazyTreeAdapter lazyTreeAdapter) {
        if (source.getChildrenStatus() == LazyNode.ChildrenStatus.LOADED) {
            return;
        }
        // не нравится доставать так сервис, но лучше пока не придумал
        LazyTreeService treeService = ServiceManager.getService(LazyTreeService.class);
        treeService.loadChildren(profile, lazyTreeAdapter.getChildrenLoadLimit(), source);
    }

    public void onPreviewClick(Profile profile, LazyTreeAdapter lazyTreeAdapter) {
        if (this.getNodeAdapterType() == LazyNodeAdapterType.LOAD_MORE_ITEMS) {
            LazyTreeService treeService = ServiceManager.getService(LazyTreeService.class);
            treeService.loadChildren(profile, lazyTreeAdapter.getChildrenLoadLimit(), this.source.getParent());
            return;
        }
        if (this.getChildrenCount() > 0) { // is not leaf
            return;
        }
        LazyNode node = source;
        String key = node.getName();
        while (node.getType() != LazyNode.Type.BUCKET) {
            node = node.getParent();
        }
        String bucket = node.getName();
        BucketService bucketService = ServiceManager.getService(BucketService.class);
        String filename = FileUtils.generateTempFilename(bucket, key);
        File downloaded = bucketService.download(profile, bucket, key, filename);
        UiUtils.openFile(downloaded);
    }

    private LazyNode createLoadMoreItemsNode() {
        if (loadMoreItemsNode == null) {
            loadMoreItemsNode = new LazyNode(source, "Load more...", LazyNode.Type.LEAF, Collections.emptyList())
                    .setChildrenStatus(LazyNode.ChildrenStatus.LOADED);
        }
        return loadMoreItemsNode;
    }

}
