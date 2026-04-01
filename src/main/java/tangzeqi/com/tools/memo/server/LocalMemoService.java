package tangzeqi.com.tools.memo.server;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang3.ObjectUtils;
import org.jaxen.Function;
import tangzeqi.com.tools.memo.MemoService;
import tangzeqi.com.tools.memo.model.MemoItem;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static tangzeqi.com.utils.FileUtils.safeFileOperation;

public class LocalMemoService implements MemoService {
    private static final String STORAGE_DIR = System.getProperty("user.home") + File.separator + ".wchat-memo";
    private static final String STORAGE_FILE = STORAGE_DIR + File.separator + "memos.txt";

    public LocalMemoService() {
        ensureStorageDirExists();
        ensureStorageFileExists();
    }

    @Override
    public void timer(Function function) {
        SwingUtilities.invokeLater(() -> {
            new Thread(() ->{
            while (true)
            try {
                Thread.sleep(10000);
                safeFileOperation(Paths.get(STORAGE_FILE), list -> {
                    for (int i = 0; i < list.size(); i++) {
                        MemoItem memoItem = JSON.parseObject(list.get(i), MemoItem.class);
                        if (memoItem.isCompleted()) {
                            continue;
                        }
                        if (memoItem.getReminderTime() == null) {
                            continue;
                        }
                        if (memoItem.getReminderDate().before(new java.util.Date())) {
                            //弹出提醒
                            int i1 = JOptionPane.showInternalOptionDialog(null, memoItem.getContent(), "任务提醒",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE, null, new String[]{"好的","稍后"}, null);
                            if (i1 == 0) {
                                memoItem.complete();
                                if(!memoItem.isCompleted())memoItem.setNextReminderTime();
                            }
                            list.set(i,JSON.toJSONString(memoItem));
                        }
                    }
                    return list;
                });
                function.call(null,null);
            } catch (Exception e) {}}).start();
        });
    }

    private void ensureStorageFileExists() {
        Path filePath = Paths.get(STORAGE_FILE);
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void ensureStorageDirExists() {
        Path dirPath = Paths.get(STORAGE_DIR);
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void addMemo(MemoItem memoItem) {
        safeFileOperation(Paths.get(STORAGE_FILE), list -> {
                list.add(JSON.toJSONString(memoItem));
                return list;
        });
    }

    @Override
    public void updateMemo(MemoItem memoItem) {
        safeFileOperation(Paths.get(STORAGE_FILE), list -> {
            for (int i = 0; i < list.size(); i++) {
                if(JSON.parseObject(list.get(i)).getString("id").equals(memoItem.getId())) {
                    list.set(i, JSON.toJSONString(memoItem));
                    break;
                }
            }
            return list;
        });
    }

    @Override
    public void deleteMemo(String id) {
        safeFileOperation(Paths.get(STORAGE_FILE), list -> {
            AtomicReference<String> remove = new AtomicReference<>();
            for (String e : list) {
                if(JSON.parseObject(e).getString("id").equals(id)) {
                    remove.set(e);
                    break;
                }
            }
            if(ObjectUtils.isNotEmpty(remove.get())) {
                list.remove(remove.get());
            }
            return list;
        });
    }

    @Override
    public void markAsCompleted(String id, boolean completed) {
        MemoItem memo = getMemoById(id);
        memo.setCompleted(false);
        memo.setNextReminderTime();
        updateMemo(memo);
    }

    /**
     * 获取所有备忘录项
     *
     * @return 包含所有备忘录项的列表，每个项是从存储文件中解析的MemoItem对象
     */
    @Override
    public List<MemoItem> getAllMemos() {
        return safeFileOperation(Paths.get(STORAGE_FILE), list -> list).stream().map(e->JSON.parseObject(e,MemoItem.class)).toList();
    }


    @Override
    public MemoItem getMemoById(String id) {
        return safeFileOperation(Paths.get(STORAGE_FILE), list -> list).stream().map(e->JSON.parseObject(e,MemoItem.class)).filter(p->p.getId().equals(id)).findFirst().orElse(new MemoItem());
    }

}