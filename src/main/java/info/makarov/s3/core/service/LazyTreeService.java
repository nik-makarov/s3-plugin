package info.makarov.s3.core.service;

import info.makarov.s3.core.entity.preferences.Profile;
import info.makarov.s3.core.exception.BucketNodeNotFoundException;
import info.makarov.s3.core.lazy.LazyNode;
import info.makarov.s3.core.lazy.LazyTree;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LazyTreeService {

    private static final int CHILDREN_LOAD_LIMIT = 3;

    private final BucketService bucketService;

    public LazyTree getLazyTree(Profile profile) {
        LazyNode root = new LazyNode(null, "root", LazyNode.Type.ROOT, new ArrayList<>())
                .setChildrenStatus(LazyNode.ChildrenStatus.LOADED);
        List<LazyNode> buckets = bucketService.getBuckets(profile).stream()
                .map(bucket -> new LazyNode(root, bucket.name(), LazyNode.Type.BUCKET, new ArrayList<>()))
                .collect(Collectors.toList());
        root.getChildren().addAll(buckets);
        return new LazyTree(root, CHILDREN_LOAD_LIMIT);
    }

    public void loadChildren(Profile profile, int loadLimit, LazyNode node) {
        if (node.getChildrenStatus() == LazyNode.ChildrenStatus.LOADED) {
            return;
        }
        String prefix = null;
        if (node.getType() == LazyNode.Type.BRANCH) {
            prefix = node.getName();
        }
        ListObjectsV2Response objects = bucketService.getObjects(profile, findBucket(node).getName(), prefix, node.getToken(), loadLimit);

        List<LazyNode> prefixes = objects.commonPrefixes().stream()
                .map(commonPrefix -> new LazyNode(node, commonPrefix.prefix(), LazyNode.Type.BRANCH, new ArrayList<>()))
                .collect(Collectors.toList());

        List<LazyNode> leafs = objects.contents().stream()
                .map(object -> new LazyNode(node, object.key(),
                        LazyNode.Type.LEAF,
                        Collections.emptyList()).setChildrenStatus(LazyNode.ChildrenStatus.LOADED))
                .collect(Collectors.toList());
        node.getChildren().addAll(prefixes);
        node.getChildren().addAll(leafs);
        if (objects.isTruncated()) {
            node.setChildrenStatus(LazyNode.ChildrenStatus.LOADING);
            node.setToken(objects.nextContinuationToken());
        } else {
            node.setChildrenStatus(LazyNode.ChildrenStatus.LOADED);
            node.setToken(null);
        }
    }

    private LazyNode findBucket(LazyNode node) {
        while (true) {
            if (node.getType() == LazyNode.Type.BUCKET) {
                return node;
            }
            if (node.getType() == null) {
                throw new BucketNodeNotFoundException("Bucket node of " + node + " not found");
            }
            node = node.getParent();
        }
    }

}
