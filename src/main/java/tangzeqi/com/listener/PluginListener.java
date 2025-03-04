package tangzeqi.com.listener;

import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginsAdvertiser;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.service.ChatService;

public class PluginListener implements DynamicPluginListener {
    @Override
    public void beforePluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        System.out.println("beforePluginLoaded pluginDescriptor = " + pluginDescriptor);
    }

    @Override
    public void beforePluginUnload(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        System.out.println("beforePluginUnload pluginDescriptor = " + pluginDescriptor + ", isUpdate = " + isUpdate);
        ChatService.shutDown();
    }

    @Override
    public void pluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        System.out.println("pluginLoaded pluginDescriptor = " + pluginDescriptor);
    }

    @Override
    public void pluginUnloaded(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        System.out.println("pluginUnloaded pluginDescriptor = " + pluginDescriptor + ", isUpdate = " + isUpdate);
    }
}
