package com.yibi.backend.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * SQL 工具
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }

    public static boolean isAnyNull(Object... os) {
        if (!ArrayUtils.isEmpty(os)) {
            Object[] var1 = os;
            int var2 = os.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Object o = var1[var3];
                if (o == null) {
                    return true;
                }
            }
        }
        return false;
    }
}
