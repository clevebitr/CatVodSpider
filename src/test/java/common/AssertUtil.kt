@file:JvmName("AssertUtil")

package common

import cn.hutool.core.collection.CollectionUtil
import com.github.catvod.bean.Result
import org.apache.commons.lang3.StringUtils

// 可选：定义一个自定义异常，便于上层捕获处理
class AssertResultException(message: String) : RuntimeException(message)

object AssertUtil {

    /**
     * 验证字符串是否能解析为有效的 Result 对象，并包含 url 或 list 数据
     * @param s 输入的 JSON 字符串
     * @throws AssertResultException 当输入无效或缺少必要字段时抛出
     */
    fun assertResult(s: String) {
        // 检查输入字符串是否为空
        if (StringUtils.isBlank(s)) {
            throw AssertResultException("Input string is null or blank.")
        }

        // 尝试解析 JSON
        val result: Result = try {
            Result.objectFrom(s) ?: throw AssertResultException("Failed to parse JSON: parsed result is null.")
        } catch (e: Exception) {
            throw AssertResultException("Failed to parse JSON: ${e.message}")
        }

        // 校验 url 或 list 是否有有效数据
        when {
            result.url != null -> {
                when (result.url) {
                    is String -> {
                        if (!StringUtils.isNotBlank(result.url as String)) {
                            throw AssertResultException("Result.url is an empty string.")
                        }
                    }
                    is List<*> -> {
                        val urlList = result.url as List<*>
                        if (CollectionUtil.isEmpty(urlList)) {
                            throw AssertResultException("Result.url is an empty list.")
                        }
                    }
                    else -> {
                        throw AssertResultException("Result.url has unsupported type: ${result.url::class.java}")
                    }
                }
            }
            else -> {
                if (result.list.isNullOrEmpty()) {
                    throw AssertResultException("Both Result.url and Result.list are null or empty.")
                }
            }
        }
    }
}