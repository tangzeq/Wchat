package tangzeqi.com.project;

import com.intellij.openapi.project.Project;
import tangzeqi.com.tools.chat.server.ChatService;

import java.util.concurrent.ConcurrentHashMap;

public class MyProject {
    private static volatile ConcurrentHashMap<String, ChatService> cache = new ConcurrentHashMap<>();

    public static ChatService cache(String project) {
        return cache.get(project);
    }

    public static void init(Project project) {
        cache.put(project.getName(), new ChatService(project));
    }
}
