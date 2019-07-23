package info.makarov.s3.core.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
@ToString
public class NodeModel {

    private final String name;

    private final Type type;

    private List<NodeModel> children = new ArrayList<>();

    public enum Type {
        LEAF,
        CATEGORY,
        BUCKET,
        ROOT,
        SYSTEM
    }
}
