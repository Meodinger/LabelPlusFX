# 新建操作

 - 流程（目标文件）
   - 确定项目位置，选择图片
     - 默认使用目标文件的文件夹作为项目位置
     - 查找项目文件夹下的图片
       - 没有找到任何图片，确认是否要手动选择项目文件夹
         - 手动选择，重新查找
         - 不选择，提示操作取消，新建操作中止，返回null
       - 找到了，继续
   - 显示选择条目对话框
     - 直接关闭了：提示操作取消，新建操作中止，返回null
     - 什么都没选：提示至少需要一张图片，新建操作中止：返回null
     - 选择了，继续
   - 生成TransFile
     - 使用默认文件版本
     - 使用默认注释
     - 根据预设分组生成分组列表
     - 根据选择的图片生成翻译映射
   - 导出TransFile
     - 导出失败，返回null
     - 导出成功，返回项目文件夹