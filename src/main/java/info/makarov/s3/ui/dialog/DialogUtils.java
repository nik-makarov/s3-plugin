package info.makarov.s3.ui.dialog;

import com.intellij.openapi.ui.Messages;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DialogUtils {

    public void showNotImplemented() {
        Messages.showWarningDialog("The function is not implemented yet =(", "Sorry");
    }

}
