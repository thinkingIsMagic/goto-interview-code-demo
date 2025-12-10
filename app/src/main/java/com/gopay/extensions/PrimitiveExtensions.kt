package com.gopay.extensions

import java.math.RoundingMode

/**
 * 判断字符串是否为非空有效。
 */
fun String?.isValid(): Boolean {
    return !this.isNullOrEmpty()
}

/**
 * 对 Int 进行除法并保留指定位数小数，向上取整。
 * @param divideBy 除数，使用 Double 以避免整数截断
 * @param decimals 小数位数，默认两位
 */
fun Int.divide(divideBy: Double, decimals: Int = 2): String {
    return (this / divideBy).toBigDecimal().setScale(decimals, RoundingMode.UP).toPlainString()
}