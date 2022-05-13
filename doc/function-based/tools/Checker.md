# 检查格式

 - 收集格式错误
   - 清空上次保存的数据（index，list）
   - 对翻译文件中每张图片的每个Label执行每个Typo
     - 标记起始位置index = 0
     - 使用Typo::regex和index查找循环查找
       - 找到了：存储Typo信息，index赋值为结果的尾
       - 没找到（null）：转到下一个Typo
   - 如果错误列表为空，返回true
   - 返回false