package info.makarov.s3.core.lazy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;


@RequiredArgsConstructor
@Getter
@Accessors(chain = true)
public class LazyNode {

    /**
     * null if root
     */
    private final LazyNode parent;

    private final String name;

    private final Type type;

    private final List<LazyNode> children;

    @Setter
    private ChildrenStatus childrenStatus = ChildrenStatus.NEW;

    @Setter
    private String token;

    public enum Type {
        LEAF,
        BRANCH,
        BUCKET,
        ROOT
    }

    public enum ChildrenStatus {
        NEW,
        LOADING,
        LOADED
    }

}
