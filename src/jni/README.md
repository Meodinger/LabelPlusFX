# IMEInterface

C#运行时库，用来操作输入法

# IMEWrapper

C#库的JNI/C++中间件

编译选项注意事项：
 - 附加包含目录添加 $(JAVA_HOME)\include\* 
 - 调试信息使用C7兼容 /C7
 - 使用公共运行时支持 /clr
 - 关闭C++异常
 - 基本运行时检查修改为默认值
 - 关闭符合模式 /permissive
