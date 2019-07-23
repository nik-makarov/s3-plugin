package info.makarov.s3.core.entity;

import lombok.Getter;

@Getter
public class BucketNodeModel extends NodeModel {

    private final long objectsCount;

    public BucketNodeModel(String name, long objectsCount) {
        super(name, Type.BUCKET);
        this.objectsCount = objectsCount;
    }

}
