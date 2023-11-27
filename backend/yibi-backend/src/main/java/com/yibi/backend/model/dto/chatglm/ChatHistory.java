package com.yibi.backend.model.dto.chatglm;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
//{
//  "query": "恼羞成怒",
//  "history": [
//    {
//      "role": "user",
//      "content": "我们来玩成语接龙，我先来，生龙活虎"
//    },
//    {
//      "role": "assistant",
//      "content": "虎头虎脑"
//    }
//  ],
//  "stream": false,
//  "model_name": "chatglm2-6b",
//  "temperature": 0.7,
//  "max_tokens": 0,
//  "prompt_name": "default"
//}
@Data
@Builder
public class ChatHistory implements Serializable {

    private String role;

    private String content;

    private static final long serialVersionUID = 1L;
}