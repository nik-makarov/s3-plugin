package info.makarov.s3.core.service;

import info.makarov.s3.core.entity.BucketNodeModel;
import info.makarov.s3.core.entity.NodeModel;
import info.makarov.s3.core.entity.preferences.Profile;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TreeService {

    private final BucketService bucketService;

    public NodeModel getFullTree(Profile profile) {
        NodeModel root = new NodeModel("root", NodeModel.Type.ROOT);
        List<NodeModel> bucketNodes = bucketService.getBucketNames(profile).stream()
                .map(x -> getTreeByBucket(profile, x))
                .collect(Collectors.toList());
        root.getChildren().addAll(bucketNodes);
        return root;
    }

    private BucketNodeModel getTreeByBucket(Profile profile, String bucket) {
        List<String> objects = bucketService.getAllKeys(profile, bucket);
        BucketNodeModel node = new BucketNodeModel(bucket, objects.size());
        group(node, objects, profile.getDelimiter(), "");
        return node;
    }

    private void group(NodeModel node, List<String> objects, String delimiter, String prefix) {
        List<String> leafs = objects.stream().filter(x -> !x.substring(prefix.length()).contains(delimiter)).collect(Collectors.toList());
        objects.removeAll(leafs);
        while (objects.size() > 0) {
            NodeModel newNode = new NodeModel(objects.get(0).substring(prefix.length()).split(delimiter)[0], NodeModel.Type.CATEGORY);
            String firstPrefix = prefix + newNode.getName() + delimiter;
            List<String> items = objects.stream().filter(x -> x.startsWith(firstPrefix)).collect(Collectors.toList());
            objects.removeAll(items);
            node.getChildren().add(newNode);
            group(newNode, items, delimiter, firstPrefix);
        }
        leafs.forEach(leaf -> node.getChildren().add(new NodeModel(leaf, NodeModel.Type.LEAF)));
    }

}
