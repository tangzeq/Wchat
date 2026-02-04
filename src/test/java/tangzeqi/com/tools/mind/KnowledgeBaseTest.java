package tangzeqi.com.tools.mind;

import tangzeqi.com.tools.mind.server.LightweightMindService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class KnowledgeBaseTest {
    public static void main(String[] args) {
        // 创建MindService实例
        MindService mindService = new LightweightMindService();
        
        // 知识库文件路径
        String knowledgeBasePath = "知识库.txt";
        
        try {
            // 读取知识库文件内容
            List<String> lines = Files.readAllLines(Paths.get(knowledgeBasePath));
            
            // 遍历每一行并存储
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    mindService.set(line, null);
                }
            }
            
            System.out.println("知识库存储完成！");
            
        } catch (IOException e) {
            System.err.println("读取知识库文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}