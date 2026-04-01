package tangzeqi.com.tools.memo;

import org.jaxen.Function;
import tangzeqi.com.tools.memo.model.MemoItem;

import java.util.List;

public interface MemoService {
    void addMemo(MemoItem memoItem);
    void updateMemo(MemoItem memoItem);
    void deleteMemo(String id);
    void markAsCompleted(String id, boolean completed);
    List<MemoItem> getAllMemos();
    MemoItem getMemoById(String id);

    void timer(Function function);
}