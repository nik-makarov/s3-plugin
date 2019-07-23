package info.makarov.s3;

import com.intellij.openapi.components.BaseComponent;
import org.jetbrains.annotations.NotNull;

public class ApplicationComponent implements BaseComponent {

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "S3 Support";
    }
}
