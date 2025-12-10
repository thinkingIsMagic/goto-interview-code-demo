package com.gopay.extensions

import java.math.RoundingMode

/**
 * 基本类型扩展函数集合
 * 
 * 这些扩展函数为基本类型（String、Int等）添加了便利方法。
 */

/**
 * 检查字符串是否有效
 * 
 * 判断字符串是否不为null且不为空。
 * 
 * @return true表示字符串有效（不为null且不为空），false表示无效
 * 
 * 使用示例：
 * val name: String? = getUserName()
 * if (name.isValid()) {
 *     // 字符串有效，可以使用
 * }
 * 
 * 注意：这是String?的扩展函数，可以安全地处理null值
 */
fun String?.isValid(): Boolean {
    return !this.isNullOrEmpty()
}

/**
 * 整数除法，返回格式化的字符串
 * 
 * 将一个整数除以一个浮点数，并返回指定小数位数的字符串。
 * 使用BigDecimal确保精度，使用UP舍入模式（向上舍入）。
 * 
 * @param divideBy 除数（浮点数）
 * @param decimals 保留的小数位数，默认为2位
 * @return 格式化后的字符串
 * 
 * 使用示例：
 * val price = 100
 * val priceInDollars = price.divide(100.0, 2)  // 返回 "1.00"
 * val priceInCents = price.divide(100.0, 0)    // 返回 "1"
 * 
 * 应用场景：
 * - 货币转换（如分转元）
 * - 单位转换（如厘米转米）
 * - 百分比计算
 */
fun Int.divide(divideBy: Double, decimals: Int = 2): String {
    // 执行除法运算
    val result = this / divideBy
    // 转换为BigDecimal以确保精度
    // setScale设置小数位数，RoundingMode.UP表示向上舍入
    // toPlainString()返回不带科学计数法的字符串
    return result.toBigDecimal().setScale(decimals, RoundingMode.UP).toPlainString()
}