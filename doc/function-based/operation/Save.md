# 保存操作

 - 流程（目标文件，静默保存）
   - 如果文件存在，标记为覆盖
   - 如果不是静默保存
     - 如果保存位置不是TransFile的项目文件夹
       - 取消保存：中止
       - 确认保存：继续
   - 设置导出目标
     - 存在覆盖标记，建立临时文件
     - 使用目标文件
   - 导出到导出目标
     - 成功：继续
     - 失败：中止
   - 如果存在覆盖标记
     - 传输导出目标到目标文件
       - 成功：继续
       - 失败：中止
   - 更新状态
     - 设置翻译文件位置
     - 标记为未改动
   - 添加目标文件到最近文件
   - 保存目标文件的翻译进度
   - 更新标题
   - 如果不是静默保存
     - 提示保存成功