package info.makarov.s3.ui.window;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import info.makarov.s3.core.entity.preferences.Profile;
import info.makarov.s3.core.service.PreferencesService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainConfigGui {
    private JPanel rootPanel;
    private com.intellij.ui.components.JBList profileNames;
    private JPanel profileNamesPanel;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;


    private CollectionListModel<Profile> profilesModel = new CollectionListModel<>(new ArrayList<>());

    public MainConfigGui() {
        JPanel panel = ToolbarDecorator.createDecorator(profileNames).setAddAction(x -> profilesModel.add(new Profile())).createPanel();
        profileNamesPanel.removeAll();
        profileNamesPanel.add(panel, BorderLayout.CENTER);
        update();
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    private void update() {
        PreferencesService preferences = ServiceManager.getService(PreferencesService.class);
        CollectionListModel<Profile> listModel = new CollectionListModel<>(preferences.getPreferences().getProfiles());
        profileNames.setModel(listModel);
    }

}
