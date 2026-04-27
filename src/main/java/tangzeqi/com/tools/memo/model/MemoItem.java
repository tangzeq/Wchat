package tangzeqi.com.tools.memo.model;

import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MemoItem {
    private String id;
    private String content;
    private String cardColor;
    private String textColor;
    private String reminderTime;
    private String repeatRule;
    private boolean completed;
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public MemoItem() {
        this.id = UUID.randomUUID().toString();
    }

    public MemoItem(String content, String cardColor, String textColor, String reminderTime, String repeatRule) {
        this();
        this.content = content;
        this.cardColor = cardColor;
        this.textColor = textColor;
        this.reminderTime = reminderTime;
        this.repeatRule = repeatRule;
        this.completed = false;
    }

    public MemoItem(String id, String content, String cardColor, String textColor, String reminderTime, String repeatRule, boolean completed) {
        this.id = id;
        this.content = content;
        this.cardColor = cardColor;
        this.textColor = textColor;
        this.reminderTime = reminderTime;
        this.repeatRule = repeatRule;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCardColor() {
        return cardColor;
    }

    public void setCardColor(String cardColor) {
        this.cardColor = cardColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    @SneakyThrows
    public Date getReminderDate() {
        return sdf.parse(getReminderTime());
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getRepeatRule() {
        return repeatRule;
    }

    public void setRepeatRule(String repeatRule) {
        this.repeatRule = repeatRule;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void complete() {
        switch (getRepeatRule()) {
            case "日" -> setCompleted(false);
            case "月" -> setCompleted(false);
            case "年" -> setCompleted(false);
            default -> setCompleted(true);
        }
    }

    @SneakyThrows
    public void setNextReminderTime() {
        // TODO: 根据repeatRule计算下一次提醒时间
        Date date = sdf.parse(getReminderTime());
        Date now = new Date();
        date = DateUtils.setYears(date, now.getYear() + 1900);
        switch (getRepeatRule()) {
            case "日" -> {
                date = DateUtils.setMonths(date, now.getMonth());
                date = DateUtils.setDays(date, now.getDate());
                date = DateUtils.addDays(date, 1);
            }
            case "月" -> {
                date = DateUtils.setMonths(date, now.getMonth());
                date = DateUtils.addMonths(date, 1);
            }
            case "年" -> {
                date = DateUtils.addYears(date, 1);
            }
            default -> {
                date = DateUtils.setMonths(date, now.getMonth());
                date = DateUtils.setDays(date, now.getDate());
                date = DateUtils.setHours(date, now.getHours());
                date = DateUtils.addHours(date, 1);
            }
        }
        setReminderTime(sdf.format(date));
    }

}