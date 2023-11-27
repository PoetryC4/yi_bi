package com.yibi.backend.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelUtils {

    public static boolean isExcelFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return StringUtils.isNotBlank(fileName) && fileName.toLowerCase().endsWith(".xlsx");
    }
    public static String getCsvFromXlsx(MultipartFile multipartFile) {
        List<Map<Integer, String>> excelRes;
        try {
            excelRes = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (CollUtil.isEmpty(excelRes)) {
            return "";
        }
        StringBuilder res = new StringBuilder();
        LinkedHashMap<Integer, String> headers = (LinkedHashMap<Integer, String>) excelRes.get(0);
        List<String> headerList = headers.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        res.append(StringUtils.join(headerList, ","));
        res.append("\n");

        for (int i = 1; i < excelRes.size(); i++) {
            LinkedHashMap<Integer, String> vals = (LinkedHashMap<Integer, String>) excelRes.get(i);
            List<String> valList = vals.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            res.append(StringUtils.join(valList, ","));
            res.append("\n");
        }
        return res.toString();
    }
}
