package com.yibi.backend.model.dto.chatglm;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
//{"text": "\"军机大臣\"是清朝时期的一个官职名称，通常指的是负责军事机密事务的官员。\n\n在清朝，军机大臣是雍正皇帝设立的一个机要机构，主要负责处理军事机密事务，协助皇帝处理政务。军机大臣的职责非常重要，因为他们的决策可以直接影响军事行动和战争结果。因此，军机大臣通常是由亲信大臣担任，地位高，权力大。\n\n在鸦片战争期间，太平天国运动的领袖洪秀全也曾担任过军机大臣一职。", "chat_history_id": "ece334da0e044815b79914f73c10a7ff"}
@Data
@Builder
public class ChatGLMResponse implements Serializable {

    private String text;

    private String chat_history_id;

    private static final long serialVersionUID = 1L;
}