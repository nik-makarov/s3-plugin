package info.makarov.s3.core.lazy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LazyTree {

    private final LazyNode root;

    private final int childrenLoadLimit;

}
