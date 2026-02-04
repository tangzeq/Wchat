package tangzeqi.com.tools.mind.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoredEntry {
    private String content;
    private double score;

}
