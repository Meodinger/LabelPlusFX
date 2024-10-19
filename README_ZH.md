<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->


<!-- PROJECT LOGO -->
简体中文 | [English](/README.md)
<br />
<p align="center">
  <a href="https://github.com/Meodinger/LabelPlusFX">
    <img src="images/logo.png" alt="Logo" width="80" height="80" />
  </a>
  <h3 align="center">Label Plus FX</h3>
  <p align="center">
    一个跨平台的Label Plus
    <br />
    <br />
    <a href="https://www.kdocs.cn/l/cpRyDN2Perkb">用户手册</a>
    ·
    <a href="https://github.com/Meodinger/LabelPlusFX/issues">反馈问题</a>
    ·
    <a href="https://github.com/Meodinger/LabelPlusFX/issues">提交建议</a>
  </p>
</p>


<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">目录</h2></summary>
  <ol>
    <li>
      <a href="#about-the-project">关于本项目</a>
    </li>
    <li>
      <a href="#getting-started">开始</a>
      <ul>
        <li><a href="#prerequisites">环境</a></li>
        <li><a href="#installation">启动步骤</a></li>
      </ul>
    </li>
    <li><a href="#usage">说明</a></li>
    <li><a href="#license">许可协议</a></li>
    <li><a href="#contact">联系方式</a></li>
  </ol>
</details>


<!-- ABOUT THE PROJECT -->
## 关于本项目

[![Product Screen Shot][product-screenshot]]()

本项目受到 [LabelPlus](https://noodlefighter.com/label_plus/)的启发。


<!-- GETTING STARTED -->
## 开始

复制本项目并启动需要以下几个简单的步骤

### 环境

 * [Liberica JDK 17 (完整版本)](https://bell-sw.com/pages/downloads/#/java-17-lts%20/%20current) : 用于主应用程序；

 * [可选] [Visual Studio 2019](https://visualstudio.microsoft.com/zh-hans/downloads/) : 用于Windows IME JNI接口；


### 启动步骤

1.克隆仓库
   ```sh
   git clone https://github.com/Meodinger/LabelPlusFX.git
   ```
2. 运行Maven命令 `package`

3. 运行脚本,  `link.bat`  `build.bat` 都可以

4. 对于Windows用户， 构建封装器库 `IMEWrapper` 然后复制 `IMEInterface.dll` 和 `IMEWrapper.dll` 到 `LabelPlusFX.exe` （使用`jpackage`）或 `runtime\java.exe`（使用`jlink`） 所在的文件夹下.

> 如果不想使用Windows IME JNI接口, 可以使用 `run.bat --disable-jni` 或`LabelPlusFX.exe --disable-jni`方式启动

> 在IDE中运行LPFX, 可以执行 `exec:java@run` 命令

<!-- USAGE EXAMPLES -->
## 说明

Label Plus FX的功能设计基于 [LabelPlus](https://noodlefighter.com/label_plus/)

更多示例，请参考用户手册和Wiki [User Manual](https://www.kdocs.cn/l/seRSJCKVOn0Y) 和 [Wiki](https://github.com/Meodinger/LabelPlusFX/wiki)


<!-- CONTRIBUTING -->
## 贡献

开源社区因贡献而变得如此美好，充满学习、启发和创造。非常感谢您所做的**任何贡献**

1. Fork项目
2. 创建功能分支  (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m '添加了一些很棒的改进'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 发起拉取请求


<!-- LICENSE -->
## 许可协议

根据AGPLv3许可证分发。有关更多信息，请参见`LICENSE`页面。


<!-- CONTACT -->
## 联系方式

Meodinger Wang - [@Meodinger_Wang](https://twitter.com/Meodinger_Wang) - meodinger@qq.com

项目链接: [https://github.com/Meodinger/LabelPlusFX](https://github.com/Meodinger/LabelPlusFX)

<!-- SPONSOR -->

## 赞助

<a href="https://afdian.net/@Meodinger">
  <img src="https://s2.loli.net/2022/04/01/p4kequKy9g7EMZb.jpg" alt="Aifadian" width="375" />
</a>

[product-screenshot]: https://s2.loli.net/2022/02/04/2H7bguJ9rcyBjUO.png