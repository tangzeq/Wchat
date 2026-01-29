package tangzeqi.com.tools.mind;

import java.util.List;

public interface MindService {
    /**
     * 向备忘录中储存永久的本地持久化记忆
     * @param mind
     * @return
     */
    public String set(String mind);

    /**
     * 根据输入内容从永久记忆中查找，并按优先级获取前十条
     * @param mind
     * @return
     */
    public List<String> get(String mind);
}
