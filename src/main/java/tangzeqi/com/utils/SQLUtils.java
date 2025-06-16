package tangzeqi.com.utils;

import com.google.gson.JsonObject;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

public class SQLUtils {

    public static String selectToJson(String sql) {
        try {
            JsonObject jsonObject = new JsonObject();
            Statement parse = CCJSqlParserUtil.parse(sql);
            if(parse instanceof Select) {
                SelectBody selectBody = ((Select) parse).getSelectBody();
                if(selectBody instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) selectBody;
                    for (SelectItem selectItem : plainSelect.getSelectItems()) {
                        selectItem.accept(new SelectItemVisitorAdapter() {
                            @Override
                            public void visit(SelectExpressionItem selectExpressionItem) {
                                String columnName = selectExpressionItem.getExpression().toString();
                                String propertyName = columnName;
                                if (selectExpressionItem.getAlias() != null) {
                                    propertyName = selectExpressionItem.getAlias().getName();
                                }
                                jsonObject.addProperty(propertyName, "");
                            }
                        });
                    }
                }
            }
            return jsonObject.toString();
        } catch (Throwable ee) {
            ee.printStackTrace();
            return "Error occurred: " + ee.getMessage();
        }
    }

}
